import datetime as dt
import io
import json
from pathlib import Path
import socket
import sys
import threading
from types import SimpleNamespace
import unittest
import urllib.error
import uuid
from contextlib import redirect_stdout
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from normalizacao import normalizar, EventoInvalido
from gateway import Encaminhador, enviar_http

TOPIC = "b1/iot/dispositivos/dispositivo-01/eventos"

def bruto():
    return {"event_id":str(uuid.uuid4()),"dispositivo_id":"dispositivo-01","tipo":"presenca",
            "ocorrido_em":"2026-11-04T09:00:00.120000-03:00","versao":1}

def mensagem(payload=None, qos=1):
    return SimpleNamespace(topic=TOPIC,payload=json.dumps(payload or bruto()).encode(),qos=qos,mid=3,dup=False)

class AckSpy:
    def __init__(self):self.acks=[]
    def ack(self,mid,qos):self.acks.append((mid,qos));return 0

class NormalizacaoTest(unittest.TestCase):
    def test_normaliza_nomes_tipo_uuid_e_fuso(self):
        r=normalizar(TOPIC,json.dumps(bruto()).encode())
        self.assertEqual(r["tipo"],"PRESENCA")
        self.assertEqual(r["ocorridoEm"],"2026-11-04T12:00:00.120000Z")
        self.assertEqual(set(r),{"eventId","dispositivoId","tipo","ocorridoEm","versao"})
    def test_rejeita_json_malformado_e_utf8_invalido(self):
        for payload in [b"{",b"\xff",b"[]"]:
            with self.assertRaises(EventoInvalido):normalizar(TOPIC,payload)
    def test_rejeita_payload_excessivo(self):
        with self.assertRaises(EventoInvalido):normalizar(TOPIC,b" "*4097)
    def test_rejeita_topico_de_outro_dispositivo(self):
        with self.assertRaises(EventoInvalido):normalizar(TOPIC.replace("01","02"),json.dumps(bruto()).encode())
    def test_rejeita_campos_tipo_versao_instante_e_identificador(self):
        for field,value in [("event_id","invalido"),("dispositivo_id","../x"),("tipo","pontos"),("versao",True),("versao",2),("ocorrido_em","2026-11-04T12:00:00")]:
            r=bruto();r[field]=value
            with self.subTest(field=field,value=value),self.assertRaises(EventoInvalido):normalizar(TOPIC,json.dumps(r).encode())
        r=bruto();r["extra"]=1
        with self.assertRaises(EventoInvalido):normalizar(TOPIC,json.dumps(r).encode())

class EncaminhadorTest(unittest.TestCase):
    def setUp(self):self.client=AckSpy()
    def tratar(self,enviar,msg=None):
        with redirect_stdout(io.StringIO()):
            return Encaminhador("http://local","token-apenas-teste",enviar).tratar(self.client,msg or mensagem())
    def test_ack_depois_da_aceitacao_http(self):
        def enviar(*args):
            self.assertEqual(self.client.acks,[])
            return "recebido"
        self.assertEqual(self.tratar(enviar),"continuar");self.assertEqual(self.client.acks,[(3,1)])
    def test_ack_para_duplicata_http(self):
        self.assertEqual(self.tratar(lambda *args:"duplicado"),"continuar");self.assertEqual(len(self.client.acks),1)
    def test_sem_ack_quando_core_indisponivel(self):
        def enviar(*args):raise urllib.error.URLError("conexao recusada")
        self.assertEqual(self.tratar(enviar),"reconectar");self.assertEqual(self.client.acks,[])
    def test_timeout_e_resposta_inesperada_nao_confirmam(self):
        def timeout(*args):raise TimeoutError()
        self.assertEqual(self.tratar(timeout),"reconectar")
        self.assertEqual(self.tratar(lambda *args:"desconhecido"),"reconectar");self.assertEqual(self.client.acks,[])
    def test_http_transitorio_reconecta_permanente_exige_intervencao(self):
        for status,acao in [(503,"reconectar"),(429,"reconectar"),(401,"parar"),(400,"parar"),(409,"parar")]:
            def enviar(*args):raise urllib.error.HTTPError("http://local",status,"teste",{},None)
            self.assertEqual(self.tratar(enviar),acao)
        self.assertEqual(self.client.acks,[])
    def test_payload_invalido_nao_chama_http_nem_ack(self):
        def enviar(*args):self.fail("não deveria encaminhar")
        msg=mensagem();msg.payload=b"{"
        self.assertEqual(self.tratar(enviar,msg),"parar");self.assertEqual(self.client.acks,[])
    def test_qos_incorreto_nao_e_aceito(self):
        self.assertEqual(self.tratar(lambda *args:"recebido",mensagem(qos=0)),"parar");self.assertEqual(self.client.acks,[])
    def test_falha_ao_enfileirar_ack_reconecta(self):
        self.client.ack=lambda *args:4
        self.assertEqual(self.tratar(lambda *args:"recebido"),"reconectar")

class HttpRealSemBrokerTest(unittest.TestCase):
    """Socket HTTP real, Core substituído por servidor de teste. Não é ensaio MQTT."""
    def test_http_real_valida_confirmacao_e_instante_equivalente(self):
        recebido={}
        class Handler(BaseHTTPRequestHandler):
            def do_POST(self):
                body=json.loads(self.rfile.read(int(self.headers["Content-Length"])))
                recebido.update(path=self.path,body=body,auth=self.headers.get("Authorization"))
                # Java pode serializar frações com precisão textual diferente.
                body["ocorridoEm"]=body["ocorridoEm"].replace(".120000Z",".120Z")
                data=json.dumps({"status":"recebido","evento":body}).encode()
                self.send_response(200);self.send_header("Content-Type","application/json");self.end_headers();self.wfile.write(data)
            def log_message(self,*args):pass
        server=ThreadingHTTPServer(("127.0.0.1",0),Handler)
        thread=threading.Thread(target=server.serve_forever,daemon=True);thread.start()
        try:
            evento=normalizar(TOPIC,json.dumps(bruto()).encode())
            status=enviar_http("http://127.0.0.1:"+str(server.server_port),"apenas-teste",evento)
            self.assertEqual(status,"recebido");self.assertEqual(recebido["path"],"/iot/eventos")
            self.assertEqual(recebido["auth"],"Bearer apenas-teste")
        finally:server.shutdown();server.server_close();thread.join(2)

if __name__=="__main__":unittest.main()
