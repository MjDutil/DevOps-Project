"""Na raiz, após mvn clean verify: python3 docs/evidencias/desafio-4/executar-lab.py.
Processos próprios, H2 descartável, portas livres, fila de capacidade 1.
"""
import concurrent.futures
import datetime
import json
import os
from pathlib import Path
import socket
import subprocess
import sys
import time
import urllib.request

ROOT = Path(__file__).resolve().parents[3]
OUT = Path(__file__).resolve().parent
resultados = {"data": datetime.datetime.now().astimezone().isoformat(), "casos": []}

def porta():
    with socket.socket() as s:
        s.bind(("127.0.0.1", 0))
        return s.getsockname()[1]

def http(base, path, method="GET", body=None):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(base + path, data=data, method=method,
                                 headers={"Content-Type": "application/json"})
    start = time.monotonic()
    with urllib.request.urlopen(req, timeout=15) as r:
        return {"metodo": method, "caminho": path, "http": r.status,
                "resposta": json.load(r), "duracao_ms": round((time.monotonic()-start)*1000, 2)}

def conferir(nome, resposta, condicao):
    resposta.update(caso=nome, passou=bool(condicao(resposta["resposta"])))
    resultados["casos"].append(resposta)
    print(json.dumps(resposta, ensure_ascii=False), flush=True)
    assert resposta["passou"], nome
    return resposta["resposta"]

def aguardar(acao, processo=None, limite=60):
    end = time.monotonic()+limite
    while time.monotonic() < end:
        if processo and processo.poll() is not None:
            raise RuntimeError("Processo encerrou")
        try:
            valor = acao()
            if valor:
                return valor
        except (OSError, ValueError):
            pass
        time.sleep(0.03)
    raise AssertionError("Condição não ocorreu dentro do limite")

def parar(p):
    if p is not None and p.poll() is None:
        p.terminate()
        try:
            p.wait(10)
        except subprocess.TimeoutExpired:
            p.kill()
            p.wait()

