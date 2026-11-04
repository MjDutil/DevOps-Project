"""Consumidor HTTP demonstrativo, somente loopback; controles exclusivos do laboratório."""
import argparse
import json
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse, parse_qs

lock = threading.Lock()
liberar = threading.Event()
liberar.set()
estado = {"recebidos": [], "finalizados": 0, "atraso": 0.0}

class Handler(BaseHTTPRequestHandler):
    def responder(self, value):
        data = json.dumps(value).encode()
        try:
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
        except (BrokenPipeError, ConnectionResetError):
            pass

    def do_GET(self):
        path = urlparse(self.path)
        query = parse_qs(path.query)
        if path.path == "/controle":
            if "bloquear" in query:
                liberar.clear()
            if "liberar" in query:
                liberar.set()
            if "atraso" in query:
                estado["atraso"] = float(query["atraso"][0])
        with lock:
            self.responder(estado)

    def log_message(self, *args):
        pass  # a saída registra os eventos, sem polling de controle

    def corpo(self):
        if self.headers.get("Transfer-Encoding", "").lower() != "chunked":
            return self.rfile.read(int(self.headers["Content-Length"]))
        chunks = []
        while True:
            tamanho = int(self.rfile.readline().split(b";", 1)[0].strip(), 16)
            if tamanho == 0:
                while self.rfile.readline() not in (b"\r\n", b"\n", b""):
                    pass
                return b"".join(chunks)
            chunks.append(self.rfile.read(tamanho))
            assert self.rfile.read(2) == b"\r\n"

    def do_POST(self):
        if self.path != "/notificacoes":
            self.send_error(404)
            return
        evento = json.loads(self.corpo())
        with lock:
            estado["recebidos"].append(evento)
        print(json.dumps({"recebido": evento}, ensure_ascii=False), flush=True)
        liberar.wait(8)
        time.sleep(estado["atraso"])
        with lock:
            estado["finalizados"] += 1
        self.responder({"status": "recebido"})

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=8091)
    args = parser.parse_args()
    ThreadingHTTPServer(("127.0.0.1", args.port), Handler).serve_forever()
