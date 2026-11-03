"""Mini-lab real D3. Execute após mvn clean verify, com Python do venv IA.

Uso: /tmp/b1-d3-venv/bin/python docs/evidencias/desafio-3/executar-lab.py
Inicia somente processos próprios, usa portas livres e H2 em memória.
"""
import concurrent.futures
import datetime
import json
import os
from pathlib import Path
import socket
import subprocess
import sys
import threading
import time
import urllib.parse
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

ROOT = Path(__file__).resolve().parents[3]
OUT = Path(__file__).resolve().parent
results = {"data": datetime.datetime.now().astimezone().isoformat(), "casos": []}


def port():
    with socket.socket() as s:
        s.bind(("127.0.0.1", 0))
        return s.getsockname()[1]


def request(base, path, method="GET", body=None):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(base + path, data=data, method=method,
                                 headers={"Content-Type": "application/json"})
    start = time.monotonic()
    with urllib.request.urlopen(req, timeout=15) as response:
        return {"metodo": method, "caminho": path, "http": response.status,
                "resposta": json.load(response),
                "duracao_ms": round((time.monotonic() - start) * 1000, 2)}


def record(name, response, predicate):
    response["caso"] = name
    response["passou"] = bool(predicate(response["resposta"]))
    results["casos"].append(response)
    print(json.dumps(response, ensure_ascii=False), flush=True)
    assert response["passou"], name
    return response["resposta"]


def ready(base, path, process):
    end = time.monotonic() + 90
    while time.monotonic() < end:
        if process.poll() is not None:
            raise RuntimeError("Processo encerrou antes de ficar disponível")
        try:
            request(base, path)
            return
        except Exception:
            time.sleep(0.2)
    raise RuntimeError("Inicialização excedeu 90 segundos")


def stop(process):
    if process is not None and process.poll() is None:
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()


class Slow(BaseHTTPRequestHandler):
    entered = threading.Event()

    def do_GET(self):
        self.entered.set()
        time.sleep(3)
        try:
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(b'{"resposta":"tardia","recomendacoes":[]}')
        except (BrokenPipeError, ConnectionResetError):
            pass

    def log_message(self, *args):
        pass


def main():
    python_port, core_port = port(), port()
    base = "http://127.0.0.1:" + str(core_port)
    python_base = "http://127.0.0.1:" + str(python_port)
    results["ambiente"] = {"python": sys.version, "core_url": base,
                           "ia_url": python_base, "banco": "H2 em memória"}
    core = ia = slow = None
    with (OUT / "core-log.txt").open("w") as core_log, (OUT / "fastapi-log.txt").open("w") as ia_log:
        def start_ia():
            return subprocess.Popen([sys.executable, "-m", "uvicorn", "main:app", "--host",
                                     "127.0.0.1", "--port", str(python_port)],
                                    cwd=ROOT / "labs/ia", stdout=ia_log, stderr=subprocess.STDOUT)
        try:
            ia = start_ia()
            ready(python_base, "/health", ia)
            env = dict(os.environ, IA_BASE_URL=python_base, SERVER_PORT=str(core_port),
                       SPRING_DATASOURCE_URL="jdbc:h2:mem:d3lab",
                       SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.h2.Driver",
                       SPRING_DATASOURCE_USERNAME="sa", SPRING_DATASOURCE_PASSWORD="",
                       SPRING_JPA_HIBERNATE_DDL_AUTO="create-drop")
            core = subprocess.Popen(["java", "-jar", "target/TDD-0.0.1-SNAPSHOT.jar"],
                                    cwd=ROOT, env=env, stdout=core_log, stderr=subprocess.STDOUT)
            ready(base, "/ia/recomendacoes/1", core)
            record("FastAPI health", request(python_base, "/health"), lambda x: x["status"] == "ok")
            record("RF03 contrato real", request(base, "/ia/recomendacoes/1"),
                   lambda x: x["aluno_id"] == 1 and len(x["recomendacoes"]) == 2)
            query = urllib.parse.urlencode({"pergunta": "Como estudar arquitetura?"})
            record("RF04 contrato real", request(base, "/ia/assistente?" + query),
                   lambda x: "Laboratório sem LLM" in x["resposta"])
            stop(ia)
            record("RF03 Python interrompido", request(base, "/ia/recomendacoes/1"),
                   lambda x: x["status"] == "indisponivel" and x["recomendacoes"] == [])
            record("RF04 Python interrompido", request(base, "/ia/assistente?" + query),
                   lambda x: x["status"] == "indisponivel")
            aluno = record("Criar aluno sem Python", request(base, "/alunos", "POST", {"media": 8}),
                           lambda x: x["cursosLiberados"] == 0)
            path = "/alunos/" + str(aluno["id"])
            for n in range(1, 5):
                record("Conclusão %d sem Python" % n, request(base, path + "/concluir-curso", "POST"),
                       lambda x, n=n: x["cursosLiberados"] == 3 * n)
            record("Fórum sem Python", request(base, path + "/recompensar-forum", "POST",
                   {"topicosCriados": 10, "maiorQuantidadeTopicosDosOutros": 8, "comentariosDeAjuda": 3}),
                   lambda x: x["cursosLiberados"] == 13)
            record("Premium sem Python", request(base, path + "/verificar-premium", "POST"),
                   lambda x: x["plano"] == "Premium" and x["voucherProjetosReais"])
            ia = start_ia()
            ready(python_base, "/health", ia)
            record("RF03 recuperação sem reiniciar Core", request(base, "/ia/recomendacoes/1"),
                   lambda x: len(x["recomendacoes"]) == 2)
            record("RF04 recuperação sem reiniciar Core", request(base, "/ia/assistente?" + query),
                   lambda x: "Laboratório sem LLM" in x["resposta"])
            stop(ia)
            slow = ThreadingHTTPServer(("127.0.0.1", python_port), Slow)
            threading.Thread(target=slow.serve_forever, daemon=True).start()
            for endpoint in ["/ia/recomendacoes/1", "/ia/assistente?" + query]:
                Slow.entered.clear()
                with concurrent.futures.ThreadPoolExecutor() as pool:
                    pending = pool.submit(request, base, endpoint)
                    assert Slow.entered.wait(5), "Servidor lento não recebeu a chamada"
                    record("Core durante chamada IA lenta", request(base, path + "/concluir-curso", "POST"),
                           lambda x: x["cursosLiberados"] in (16, 19))
                    result = pending.result()
                record("Timeout leitura real " + endpoint, result,
                       lambda x: x["status"] == "indisponivel")
                assert 1000 <= result["duracao_ms"] < 2800, result
            results["sucesso"] = True
        except Exception as error:
            results["sucesso"] = False
            results["erro"] = repr(error)
            raise
        finally:
            if slow:
                slow.shutdown()
                slow.server_close()
            stop(ia)
            stop(core)
            (OUT / "resultados-lab.json").write_text(json.dumps(results, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    main()