def executar(modo):
    cp, np = porta(), porta()
    core_url, consumidor_url = "http://127.0.0.1:"+str(cp), "http://127.0.0.1:"+str(np)
    core = consumidor = None
    log_path = OUT / (modo+"-core.txt")
    with log_path.open("w") as log, (OUT / (modo+"-consumidor.txt")).open("w") as log_consumidor:
        def iniciar_consumidor():
            p = subprocess.Popen([sys.executable, str(OUT/"consumidor.py"), "--port", str(np)],
                                 stdout=log_consumidor, stderr=subprocess.STDOUT)
            aguardar(lambda: http(consumidor_url, "/estado"), p)
            return p
        try:
            consumidor = iniciar_consumidor()
            env = dict(os.environ, SERVER_PORT=str(cp), NOTIFICACOES_HABILITADAS="true",
                       NOTIFICACOES_MODO=modo, NOTIFICACOES_URL=consumidor_url,
                       NOTIFICACOES_FILA_CAPACIDADE="1", SPRING_DATASOURCE_URL="jdbc:h2:mem:d4lab",
                       SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.h2.Driver", SPRING_DATASOURCE_USERNAME="sa",
                       SPRING_DATASOURCE_PASSWORD="", SPRING_JPA_HIBERNATE_DDL_AUTO="create-drop")
            core = subprocess.Popen(["java", "-jar", "target/TDD-0.0.1-SNAPSHOT.jar"],cwd=ROOT,
                                    env=env, stdout=log, stderr=subprocess.STDOUT)
            aluno = aguardar(lambda: http(core_url, "/alunos", "POST", {"media": 8}), core)["resposta"]
            base = "/alunos/"+str(aluno["id"])
            saldo = 3
            conferir(modo+" sucesso", http(core_url, base+"/concluir-curso", "POST"), lambda x:x["cursosLiberados"]==saldo)
            aguardar(lambda: http(consumidor_url,"/estado")["resposta"]["finalizados"]==1)
            conferir(modo+" consumidor recebeu contrato", http(consumidor_url,"/estado"),
                     lambda x:x["recebidos"][0]["alunoId"]==aluno["id"] and x["recebidos"][0]["cursosLiberados"]==3 and bool(x["recebidos"][0]["eventoId"]))
            # Gate comprova a diferença sem depender somente de tempos.
            http(consumidor_url, "/controle?bloquear=1")
            saldo += 3
            with concurrent.futures.ThreadPoolExecutor() as pool:
                chamada = pool.submit(http, core_url, base+"/concluir-curso", "POST")
                aguardar(lambda:len(http(consumidor_url,"/estado")["resposta"]["recebidos"])==2)
                if modo == "sincrono":
                    assert not chamada.done(), "REST síncrono deveria aguardar consumidor"
                    http(consumidor_url, "/controle?liberar=1")
                resposta = chamada.result(5)
                conferir(modo+" gate consumidor", resposta, lambda x:x["cursosLiberados"]==saldo)
                if modo == "assincrono":
                    conferir("assíncrono respondeu antes do consumidor terminar", http(consumidor_url,"/estado"),lambda x:x["finalizados"]==1)
                    http(consumidor_url, "/controle?liberar=1")
            aguardar(lambda:http(consumidor_url,"/estado")["resposta"]["finalizados"]==2)
            if modo == "assincrono":
                http(consumidor_url,"/controle?bloquear=1")
                saldo += 3
                conferir("saturação primeiro evento", http(core_url,base+"/concluir-curso","POST"),lambda x:x["cursosLiberados"]==saldo)
                aguardar(lambda:len(http(consumidor_url,"/estado")["resposta"]["recebidos"])==3)
                saldo += 3
                conferir("saturação evento enfileirado", http(core_url,base+"/concluir-curso","POST"),lambda x:x["cursosLiberados"]==saldo)
                saldo += 3
                conferir("saturação evento descartado sem falha Core", http(core_url,base+"/concluir-curso","POST"),lambda x:x["cursosLiberados"]==saldo)
                assert "D4 DESCARTADA" in log_path.read_text()
                http(consumidor_url,"/controle?liberar=1")
                aguardar(lambda:http(consumidor_url,"/estado")["resposta"]["finalizados"]==4)
                conferir("saturação somente dois dos três eventos recebidos",http(consumidor_url,"/estado"),lambda x:len(x["recebidos"])==4)
            parar(consumidor)
            falhas_antes=log_path.read_text().count("D4 FALHA")
            saldo += 3
            conferir(modo+" consumidor parado",http(core_url,base+"/concluir-curso","POST"),lambda x:x["cursosLiberados"]==saldo)
            aguardar(lambda:log_path.read_text().count("D4 FALHA")>falhas_antes)
            conferir(modo+" leitura persistida por outro endpoint",http(core_url,base+"/verificar-premium","POST"),lambda x:x["cursosLiberados"]==saldo)
            consumidor = iniciar_consumidor()
            conferir(modo+" consumidor reiniciado sem replay automático", http(consumidor_url,"/estado"),lambda x:x["recebidos"]==[])
            saldo += 3
            conferir(modo+" novo evento após recuperação",http(core_url,base+"/concluir-curso","POST"),lambda x:x["cursosLiberados"]==saldo)
            aguardar(lambda:http(consumidor_url,"/estado")["resposta"]["finalizados"]==1)
            conferir(modo+" recuperação recebe somente evento novo",http(consumidor_url,"/estado"),lambda x:len(x["recebidos"])==1 and x["recebidos"][0]["cursosLiberados"]==saldo)
            http(consumidor_url,"/controle?atraso=3")
            falhas_antes=log_path.read_text().count("D4 FALHA")
            saldo += 3
            conferir(modo+" consumidor lento",http(core_url,base+"/concluir-curso","POST"),lambda x:x["cursosLiberados"]==saldo)
            aguardar(lambda:log_path.read_text().count("D4 FALHA")>falhas_antes)
            conferir(modo+" persistência mesmo após timeout",http(core_url,base+"/verificar-premium","POST"),lambda x:x["cursosLiberados"]==saldo)
        finally:
            parar(core)
            parar(consumidor)

def main():
    try:
        executar("sincrono")
        executar("assincrono")
        resultados["sucesso"] = True
    except Exception as ex:
        resultados.update(sucesso=False,erro=repr(ex))
        raise
    finally:
        (OUT/"resultados-http.json").write_text(json.dumps(resultados,ensure_ascii=False,indent=2)+"\n")

if __name__ == "__main__":
    main()
