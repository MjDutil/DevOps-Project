# Mini-lab D5 — IoT, MQTT e gateway

**Estado: mini-lab MQTT real validado**, com 15 cenários aprovados no Docker Desktop 29.8.0 e Mosquitto 2.0.22. [Resultados reais e limites](../../docs/evidencias/desafio-5/README.md). As limitações de entrega e segurança abaixo permanecem; D5 aguarda commit/publicação autorizados.

Arquitetura: simulador Python → MQTT 3.1.1/QoS 1 → Mosquitto → gateway Paho → HTTP/JSON → entrada IoT opcional do Core. Não utiliza RabbitMQ/Kafka nem o executor D4 como broker. O evento é recebido demonstrativamente, sem concluir curso, liberar cursos ou conceder moedas.

## Pré-requisitos e verificações sem broker

JDK 17, Maven, Python 3.9+ e Docker Desktop para o ensaio MQTT. No macOS, abra Docker Desktop e aguarde o motor iniciar. `docker info` deve terminar com sucesso antes de iniciar o broker.

```bash
# Na raiz
python3 -m venv /tmp/b1-d5-venv
/tmp/b1-d5-venv/bin/pip install -r labs/iot/requirements.txt
mvn clean verify
/tmp/b1-d5-venv/bin/python -m unittest discover -s labs/iot/tests -v
/tmp/b1-d5-venv/bin/python labs/iot/executar-http.py
docker compose -f labs/iot/compose.yml config --quiet
```

Esses testes não precisam de broker. A suíte Python usa um cliente espião para testar decisões de ACK e um servidor HTTP de teste, não um broker simulado apresentado como MQTT real. `executar-http.py` usa Core real, token temporário e H2 descartável, valida 401/400/409, aceitação, duplicata, evicção e ausência de recompensas.

## Roteiro MQTT completo

Depois que `docker info` funcionar e o JAR estiver construído:

```bash
/tmp/b1-d5-venv/bin/python labs/iot/executar-mqtt.py
```

O roteiro usa um projeto Compose exclusivo, portas livres, credenciais locais, Core/H2 e gateway em processos separados. Testa sucesso, duplicata, autenticação, ACL, gateway desconectado, Core indisponível, broker indisponível/recuperado e payload inválido. Também verifica que presença/início/conclusão de atividade e duplicatas não alteram saldo ou Premium. Grava evidências em `docs/evidencias/desafio-5/` e remove somente containers/volume do projeto criado por ele. O estado final de mensagem inválida é proposital: gateway encerra sem ACK para exigir intervenção. O preflight inicialmente foi bloqueado, mas o roteiro foi executado até o fim após recuperar o Docker. O [histórico de diagnóstico](../../docs/evidencias/desafio-5/docker-runtime.txt) está preservado.

## Execução manual

```bash
# Gera senhas/token aleatórios somente em labs/iot/.runtime (ignorado pelo Git)
/tmp/b1-d5-venv/bin/python labs/iot/preparar_lab.py
# Broker local em 127.0.0.1:18883
# Nome do projeto explícito, separado do Compose principal
docker compose -p b1-iot-manual -f labs/iot/compose.yml up -d
# Core em outro terminal, lê token sem imprimir ou colocar na linha de comando
python3 labs/iot/iniciar_core.py
# Gateway em outro terminal, com clientId estável e clean_session=False
/tmp/b1-d5-venv/bin/python labs/iot/gateway.py
# Simulador em outro terminal
/tmp/b1-d5-venv/bin/python labs/iot/simulador.py --evento labs/iot/evento-exemplo.json
# Repetir exatamente o mesmo arquivo demonstra duplicata
/tmp/b1-d5-venv/bin/python labs/iot/simulador.py --evento labs/iot/evento-exemplo.json
```

`preparar_lab.py` usa `mosquitto_passwd` da imagem fixada `eclipse-mosquitto:2.0.22`, com senha via stdin. Não sobrescreve credenciais existentes. `.runtime/credenciais.json` tem permissão 0600; o arquivo de hashes é legível pelo usuário Mosquitto no container, mas fica em diretório local restrito e ignorado pelo Git. Não publique `.runtime`, não cole seu conteúdo em evidências e não exponha o broker fora do laboratório.

O gateway usa `http://127.0.0.1:8080` por padrão. O perfil `iot-lab` vincula Core a 127.0.0.1 e exige token não vazio. Sem o perfil/propriedade habilitadora, `/iot/eventos` não existe. A capacidade padrão do registro é 1.024 eventos, ajustável com `IOT_CAPACIDADE`.

