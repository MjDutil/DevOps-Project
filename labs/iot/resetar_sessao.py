"""Reset explícito de uma sessão de laboratório: descarta assinaturas e pendências.
Pare o gateway correspondente antes. Nunca é executado automaticamente pelo gateway.
"""
import argparse
import json
from pathlib import Path
import time
import paho.mqtt.client as mqtt

if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--confirmar-descarte",action="store_true",required=True)
    parser.add_argument("--client-id",default="b1-gateway-persistente")
    parser.add_argument("--port",type=int,default=18883)
    parser.add_argument("--credenciais",type=Path,default=Path(__file__).parent/".runtime/credenciais.json")
    args=parser.parse_args()
    cred=json.loads(args.credenciais.read_text())
    client=mqtt.Client(mqtt.CallbackAPIVersion.VERSION2,client_id=args.client_id,clean_session=True,protocol=mqtt.MQTTv311)
    client.username_pw_set(cred["gateway_username"],cred["gateway_password"])
    resultado={"aceito":False}
    def conectado(c,userdata,flags,reason,props):resultado["aceito"]=not reason.is_failure
    client.on_connect=conectado
    try:
        client.connect("127.0.0.1",args.port,30)
        fim=time.monotonic()+5
        while not resultado["aceito"] and time.monotonic()<fim:
            if client.loop(0.1)!=mqtt.MQTT_ERR_SUCCESS:break
        if not resultado["aceito"]:raise SystemExit("Reset não confirmado")
        print("Sessão de laboratório resetada; assinaturas e mensagens pendentes foram descartadas.")
    finally:client.disconnect();client.loop(0.1)
