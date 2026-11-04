"""Audita logs reais já coletados, sem iniciar broker ou substituir ensaio real."""
import datetime as dt
import json
from pathlib import Path
import re

OUT=Path(__file__).resolve().parents[2]/"docs/evidencias/desafio-5"

def instante(value):
    # Docker registra nanossegundos; Python 3.9 aceita precisão de microssegundos.
    value=re.sub(r"\.(\d{6})\d+Z$",r".\1+00:00",value)
    return dt.datetime.fromisoformat(value.replace("Z","+00:00"))

def main():
    resultado=json.loads((OUT/"resultados-mqtt.json").read_text())
    assert resultado["sucesso"] and resultado["status"]=="executado"
    assert all(c["passou"] for c in resultado["casos"])
    gateway=[json.loads(l) for l in (OUT/"mqtt-gateway.txt").read_text().splitlines() if l.startswith("{")]
    broker=(OUT/"mqtt-broker.txt").read_text().splitlines()
    core=(OUT/"mqtt-core.txt").read_text().splitlines()
    client=resultado["client_id"]
    acks={}
    enviados={}
    for line in broker:
        prefix=line.split("|",1)[1].strip().split()[0]
        found=re.search(r"Received PUBACK from "+re.escape(client)+r" \(Mid: (\d+),",line)
        if found:acks[int(found[1])]=instante(prefix)
        found=re.search(r"Sending PUBLISH to "+re.escape(client)+r" \(d(\d), q1, r0, m(\d+),",line)
        if found:enviados.setdefault(int(found[2]),[]).append((int(found[1]),instante(prefix)))
    confirmacoes=[row for row in gateway if row["tipo"]=="ACK_ENFILEIRADO"]
    for row in confirmacoes:
        candidatos=[instante(l.split()[0]) for l in core if "evento="+row["evento"] in l and "status="+row["status_http"] in l]
        assert candidatos and row["mid"] in acks
        confirmado=next((x for x in gateway if x["tipo"]=="HTTP_CONFIRMADO" and x.get("evento")==row["evento"] and x.get("mid")==row["mid"]),None)
        assert confirmado and gateway.index(confirmado)<gateway.index(row),"ACK sem confirmação HTTP anterior no gateway"
    invalido=max(enviados)
    assert invalido not in acks and any(row["tipo"]=="REJEITADO_SEM_ACK" for row in gateway)
    assert any(row["tipo"]=="CONECTADO" and row["sessao_presente"] for row in gateway)
    assert any("Denied PUBLISH" in l for l in broker)
    caso=next(c for c in resultado["casos"] if c["caso"].startswith("Core indisponível:"))
    falhas=[x for x in gateway if x["tipo"]=="HTTP_FALHOU_SEM_ACK" and x.get("evento")==caso["event_id"]]
    ack=next(x for x in confirmacoes if x["evento"]==caso["event_id"])
    assert falhas and ack["duplicado_mqtt"]
    delay=(acks[ack["mid"]]-enviados[ack["mid"]][0][1]).total_seconds()
    print("PASSOU:",len(resultado["casos"]),"cenários MQTT reais aprovados.")
    print("PASSOU:",len(confirmacoes),"ACKs com confirmação HTTP anterior no mesmo processo e PUBACK correlacionado no broker.")
    print("PASSOU: último payload inválido, mid",invalido,"sem PUBACK do gateway.")
    print("PASSOU: sessão persistente retomada, ACL negou publicação e reentrega DUP observada.")
    print("Core indisponível:",len(falhas),"falhas sem ACK; intervalo observado até PUBACK:",round(delay,3),"s. Não é SLA.")
    print("Limite: relógios host/VM não são usados para ordenar causalmente os dois sistemas; logs não provam atomicidade ou exatamente uma vez.")

if __name__=="__main__":main()
