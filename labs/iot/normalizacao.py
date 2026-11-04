"""Contrato do dispositivo -> contrato HTTP do Core, sem dependência de MQTT."""
import datetime as dt
import json
import re
import uuid

TIPOS = {"presenca": "PRESENCA", "inicio_atividade": "INICIO_ATIVIDADE", "conclusao_atividade": "CONCLUSAO_ATIVIDADE"}
CAMPOS = {"event_id", "dispositivo_id", "tipo", "ocorrido_em", "versao"}
MAX_PAYLOAD = 4096

class EventoInvalido(ValueError):
    pass


def normalizar(topic, payload):
    try:
        if not isinstance(payload, bytes) or len(payload) > MAX_PAYLOAD:
            raise ValueError("Tamanho/formato de payload inválido")
        bruto = json.loads(payload.decode("utf-8"))
        if not isinstance(bruto, dict) or set(bruto) != CAMPOS:
            raise ValueError("Campos inválidos")
        dispositivo = bruto["dispositivo_id"]
        if not isinstance(dispositivo, str) or not re.fullmatch(r"[A-Za-z0-9_-]{1,64}", dispositivo):
            raise ValueError("Dispositivo inválido")
        if topic != "b1/iot/dispositivos/" + dispositivo + "/eventos":
            raise ValueError("Tópico incompatível com dispositivo")
        if type(bruto["versao"]) is not int or bruto["versao"] != 1:
            raise ValueError("Versão inválida")
        if not isinstance(bruto["event_id"], str) or not isinstance(bruto["ocorrido_em"], str):
            raise ValueError("Identificador/instante inválidos")
        evento_id = str(uuid.UUID(bruto["event_id"]))
        instante = dt.datetime.fromisoformat(bruto["ocorrido_em"].replace("Z", "+00:00"))
        if instante.tzinfo is None or instante.utcoffset() is None:
            raise ValueError("Instante deve ter fuso horário")
        instante = instante.astimezone(dt.timezone.utc).isoformat().replace("+00:00", "Z")
        tipo = TIPOS[bruto["tipo"]]
        return {"eventId": evento_id, "dispositivoId": dispositivo, "tipo": tipo,
                "ocorridoEm": instante, "versao": 1}
    except (ValueError, TypeError, KeyError, AttributeError, OverflowError) as ex:
        raise EventoInvalido("Mensagem não atende ao contrato IoT") from ex
