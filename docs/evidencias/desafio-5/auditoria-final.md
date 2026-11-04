# Auditoria final D5 — potencial de atendimento ao enunciado

Data: 2026-11-04. Escopo: arquivos atuais, execução real e cinco critérios oficiais. Sem atribuir nota definitiva. Enunciado e hash estão no [registro de evidências](README.md).

| Critério oficial | Situação | Evidências e conclusão |
|---|---|---|
| RF/RNF e até três ASRs — 0,25 | ATENDIDO | [ADR-005](../../adr/ADR-005.md#rnfs-até-três-asrs-e-rpcs) e [matriz](../../arquitetura/README.md#2-matriz-cumulativa-de-asrs): RF06, seis RNFs analisados e três ASRs com efeito estrutural justificado. |
| RPCs, alternativas e trade-offs — 0,20 | ATENDIDO | [ADR-005](../../adr/ADR-005.md#alternativas-e-trade-offs): dispositivos limitados, conectividade, custo de broker; REST direto × MQTT/gateway × mensageria interna. Sem RabbitMQ/Kafka adicional. |
| ADR e justificativa — 0,20 | ATENDIDO | [ADR-005](../../adr/ADR-005.md), [gateway](../../../labs/iot/gateway.py), [normalizador](../../../labs/iot/normalizacao.py) e [registro Core](../../../src/main/java/org/example/tdd/iot/RegistroEventosIot.java): decisão, consequências e limites correspondem ao código. |
| C4 coerente — 0,15 | ATENDIDO | [Container/fluxo](../../arquitetura/diagramas.md#c4-container--desafio-5), [SVG Container](c4-container.svg), [SVG sequência](sequencia.svg): dispositivo, broker, gateway, Core e protocolos; executor D4 interno e distinto do MQTT. Fontes renderizados e links conferidos. |
| Mini-lab, evidências e Git — 0,20 | PARCIAL somente no Git | [15 cenários MQTT reais](resultados-mqtt.json), [auditoria de ACK/PUBACK](auditoria-mqtt.txt), [96 testes Maven](mvn-clean-verify.txt), [14 testes Python](testes-python.txt), [12 checks HTTP](resultados-http.json), regressões [D3](regressao-d3/resultados-lab.json)/[D4](regressao-d4/resultados-http.json). Mini-lab atendido; arquivos D5 ainda não commitados/publicados por restrição expressa do usuário. |

**Conclusão:** D5 tecnicamente pronto para entrega no escopo do mini-lab acadêmico; falta consolidar/publicar o Git mediante autorização. Não exige reescrever gamificação nem adicionar mensageria interna.

Cenários finais: publicação/normalização/aceitação, duplicata, presença/início/conclusão sem recompensa, autenticação, ACL, sessão persistente, Core fora do ar e reentrega DUP, broker fora do ar/recuperado e payload inválido sem ACK. Sete ACKs foram correlacionados a confirmação HTTP anterior no mesmo processo e PUBACK no broker; payload inválido final não foi confirmado.

Limites: deduplicação volátil/FIFO, estado Paho não durável, nenhuma garantia de exatamente uma vez, intervenção em mensagem inválida, sem TLS/hardware/carga/HA ou crash de disco. O cenário acadêmico não equivale a garantia de entrega em produção. [Resultados completos, tentativas e reprodução](README.md).
