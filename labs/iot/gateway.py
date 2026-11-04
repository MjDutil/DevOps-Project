"""Gateway MQTT 3.1.1. ACK só após aceitação/duplicata HTTP. Sem outbox própria."""
import argparse
import datetime as dt
import json
import time
import urllib.error
import urllib.request
from pathlib import Path
import paho.mqtt.client as mqtt
from normalizacao import normalizar, EventoInvalido


def registrar(tipo, **campos):
    print(json.dumps({"tipo": tipo, **campos}, ensure_ascii=False), flush=True)


def enviar_http(url, token, evento):
    req = urllib.request.Request(url.rstrip("/") + "/iot/eventos", data=json.dumps(evento).encode(),
                                 headers={"Content-Type": "application/json", "Authorization": "Bearer " + token}, method="POST")
    with urllib.request.urlopen(req, timeout=2) as response:
        result = json.load(response)
        if response.status != 200 or not isinstance(result, dict) or result.get("status") not in ("recebido", "duplicado") :
            raise ValueError("Core não confirmou o mesmo evento")
        confirmado = result.get("evento")
        if not isinstance(confirmado, dict) or set(confirmado) != set(evento):
            raise ValueError("Contrato da resposta inválido")
        for campo in evento:
            if campo == "ocorridoEm":
                if not isinstance(confirmado[campo], str):
                    raise ValueError("Instante não confirmado")
                if dt.datetime.fromisoformat(confirmado[campo].replace("Z", "+00:00")) != dt.datetime.fromisoformat(evento[campo].replace("Z", "+00:00")):
                    raise ValueError("Instante não confirmado")
            elif confirmado[campo] != evento[campo]:
                raise ValueError("Core confirmou outro evento")
        return result["status"]


class Encaminhador:
    def __init__(self, url, token, enviar=enviar_http):
        self.url, self.token, self.enviar = url, token, enviar

    def tratar(self, client, message):
        if message.qos != 1:
            registrar("REJEITADO_SEM_ACK", motivo="qos-diferente-de-1")
            return "parar"
        try:
            evento = normalizar(message.topic, message.payload)
        except EventoInvalido:
            registrar("REJEITADO_SEM_ACK", motivo="payload-invalido")
            return "parar"
        try:
            status = self.enviar(self.url, self.token, evento)
            if status not in ("recebido", "duplicado"):
                raise ValueError("Resposta não confirma aceitação")
        except urllib.error.HTTPError as ex:
            registrar("HTTP_FALHOU_SEM_ACK", evento=evento["eventId"], status=ex.code)
            return "reconectar" if ex.code >= 500 or ex.code in (408, 429) else "parar"
        except (OSError, ValueError):
            registrar("HTTP_FALHOU_SEM_ACK", evento=evento["eventId"], motivo="rede-timeout-ou-resposta-invalida")
            return "reconectar"
        registrar("HTTP_CONFIRMADO", evento=evento["eventId"], status_http=status, mid=message.mid)
        rc = client.ack(message.mid, message.qos)
        if rc != mqtt.MQTT_ERR_SUCCESS:
            registrar("ACK_FALHOU", evento=evento["eventId"])
            return "reconectar"
        registrar("ACK_ENFILEIRADO", evento=evento["eventId"], status_http=status,
                  duplicado_mqtt=bool(message.dup), mid=message.mid)
        return "continuar"


def executar(args, credenciais):
    encaminhador = Encaminhador(args.core_url, credenciais["core_token"])
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=args.client_id,
                         clean_session=False, protocol=mqtt.MQTTv311, manual_ack=True)
    client.username_pw_set(credenciais["gateway_username"], credenciais["gateway_password"])
    client.max_inflight_messages_set(1)
    conectado = False
    acao = "continuar"

    def on_connect(c, userdata, flags, reason, properties):
        nonlocal conectado, acao
        if reason.is_failure:
            registrar("CONEXAO_RECUSADA", codigo=str(reason))
            acao = "parar"
            return
        conectado = True
        registrar("CONECTADO", sessao_presente=flags.session_present, client_id=args.client_id)
        c.subscribe("b1/iot/dispositivos/+/eventos", qos=1)

    def on_subscribe(c, userdata, mid, reasons, properties):
        nonlocal acao
        if any(reason.is_failure or reason.value != 1 for reason in reasons):
            registrar("ASSINATURA_RECUSADA")
            acao = "parar"
        else:
            registrar("ASSINATURA_CONFIRMADA", client_id=args.client_id)

    def on_message(c, userdata, message):
        nonlocal acao
        acao = encaminhador.tratar(c, message)

    client.on_connect, client.on_subscribe, client.on_message = on_connect, on_subscribe, on_message
    tentativas = 0
    try:
        while True:
            conectado, acao = False, "continuar"
            try:
                client.connect(args.host, args.port, keepalive=30)
                while acao == "continuar":
                    rc = client.loop(timeout=0.2)
                    if rc != mqtt.MQTT_ERR_SUCCESS:
                        if acao != "parar":
                            acao = "reconectar"
                        break
                # Não envia ACK pendente em falhas: não houve ack() nesse caminho.
                client.disconnect()
                client.loop(timeout=0.1)
                if acao == "parar":
                    registrar("INTERVENCAO_NECESSARIA", motivo="payload-permanente-ou-configuracao; sessao pode conter mensagem sem ACK")
                    return 2
            except OSError:
                registrar("BROKER_INDISPONIVEL")
            tentativas += 1
            espera = min(0.5 * 2 ** min(tentativas - 1, 4), 5)
            registrar("RECONEXAO_AGENDADA", espera_segundos=espera)
            time.sleep(espera)
    except KeyboardInterrupt:
        registrar("ENCERRANDO")
        return 0
    finally:
        client.disconnect()


def argumentos():
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=18883)
    parser.add_argument("--client-id", default="b1-gateway-persistente")
    parser.add_argument("--core-url", default="http://127.0.0.1:8080")
    parser.add_argument("--credenciais", type=Path, default=Path(__file__).parent/".runtime/credenciais.json")
    return parser.parse_args()

if __name__ == "__main__":
    args = argumentos()
    raise SystemExit(executar(args, json.loads(args.credenciais.read_text())))
