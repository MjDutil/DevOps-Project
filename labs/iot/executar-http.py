"""Integração HTTP real com Core; independe do broker e NÃO comprova MQTT."""
import datetime as dt
import json
import os
from pathlib import Path
import secrets
import socket
import subprocess
import time
import urllib.error
import urllib.request
import uuid
from normalizacao import normalizar
from gateway import enviar_http

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/"docs/evidencias/desafio-5"

def http(url,path,method="POST",body=None,token=None):
    headers={"Content-Type":"application/json"}
    if token:headers["Authorization"]="Bearer "+token
    req=urllib.request.Request(url+path,data=None if body is None else json.dumps(body).encode(),method=method,headers=headers)
    start=time.monotonic()
    try:r=urllib.request.urlopen(req,timeout=10)
    except urllib.error.HTTPError as ex:r=ex
    with r:
        return {"metodo":method,"caminho":path,"http":r.code,"resposta":json.load(r),"duracao_ms":round((time.monotonic()-start)*1000,2)}

def main():
    with socket.socket() as s:s.bind(("127.0.0.1",0));port=s.getsockname()[1]
    token=secrets.token_urlsafe(32)
    env=dict(os.environ,SERVER_PORT=str(port),SPRING_PROFILES_ACTIVE="iot-lab",IOT_TOKEN=token,IOT_CAPACIDADE="2",
             SPRING_DATASOURCE_URL="jdbc:h2:mem:d5http",SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.h2.Driver",
             SPRING_DATASOURCE_USERNAME="sa",SPRING_DATASOURCE_PASSWORD="",SPRING_JPA_HIBERNATE_DDL_AUTO="create-drop",NOTIFICACOES_HABILITADAS="false")
    url="http://127.0.0.1:"+str(port)
    result={"data":dt.datetime.now().astimezone().isoformat(),"escopo":"HTTP real sem broker MQTT","casos":[]}
    def check(nome,response,status,pred=lambda x:True):
        response.update(caso=nome,passou=response["http"]==status and bool(pred(response["resposta"])))
        result["casos"].append(response);print(json.dumps(response,ensure_ascii=False),flush=True)
        assert response["passou"],nome
        return response["resposta"]
    with (OUT/"http-core.txt").open("w") as log:
        p=subprocess.Popen(["java","-jar","target/TDD-0.0.1-SNAPSHOT.jar"],cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT)
        try:
            end=time.monotonic()+90
            while True:
                if p.poll() is not None:raise RuntimeError("Core encerrou")
                try:aluno=http(url,"/alunos",body={"media":8});break
                except OSError:
                    if time.monotonic()>end:raise
                    time.sleep(0.2)
            aluno=check("criar aluno antes de IoT",aluno,200,lambda x:x["cursosLiberados"]==0)
            bruto={"event_id":str(uuid.uuid4()),"dispositivo_id":"dispositivo-01","tipo":"presenca","ocorrido_em":"2026-11-04T09:00:00.120000-03:00","versao":1}
            evento=normalizar("b1/iot/dispositivos/dispositivo-01/eventos",json.dumps(bruto).encode())
            check("token ausente",http(url,"/iot/eventos",body=evento),401)
            check("token incorreto",http(url,"/iot/eventos",body=evento,token="incorreto"),401)
            check("payload inválido",http(url,"/iot/eventos",body={},token=token),400)
            check("aceitar evento normalizado",http(url,"/iot/eventos",body=evento,token=token),200,lambda x:x["status"]=="recebido" and x["evento"]["tipo"]=="PRESENCA")
            check("detectar duplicata",http(url,"/iot/eventos",body=evento,token=token),200,lambda x:x["status"]=="duplicado")
            assert enviar_http(url,token,evento)=="duplicado"
            result["gateway_http_real_confirmou_duplicata"]=True
            check("mesmo ID outro conteúdo",http(url,"/iot/eventos",body=dict(evento,tipo="INICIO_ATIVIDADE"),token=token),409)
            check("IoT não concede recompensa",http(url,"/alunos/"+str(aluno["id"])+"/verificar-premium"),200,lambda x:x["cursosLiberados"]==0 and x["moedas"]==0)
            for n in range(2):
                check("janela novo evento "+str(n+1),http(url,"/iot/eventos",body=dict(evento,eventId=str(uuid.uuid4())),token=token),200,lambda x:x["status"]=="recebido")
            check("evicção permite nova aceitação",http(url,"/iot/eventos",body=evento,token=token),200,lambda x:x["status"]=="recebido")
            check("conclusão anterior preservada",http(url,"/alunos/"+str(aluno["id"])+"/concluir-curso"),200,lambda x:x["cursosLiberados"]==3)
            result["sucesso"]=True
        except Exception as ex:
            result.update(sucesso=False,erro=repr(ex));raise
        finally:
            p.terminate()
            try:p.wait(10)
            except subprocess.TimeoutExpired:p.kill();p.wait()
            (OUT/"resultados-http.json").write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n")

if __name__=="__main__":main()
