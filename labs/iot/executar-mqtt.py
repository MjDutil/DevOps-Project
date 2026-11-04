"""Ensaio MQTT REAL. Requer Docker ativo e venv Paho. Nunca simula broker.
Na raiz, após mvn clean verify: /tmp/b1-d5-venv/bin/python labs/iot/executar-mqtt.py
"""
import datetime as dt
import json
import os
from pathlib import Path
import socket
import subprocess
import sys
import time
import uuid
import urllib.error
import urllib.request
from simulador import publicar
from normalizacao import normalizar

LAB=Path(__file__).resolve().parent
ROOT=LAB.parents[1]
OUT=ROOT/"docs/evidencias/desafio-5"

def porta():
    with socket.socket() as s:s.bind(("127.0.0.1",0));return s.getsockname()[1]

def aguardar(condicao,processo=None,segundos=75):
    fim=time.monotonic()+segundos
    while time.monotonic()<fim:
        if processo is not None and processo.poll() is not None:raise RuntimeError("Processo encerrou antes da condição")
        try:
            if condicao():return
        except (OSError,ValueError):pass
        time.sleep(0.1)
    raise TimeoutError("Condição não observada")

def parar(processo):
    if processo and processo.poll() is None:
        processo.terminate()
        try:processo.wait(10)
        except subprocess.TimeoutExpired:processo.kill();processo.wait()

