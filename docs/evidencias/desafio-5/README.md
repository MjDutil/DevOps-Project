# Evidências D5 — IoT e MQTT

Data: 2026-11-04. Base: commit local D4 `59cb7d1`. **D5 validado tecnicamente no mini-lab real**, sem commit/push/merge nesta etapa. [Git anterior](git-antes.json), [preservação](preservacao.json) e [auditoria pelos cinco critérios](auditoria-final.md).

## Fonte e hipótese

Enunciado: `Exercicio_B1_Architecture_Lab_6_Desafios_Reavaliado.docx`, em Downloads. SHA-256 de `word/document.xml`: `9475a86aeb9e0d8c6d3d6e1b8c9c51cdd1a7b5e0c0433981b279c10f00898d05`. O mini-lab exige simulador publicando em broker MQTT, consumidor recebendo/transformando e encaminhamento para uso da aplicação.

Hipótese: MQTT 3.1.1/QoS 1 com sessão persistente e gateway desacopla dispositivos do Core. O gateway valida/normaliza e confirma a mensagem somente após aceitação HTTP ou duplicata idêntica. Eventos IoT não chamam conclusão de curso nem alteram recompensas.

[ADR-005](../../adr/ADR-005.md) registra RF06 → RNFs → três ASRs (eficiência, conectividade intermitente e segurança) → RPCs → REST direto × MQTT/gateway × mensageria interna adicional → decisão → C4 → evidência. Não se adicionou RabbitMQ/Kafka ou dependência Maven.

## Resultado real MQTT

**15 cenários aprovados**, usando Docker Desktop **29.8.0**, Mosquitto **2.0.22**, Paho **2.1.0**, JAR Spring/H2 e gateway em processos separados. [Saída nominal](execucao-mqtt.txt), [JSON](resultados-mqtt.json), [broker](mqtt-broker.txt), [gateway](mqtt-gateway.txt), [Core](mqtt-core.txt), [Compose/limpeza](mqtt-compose.txt).

| Cenário | Resultado efetivamente observado |
|---|---|
| Publicação e recepção | Simulador recebeu PUBACK QoS 1; gateway normalizou JSON; Core confirmou o mesmo evento. |
| Duplicata | Mesmo evento publicado novamente recebeu `duplicado` no Core e ACK manual. |
| Tipos de atividade | Presença, início e conclusão de atividade percorreram MQTT → gateway → HTTP. |
| Preservação inicial | Aluno já Premium: cursos 12, moedas 3, voucher verdadeiro e plano Premium antes/depois de IoT e duplicatas; comparação de respostas HTTP reais. |
| Autenticação | Senha incorreta e cliente anônimo recusados; logs Mosquitto registram CONNACK de recusa e `not authorised`. |
| ACL | Dispositivo-01 tentou publicar no tópico do dispositivo-02: `Denied PUBLISH`; evento não aceito no gateway/Core. PUBACK não foi usado sozinho como prova de autorização. |
| Sessão persistente | Gateway encerrado, evento publicado com ele offline e processo reiniciado com mesmo clientId: sessão presente e evento entregue. |
| Core indisponível | Mensagem ficou sem ACK após falhas HTTP; gateway desconectou/reconectou e recebeu reentrega com DUP. Após recuperar Core, aceitou e confirmou. |
| Broker indisponível | Broker parado por Compose: simulador informou falha, sem prometer outbox local. Após reiniciar, gateway retomou conexão e novo evento chegou. |
| Payload inválido | JSON inválido chegou ao gateway e gerou rejeição sem ACK; processo encerrou com código 2 e exigiu intervenção. |
| Preservação final | Após as falhas MQTT, estado Premium pré-existente continuou igual. Conclusão concedeu +3 somente quando seu endpoint foi chamado explicitamente pelo roteiro. |
| Confirmação no protocolo | Broker registrou tráfego q1 e PUBACK do gateway; auditor correlacionou sete confirmações por evento/mid. |

O [auditor reproduzível](../../../labs/iot/auditar_mqtt.py) aprovou a [correlação dos logs](auditoria-mqtt.txt): sete ACKs têm `HTTP_CONFIRMADO` anterior no mesmo processo e PUBACK correlacionado no broker; o payload inválido final (mid 8) não recebeu PUBACK do gateway. Na última execução, o evento com Core indisponível teve seis falhas sem ACK e intervalo de **17,725 s** entre a primeira tentativa e o PUBACK, medido somente pelo relógio do broker. Não é SLA nem reentrega imediata.