O simulador sem `--evento` gera um novo evento. Para repetição idêntica, use o mesmo arquivo: reutilizar só o ID com outro instante/conteúdo gera conflito HTTP 409, não uma duplicata válida.

## Contrato e validação

Tópico: `b1/iot/dispositivos/{dispositivo_id}/eventos`. Payload do dispositivo: [exemplo](evento-exemplo.json), versão 1, UUID, ID de dispositivo, tipo e instante com fuso. Tipos: `presenca`, `inicio_atividade`, `conclusao_atividade`.

O gateway limita o payload a 4.096 bytes, exige campos conhecidos, valida UUID/fuso/versão/tópico e normaliza para `eventId`, `dispositivoId`, tipo em maiúsculas, instante UTC e `versao`. Usa `POST /iot/eventos` com token do gateway. O Core confirma `recebido` ou `duplicado` com o mesmo evento. Campos inválidos geram 400; mesmo ID com outro conteúdo, 409; token inválido/ausente, 401.

A janela FIFO em memória deduplica os últimos N eventos. Após evicção ou reinício, um ID pode ser aceito novamente. Não é armazenamento durável, histórico de curso nem operação de gamificação. Não há garantia de exatamente uma vez.

## ACK, falhas e intervenção

- MQTT é 3.1.1, QoS 1 e `retain=false`. PUBACK ao simulador confirma somente o trecho até o broker, não o Core.
- Gateway tem clientId estável, `clean_session=False` e `manual_ack=True`. O ACK é solicitado somente após HTTP 200 de aceitação/duplicata com confirmação do mesmo evento. O log `HTTP_CONFIRMADO` precede ack(); `ACK_ENFILEIRADO` não prova sozinho entrega do pacote. O [auditor dos logs reais](auditar_mqtt.py) também confere PUBACK no broker por mid, sem usar relógios diferentes como prova de ordem causal.
- Falha transitória HTTP/rede: sem ACK; gateway desconecta e tenta nova conexão com backoff de 0,5 até 5 s. A reentrega depende da sessão, conexão e limites do broker, não de um timer garantido. No ensaio real, a sessão foi retomada e a reentrega com DUP foi observada após recuperação do Core; isso não garante todo cenário de falha.
- Payload inválido, QoS inesperado ou HTTP permanente: sem ACK e gateway encerra com código 2. Não faz loop infinito nem confirma um evento rejeitado. Mensagem pendente pode impedir progresso; exige intervenção. Um dispositivo autenticado com payload inválido pode causar essa parada, limite deliberado do mini-lab, não solução de produção.
- Para limpar uma sessão de laboratório, pare primeiro o gateway e execute o comando explícito abaixo. **Isso descarta assinaturas e mensagens pendentes daquela sessão.** Não é recuperação sem perda.

```bash
/tmp/b1-d5-venv/bin/python labs/iot/resetar_sessao.py --confirmar-descarte
```

## Persistência e segurança: limites

O Mosquitto está configurado com volume, persistência, fila offline limitada a 128 mensagens e uma mensagem em voo por consumidor. Isso não prova retenção ilimitada ou sobrevivência a crash de disco. Mensagens publicadas antes de estabelecer a assinatura persistente podem não ser recebidas, pois `retain=false`.

O cliente Paho mantém estado de sessão em memória; não há outbox persistente no simulador/gateway. O reinício do cliente perde estado local; uma sessão existente no broker não resolve todos os cenários. Queda entre aceitação HTTP e ACK pode gerar duplicata. A deduplicação também não sobrevive a reinício do Core nem à evicção. [Limites oficiais Paho](https://eclipse.dev/paho/clients/python/docs/).

Mosquitto exige autenticação: dispositivo-01 publica somente no próprio tópico, gateway lê os tópicos de eventos. MQTT 3.1.1 pode confirmar o pacote e descartar uma publicação negada por ACL; portanto PUBACK não é prova suficiente de autorização. O roteiro verifica também o log de negação do broker e a ausência de aceitação pelo gateway.

Portas são locais; sem TLS neste ensaio loopback. Autenticação/ACL configuradas e token HTTP não significam segurança de produção: faltam TLS, provisionamento/rotação de dispositivos, proteção contra abuso e persistência ponta a ponta. Nenhuma credencial real deve ser versionada.

[ADR-005](../../docs/adr/ADR-005.md) · [C4](../../docs/arquitetura/diagramas.md#c4-container--desafio-5) · [Evidências](../../docs/evidencias/desafio-5/README.md)
