"""Publica um evento QoS 1 e aguarda PUBACK, sem prometer aceitação pelo Core."""
import argparse
import datetime as dt
import json
from pathlib import Path
import time
import uuid
import paho.mqtt.client as mqtt
from normalizacao import normalizar


def publicar(host, port, usuario, senha, topic, payload, timeout=8):
    client_id = "b1-simulador-"+uuid.uuid4().hex[:12]
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=client_id, protocol=mqtt.MQTTv311)
    client.username_pw_set(usuario, senha)
    resultado = {"conectado": False, "recusado": False, "publicado": False}
    def conectado(c, userdata, flags, reason, properties):
        resultado["conectado"] = not reason.is_failure
        resultado["recusado"] = reason.is_failure
    def publicado(c, userdata, mid, reason, properties):
        resultado["publicado"] = not reason.is_failure
    client.on_connect, client.on_publish = conectado, publicado
    try:
        client.connect(host, port, keepalive=30)
        fim = time.monotonic()+timeout
        while not resultado["conectado"] and not resultado["recusado"] and time.monotonic()<fim:
            if client.loop(0.1) != mqtt.MQTT_ERR_SUCCESS:
                break
        if not resultado["conectado"]:
            raise ConnectionError("Conexão MQTT não aceita")
        info = client.publish(topic, payload, qos=1, retain=False)
        if info.rc != mqtt.MQTT_ERR_SUCCESS:
            raise ConnectionError("Publicação não aceita pelo cliente MQTT")
        while not resultado["publicado"] and time.monotonic()<fim:
            if client.loop(0.1) != mqtt.MQTT_ERR_SUCCESS:
                break
        if not resultado["publicado"]:
            raise TimeoutError("PUBACK não observado")
        return {"puback_observado": True, "qos": 1, "retain": False, "client_id": client_id}
    finally:
        client.disconnect()
        client.loop(0.1)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=18883)
    parser.add_argument("--credenciais", type=Path, default=Path(__file__).parent/".runtime/credenciais.json")
    parser.add_argument("--evento", type=Path, help="Arquivo JSON para publicar/repetir o mesmo event_id")
    parser.add_argument("--event-id", default=None)
    args = parser.parse_args()
    cred = json.loads(args.credenciais.read_text())
    evento = json.loads(args.evento.read_text()) if args.evento else {
        "event_id": args.event_id or str(uuid.uuid4()), "dispositivo_id": "dispositivo-01",
        "tipo": "presenca", "ocorrido_em": dt.datetime.now(dt.timezone.utc).isoformat(), "versao": 1}
    topic = "b1/iot/dispositivos/"+evento["dispositivo_id"]+"/eventos"
    payload = json.dumps(evento).encode()
    normalizar(topic, payload)
    try:
        print(json.dumps({"event_id":evento["event_id"], **publicar(args.host,args.port,cred["device_username"],cred["device_password"],topic,payload)}))
    except (OSError, ValueError) as ex:
        print(json.dumps({"status":"falha", "tipo":type(ex).__name__}))
        raise SystemExit(1)