Os 15 cenários são distintos dos testes Maven/Python. O roteiro usa projeto Compose e portas exclusivos, remove apenas seus containers/volume e preserva os três containers antigos, que permaneceram parados. [Versão/digest e diagnóstico Docker](docker-runtime.txt).

## Testes e regressões executados

| Verificação | Resultado |
|---|---|
| Novos testes Core direcionados | **11 aprovados**; [log](testes-core-direcionados.txt). |
| `mvn clean verify` | **96 testes aprovados**, zero falhas/erros/ignorados, BUILD SUCCESS; última execução **56,988 s**. [Log](mvn-clean-verify.txt), [contadores/JaCoCo](resumo-testes.json). |
| Python unittest | **14 aprovados**; cliente espião testa decisões do gateway e um servidor HTTP local testa conferência da resposta. [Log](testes-python.txt). Esses testes não substituem o ensaio MQTT real acima. |
| Core/gateway HTTP real | **12 checks aprovados**: 401, 400, 409, normalização/aceitação, duplicata, janela/evicção e ausência de recompensas. [Saída](execucao-http.txt), [JSON](resultados-http.json), [Core](http-core.txt). |
| Regressão D3 real | **18 checks aprovados**, incluindo FastAPI parado/recuperado e timeout. [Saída](regressao-d3.txt), [JSON](regressao-d3/resultados-lab.json). |
| Regressão D4 real | **25 checks aprovados**, incluindo modos síncrono/assíncrono, consumidor parado, saturação e saldo persistido. [Saída](regressao-d4.txt), [JSON](regressao-d4/resultados-http.json). |
| Fronteiras D1 / núcleo D2 | [Imports](fronteiras.txt) e [núcleo JDK/jdeps](nucleo-isolado.txt) aprovados. |
| Compose e diagramas | [Configuração](compose-config.txt); [Container Mermaid](mermaid-container.txt) e [sequência](mermaid-sequencia.txt) renderizados. |

[Ambiente Java/Maven/Python](ambiente.txt), [instalação Paho no venv temporário](python-install.txt), [versão Python resolvida](python-freeze.txt). O JAR dos ensaios reais não foi instrumentado; seus checks não incrementam cobertura JaCoCo Maven. Os 85 testes anteriores foram preservados, e os 40 arquivos anteriores de fontes/testes mantiveram SHA-256.

Testes Java em [src/test/java/org/example/tdd/iot](../../../src/test/java/org/example/tdd/iot/) e Python em [test_gateway.py](../../../labs/iot/tests/test_gateway.py) incluem: entrada ausente por padrão, token obrigatório, duplicatas, conflito de conteúdo, concorrência na janela, perda de deduplicação no reinício/evicção e rejeição de contrato inválido. Não houve alteração em regras de gamificação.

## Reprodução

No macOS, abra Docker Desktop e confirme que `docker info` funciona **e que containers iniciam**. Nesta execução, a API respondia mas containers mínimos ficaram em Created; reiniciar Docker Desktop uma vez recuperou o runtime, sem apagar dados anteriores.

```bash
# Na raiz: JDK 17, Maven e Python 3.9+
python3 -m venv /tmp/b1-d5-venv
/tmp/b1-d5-venv/bin/pip install -r labs/iot/requirements.txt
mvn clean verify
/tmp/b1-d5-venv/bin/python -m unittest discover -s labs/iot/tests -v
/tmp/b1-d5-venv/bin/python labs/iot/executar-http.py
/tmp/b1-d5-venv/bin/python labs/iot/executar-mqtt.py
/tmp/b1-d5-venv/bin/python labs/iot/auditar_mqtt.py
```

Roteiros: [HTTP](../../../labs/iot/executar-http.py), [MQTT real](../../../labs/iot/executar-mqtt.py), [consumidor/gateway](../../../labs/iot/gateway.py), [simulador](../../../labs/iot/simulador.py). [Execução manual completa](../../../labs/iot/README.md).

Os roteiros regravam seus próprios logs/JSON; para capturar stdout/stderr, redirecionar para `testes-python.txt`, `execucao-http.txt`, `execucao-mqtt.txt` e `auditoria-mqtt.txt` nesta pasta conforme o comando. Cada ensaio MQTT usa logs novos para impedir falsos positivos de tentativas anteriores. O clientId é estável entre reinícios dentro do ensaio, mas exclusivo por execução para não interferir em outras sessões.

Credenciais aleatórias são geradas em `labs/iot/.runtime`, ignorado pelo Git; arquivo JSON 0600, sem valores em argumentos de processo ou logs. Mosquitto lê somente o arquivo de hashes no container. O gerador foi executado com sucesso e agora possui timeout/limpeza dos seus containers temporários. Não publique `.runtime`.