def main():
    resultado={"data":dt.datetime.now().astimezone().isoformat(),"escopo":"MQTT real com Mosquitto","casos":[]}
    info=subprocess.run(["docker","info"],capture_output=True,text=True,timeout=15)
    if info.returncode:
        resultado.update(status="bloqueado",sucesso=False,motivo="Docker daemon indisponível; nenhum teste MQTT real executado")
        (OUT/"resultados-mqtt.json").write_text(json.dumps(resultado,ensure_ascii=False,indent=2)+"\n")
        print(resultado["motivo"],flush=True)
        return 2
    if not (LAB/".runtime/credenciais.json").exists():
        preparo=subprocess.run([sys.executable,str(LAB/"preparar_lab.py")],timeout=180)
        if preparo.returncode:
            resultado.update(status="falhou",sucesso=False,motivo="Preparação de credenciais falhou antes do MQTT")
            (OUT/"resultados-mqtt.json").write_text(json.dumps(resultado,ensure_ascii=False,indent=2)+"\n")
            return 1
    cred=json.loads((LAB/".runtime/credenciais.json").read_text())
    mqtt_port,core_port=porta(),porta()
    core_url="http://127.0.0.1:"+str(core_port)
    client_id="b1-gateway-e2e-"+str(os.getpid()) # estável durante os reinícios deste ensaio
    env=dict(os.environ,MQTT_LAB_PORT=str(mqtt_port))
    compose=["docker","compose","-p","b1-d5-"+str(os.getpid()),"-f",str(LAB/"compose.yml")]
    gateway=core=None
    broker_iniciado=False
    gateway_log=OUT/"mqtt-gateway.txt"
    # Cada execução usa logs novos para impedir falsos positivos de tentativas anteriores.
    for nome in ["mqtt-gateway.txt","mqtt-core.txt","mqtt-compose.txt","mqtt-broker.txt"]:
        (OUT/nome).write_text("")
    resultado.update(client_id=client_id,mqtt_port=mqtt_port,core_port=core_port,
                     protocolo="MQTT 3.1.1",qos=1,retain=False,clean_session=False,manual_ack=True)
    handles=[]
    def comando(*args):
        p=subprocess.run(compose+list(args),env=env,capture_output=True,text=True,timeout=90)
        with (OUT/"mqtt-compose.txt").open("a") as f:f.write(p.stdout+p.stderr)
        if p.returncode:raise RuntimeError("Falha Docker Compose; consulte mqtt-compose.txt")
        return p.stdout
    def registrar(caso,**detalhes):
        row={"caso":caso,"passou":True,**detalhes};resultado["casos"].append(row)
        print(json.dumps(row,ensure_ascii=False),flush=True)
    def logs_gateway():return gateway_log.read_text() if gateway_log.exists() else ""
    def ack(id):
        return any(json.loads(l).get("evento")==id and json.loads(l).get("tipo")=="ACK_ENFILEIRADO" for l in logs_gateway().splitlines() if l.startswith("{"))
    def iniciar_gateway():
        h=gateway_log.open("a");handles.append(h)
        p=subprocess.Popen([sys.executable,str(LAB/"gateway.py"),"--port",str(mqtt_port),"--client-id",client_id,"--core-url",core_url],stdout=h,stderr=subprocess.STDOUT,cwd=ROOT)
        return p
    def iniciar_core():
        h=(OUT/"mqtt-core.txt").open("a");handles.append(h)
        core_env=dict(os.environ,SPRING_PROFILES_ACTIVE="iot-lab",SERVER_PORT=str(core_port),IOT_TOKEN=cred["core_token"],
                      SPRING_DATASOURCE_URL="jdbc:h2:mem:d5mqtt",SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.h2.Driver",
                      SPRING_DATASOURCE_USERNAME="sa",SPRING_DATASOURCE_PASSWORD="",SPRING_JPA_HIBERNATE_DDL_AUTO="create-drop",NOTIFICACOES_HABILITADAS="false")
        p=subprocess.Popen(["java","-jar","target/TDD-0.0.1-SNAPSHOT.jar"],cwd=ROOT,env=core_env,stdout=h,stderr=subprocess.STDOUT)
        def pronto():
            req=urllib.request.Request(core_url+"/iot/eventos",data=b"{}",headers={"Content-Type":"application/json","Authorization":"Bearer "+cred["core_token"]},method="POST")
            try:
                with urllib.request.urlopen(req,timeout=2):return False
            except urllib.error.HTTPError as e:return e.code==400
        aguardar(pronto,p)
        return p
    def requisicao_core(path,body=None):
        req=urllib.request.Request(core_url+path,data=None if body is None else json.dumps(body).encode(),
                                   headers={"Content-Type":"application/json"},method="POST")
        with urllib.request.urlopen(req,timeout=5) as r:
            assert r.status==200
            return json.load(r)
    def criar_baseline():
        aluno=requisicao_core("/alunos",{"media":8})
        # Estado pré-existente positivo: IoT não pode creditar novamente ou apagar benefícios.
        for _ in range(4):
            requisicao_core("/alunos/"+str(aluno["id"])+"/concluir-curso")
        estado=requisicao_core("/alunos/"+str(aluno["id"])+"/verificar-premium")
        assert estado["cursosLiberados"]==12 and estado["moedas"]==3 and estado["voucherProjetosReais"] and estado["plano"]=="Premium"
        return aluno["id"],estado
    def evento(dispositivo="dispositivo-01"):
        return {"event_id":str(uuid.uuid4()),"dispositivo_id":dispositivo,"tipo":"presenca","ocorrido_em":dt.datetime.now(dt.timezone.utc).isoformat(),"versao":1}
    def enviar(e,topic=None,senha=None):
        return publicar("127.0.0.1",mqtt_port,cred["device_username"],senha or cred["device_password"],topic or "b1/iot/dispositivos/"+e["dispositivo_id"]+"/eventos",json.dumps(e).encode())
    try:
        broker_iniciado=True;comando("up","-d")
        def tcp():
            with socket.create_connection(("127.0.0.1",mqtt_port),timeout=1):return True
        aguardar(tcp)
        core=iniciar_core();aluno_id,baseline=criar_baseline();gateway=iniciar_gateway()
        aguardar(lambda:"ASSINATURA_CONFIRMADA" in logs_gateway(),gateway)
        e=evento();pub=enviar(e);aguardar(lambda:ack(e["event_id"]),gateway)
        registrar("publicação QoS1, normalização e aceitação HTTP",event_id=e["event_id"],publicacao=pub)
        enviar(e)
        aguardar(lambda:any('"status_http": "duplicado"' in l and e["event_id"] in l for l in logs_gateway().splitlines()),gateway)
        registrar("duplicata reconhecida pelo Core e ACK manual",event_id=e["event_id"])
        ids=[]
        for tipo in ["inicio_atividade","conclusao_atividade"]:
            extra=evento();extra["tipo"]=tipo;enviar(extra);aguardar(lambda:ack(extra["event_id"]),gateway);ids.append(extra["event_id"])
        registrar("eventos de início e conclusão de atividade encaminhados",event_ids=ids)
        depois=requisicao_core("/alunos/"+str(aluno_id)+"/verificar-premium")
        assert depois==baseline
        registrar("IoT e duplicatas não alteram cursos, moedas, voucher ou Premium",antes=baseline,depois=depois)
        try:enviar(evento(),senha="senha-incorreta-apenas-teste")
        except (OSError,ValueError):registrar("autenticação MQTT inválida recusada")
        else:raise AssertionError("Broker aceitou senha incorreta")
        try:
            publicar("127.0.0.1",mqtt_port,"","","b1/iot/dispositivos/dispositivo-01/eventos",b"{}")
        except (OSError,ValueError):registrar("cliente MQTT anônimo recusado")
        else:raise AssertionError("Broker aceitou cliente anônimo")
        negacoes_antes=comando("logs","--no-color","mosquitto").lower().count("denied publish")
        proibido=evento("dispositivo-02")
        # MQTT 3.1.1 pode confirmar o pacote e descartar a publicação negada por ACL.
        try:enviar(proibido)
        except (OSError,ValueError):pass
        def negado():return comando("logs","--no-color","mosquitto").lower().count("denied publish")>negacoes_antes
        aguardar(negado,segundos=10)
        assert not ack(proibido["event_id"])
        registrar("ACL bloqueou publicação em tópico de outro dispositivo",event_id=proibido["event_id"])
        parar(gateway)
        e=evento();enviar(e)
        recorte=len(logs_gateway())
        gateway=iniciar_gateway();aguardar(lambda:ack(e["event_id"]),gateway)
        assert '"sessao_presente": true' in logs_gateway()[recorte:]
        registrar("gateway desconectado e retomada da sessão persistente",event_id=e["event_id"])
        parar(core)
        e=evento();enviar(e)
        aguardar(lambda:any('"HTTP_FALHOU_SEM_ACK"' in l and e["event_id"] in l for l in logs_gateway().splitlines()),gateway)
        assert not ack(e["event_id"])
        core=iniciar_core();aguardar(lambda:ack(e["event_id"]),gateway)
        prova=[json.loads(l) for l in logs_gateway().splitlines() if l.startswith("{") and e["event_id"] in l]
        assert any(x.get("tipo")=="ACK_ENFILEIRADO" and x.get("duplicado_mqtt") for x in prova)
        registrar("Core indisponível: sem ACK, reconexão e reentrega observada",event_id=e["event_id"],duplicado_mqtt_observado=True)
        aluno_id,baseline=criar_baseline()
        comando("stop","mosquitto")
        try:enviar(evento())
        except (OSError,ValueError):registrar("broker indisponível: simulador informou falha sem prometer retenção local")
        else:raise AssertionError("Publicação não deveria funcionar com broker parado")
        antes=logs_gateway().count('"ASSINATURA_CONFIRMADA"')
        comando("start","mosquitto")
        aguardar(lambda:logs_gateway().count('"ASSINATURA_CONFIRMADA"')>antes,gateway)
        e=evento();enviar(e);aguardar(lambda:ack(e["event_id"]),gateway)
        registrar("broker recuperado e novo evento entregue",event_id=e["event_id"])
        publicar("127.0.0.1",mqtt_port,cred["device_username"],cred["device_password"],"b1/iot/dispositivos/dispositivo-01/eventos",b"{")
        aguardar(lambda:gateway.poll() is not None,segundos=10)
        assert gateway.returncode==2 and "REJEITADO_SEM_ACK" in logs_gateway()
        registrar("payload inválido sem ACK; gateway encerra exigindo intervenção")
        depois=requisicao_core("/alunos/"+str(aluno_id)+"/verificar-premium")
        assert depois==baseline
        registrar("estado de gamificação preservado após falhas MQTT",antes=baseline,depois=depois)
        conclusao=requisicao_core("/alunos/"+str(aluno_id)+"/concluir-curso")
        assert conclusao["cursosLiberados"]==baseline["cursosLiberados"]+3
        registrar("conclusão anterior funciona quando chamada explicitamente",resposta=conclusao)
        broker_prova=comando("logs","--no-color","mosquitto")
        assert "q1" in broker_prova and "Received PUBACK from "+client_id in broker_prova
        registrar("broker registrou tráfego QoS1 e PUBACK do gateway",client_id=client_id)
        resultado.update(status="executado",sucesso=True)
        return 0
    except Exception as ex:
        resultado.update(status="falhou",sucesso=False,erro=type(ex).__name__+": "+str(ex))
        raise
    finally:
        parar(gateway);parar(core)
        for h in handles:h.close()
        if broker_iniciado:
            try:
                broker_logs=comando("logs","--no-color","--timestamps","mosquitto")
                (OUT/"mqtt-broker.txt").write_text(broker_logs)
            finally:comando("down","-v") # remove somente volumes do projeto exclusivo deste ensaio
        (OUT/"resultados-mqtt.json").write_text(json.dumps(resultado,ensure_ascii=False,indent=2)+"\n")

if __name__=="__main__":raise SystemExit(main())