## Contratos, arquitetura e garantias

[Evento puro](../../../src/main/java/org/example/tdd/iot/EventoIot.java), [registro FIFO](../../../src/main/java/org/example/tdd/iot/RegistroEventosIot.java), [controller](../../../src/main/java/org/example/tdd/iot/IotController.java), [configuração](../../../src/main/java/org/example/tdd/iot/IotConfiguration.java), [perfil local](../../../src/main/resources/application-iot-lab.properties). [Normalizador](../../../labs/iot/normalizacao.py), [Mosquitto](../../../labs/iot/mosquitto.conf) e [ACL](../../../labs/iot/acl).

[C4 cumulativo](../../arquitetura/diagramas.md#c4-container--desafio-5), Container [fonte](c4-container.mmd)/[SVG](c4-container.svg) e sequência [fonte](sequencia.mmd)/[SVG](sequencia.svg). Executor em memória D4 não é broker MQTT. Entrada IoT é opcional, local e desabilitada por padrão; não emite conclusão de curso.

- MQTT 3.1.1/QoS 1, retain false, clean_session=False e ACK manual. PUBACK ao dispositivo não comprova aceitação pelo Core. Reconexão e reentrega foram observadas no cenário executado, sem garantia de prazo ou de todo tipo de reinício.
- Sessão/persistência do broker não são persistência do estado Paho. Não há outbox no simulador/gateway nem transação conjunta MQTT/HTTP. Queda entre HTTP e ACK pode duplicar mensagem.
- Deduplicação FIFO do Core é volátil e limitada aos últimos N eventos; reinício/evicção permitem nova aceitação. Não há exatamente uma vez, histórico de curso ou aceitação durável de IoT.
- Payload/rejeição permanente: gateway encerra sem ACK; exige intervenção e pode bloquear outros eventos. Reset manual de sessão descarta pendências; não é recuperação sem perda. Foi testada a parada por payload inválido, não uma DLQ.
- Broker tem fila offline limitada a 128 e volume persistente. Testou-se parada/reinício normal, não crash de disco, HA ou retenção ilimitada. O Core usa H2 em memória; ao reiniciar, o roteiro cria um novo aluno Premium para comparar estados dentro de cada fase, sem alegar persistência do aluno entre processos. Sem assinatura persistente prévia, retain false não recupera mensagens antigas.
- Portas locais, autenticação/ACL e token HTTP foram exercitados. Sem TLS, hardware real, provisionamento/rotação de dispositivos, carga/energia ou segurança de produção. PostgreSQL/Compose principal e navegador não foram exercitados.

## Tentativas anteriores e limitações de medição

O [preflight anterior](mqtt-preflight-anterior.json) registrava daemon indisponível; não descreve o estado final. Depois, a API ficou acessível mas o runtime não iniciava nem um container sem volumes. [Diagnóstico e recuperação](docker-runtime.txt). Um bootstrap interrompido deixou hash parcial sem credenciais utilizáveis; [tentativa](preparacao-interrompida-resultados-mqtt.json) preservada. Foi removido exclusivamente esse arquivo próprio incompleto e a preparação foi repetida com sucesso.

Os 15 cenários reais passaram na primeira execução completa. Uma [auditoria adicional inicial](primeira-auditoria-mqtt.txt) por timestamps exatos falhou devido à diferença de alguns milissegundos entre relógios macOS/VM, não por ACK antecipado demonstrado. [Diagnóstico](relogios-host-vm.txt). Acrescentou-se `HTTP_CONFIRMADO` antes de ack(), repetiu-se o roteiro inteiro com sucesso e a auditoria final usa ordem no mesmo processo, eventId/mid e pacote PUBACK, sem inferir ordem causal de relógios distintos.

## Preservação, segurança e Git

[Preservação por hashes](preservacao.json), [conferência de segurança](seguranca.txt), [validação documental](validacao-documentacao.txt). Fontes/testes D1–D4 e pom.xml preservados; novos logs de regressão ficam somente nesta pasta. Logs contêm caminhos locais e identificadores fictícios. A verificação de segurança compara os segredos reais gerados com o conjunto publicável, sem imprimir seus valores.

D4 permanece no commit local `59cb7d1`. D5 está tecnicamente pronto para revisão/commit, mas **versionamento e publicação ainda dependem de autorização**. Nenhum commit D5, push ou merge realizado; D6 permanece pendente.
