# Architecture Lab B1 — Registro arquitetural cumulativo

**Produto:** Educação Continuada Gamificada, evolução incremental do MVP da AC1.
**Repositório:** https://github.com/MjDutil/DevOps-Project
**Equipe B1 confirmada:** Felipe, Isadora e Maria Julia, conforme o [registro preservado da AC1](../ac1/README.md#integrantes) e confirmação da usuária. Sobrenomes completos e RAs não constam no registro original; nenhuma informação foi inventada.

> Atualização D2 (2026-10-27): D1 consolidado em commits separados de limpeza e implementação. D2 valida Ports & Adapters na conclusão de curso com contrato comum fake/JPA e execução pura sem Spring/banco. [Evidências D1](../evidencias/desafio-1/README.md) e [D2](../evidencias/desafio-2/README.md). Esse registro histórico refere-se à entrega D1/D2; o D3 foi validado posteriormente, conforme seção 2.5.

> Nota do pacote inicial (estado anterior): este registro reúne **decisões propostas** e um mini-lab de código para D1/D2. As demais evidências marcadas PENDENTES não foram executadas; não devem ser declaradas prontas antes dos testes e prints. O projeto real possui cursos liberados, recompensas no fórum e Premium, mas ainda não tem ranking, badges, ML/RAG real ou IoT. O serviço Python demonstrativo foi validado posteriormente no D3.

## Como consultar a documentação para avaliação

O [README da raiz](../../README.md) apresenta o projeto, as tecnologias, o estado dos seis desafios e os comandos essenciais. Este registro cumulativo concentra a matriz de ASRs e a rastreabilidade RF → RNF → ASR → RPC → alternativas → ADR → C4 → evidência; os ADRs e as evidências vinculadas detalham os critérios obrigatórios para avaliação. A [revisão editorial](../evidencias/revisao-readme/README.md) registra a nova execução dos testes e a validação de links/diagramas. Merge/publicação em `main` aguardam aprovação.

## 1. RFs e RNFs do case

- **RF01 Ranking:** previsto no enunciado; não implementado na AC1 fornecida.
- **RF02 Conquistas:** parcialmente representado por liberação de cursos, bônus de fórum e Premium; badges não implementados.
- **RF03 Recomendação inteligente:** contrato demonstrativo Spring/FastAPI validado no D3; modelo inteligente futuro.
- **RF04 Assistente educacional:** contrato demonstrativo e fallback validados no D3; RAG/LLM real futuro.
- **RF05 Notificações/eventos:** consumidor HTTP demonstrativo no D4; sem email real ou entrega durável.
- **RF06 Receber eventos IoT:** recepção demonstrativa MQTT → gateway → Core validada no D5; sem efeitos em gamificação.

RNF/FURPS+: Performance (desempenho/escalabilidade), Reliability (disponibilidade/recuperação), Supportability (manutenção/testabilidade/deploy), Functionality/Security (autorização de dispositivos), Usability (mantida na experiência do aluno) e outras restrições do case.

## 2. Matriz cumulativa de ASRs

| Desafio | RF | ASR 1 | ASR 2 | ASR 3 | RPC principal | ADR | Evidência |
|---|---|---|---|---|---|---|---|
| 1 | 01 futuro, 02 exercitado | Manutenibilidade | Escalabilidade | Complexidade operacional | Spring Boot/6 devs/JPA/legado AC1 | [001](../adr/ADR-001.md) | [Fronteira pública, comparação e testes HTTP/JPA antes/depois](../evidencias/desafio-1/README.md) |
| 2 | 02 exercitado, 01 futuro | Testabilidade | Modificabilidade | Manutenibilidade | Spring Boot, regras reaproveitadas, sem reescrita total | [002](../adr/ADR-002.md) | [Contrato comum fake/JPA, JDK isolado, transação e regressões](../evidencias/desafio-2/README.md) |
| 3 | 03, 04 | Tolerância a falhas | Deploy independente | Escala da IA | Python + Core Java | [003](../adr/ADR-003.md) | [Integração real, interrupção, recuperação e timeout de leitura](../evidencias/desafio-3/README.md) |
| 4 | 02, 05 demonstrativo | Tolerância a falhas | Latência | Consistência transacional | Core conclui com consumidor indisponível; broker exige justificativa | [004](../adr/ADR-004.md) | [Pós-commit, comparação síncrona/assíncrona, saturação e HTTP real](../evidencias/desafio-4/README.md) |
| 5 | 06 demonstrativo | Eficiência de comunicação | Tolerância à conectividade intermitente | Segurança de integração | Dispositivos limitados, rede instável e custo de broker | [005](../adr/ADR-005.md) | [HTTP, MQTT real, ACL, sessão persistente e reentrega](../evidencias/desafio-5/README.md) |
| 6 | 03/04 observados, 02 preservado; 01–06 na evolução | Testabilidade | Observabilidade | Confiabilidade | Jenkins/Prometheus/Grafana; Java 17; preservar contratos | [006](../adr/ADR-006.md) | [CI/métricas reais; entrega B1](../evidencias/desafio-6/README.md) |

## 2.1 Rastreabilidade do Desafio 1 implementado

| Elemento | Registro |
|---|---|
| RF | RF02: conclusão de curso com recompensa; RF01: crescimento futuro de ranking, sem implementação neste mini-lab. |
| RNF | Manutenibilidade, escalabilidade, complexidade operacional, testabilidade e desempenho analisados. |
| ASR | Manutenibilidade → contrato público; escalabilidade → fronteira evolutiva; complexidade operacional → um processo/deploy. |
| RPC | Core Java/Spring Boot, equipe de seis no case, JPA/banco relacional e legado AC1. |
| Alternativas | Camadas × monólito modular × decomposição seletiva, com trade-offs no ADR-001. |
| ADR | [ADR-001](../adr/ADR-001.md): monólito modular incremental na conclusão de curso. |
| C4 | [Container atual](diagramas.md): um Core; módulo não é serviço separado. |
| Evidência | [D1](../evidencias/desafio-1/README.md): clone/histórico, baseline local, dependências, diff e testes reais. |

A migração preserva o legado e torna explícita apenas a fronteira selecionada. Não comprova capacidade para 100.000 usuários, eliminação de concorrência ou modularização completa do Core. O fake e a porta de persistência já existiam no pacote inicial; a comparação de adapters foi concluída no D2, conforme seção seguinte.

## 2.2 Rastreabilidade do Desafio 2 implementado

| Elemento | Registro |
|---|---|
| RF | RF02: conclusão/recompensa; fórum e Premium preservados. RF01 continua futuro. |
| RNF / ASR | Testabilidade → execução somente JDK; modificabilidade → adapters intercambiáveis pelo contrato; manutenibilidade → regra única e infraestrutura externa. |
| RPC | Core Spring Boot, domínio AC1 reaproveitado, JPA/banco relacional existente, proibição de reescrita global. |
| Alternativas | Manter estado D1 parcial em meio às camadas legadas × consolidar Ports & Adapters incremental × migração global Clean/Onion/DDD; a AC1 original também é comparada. |
| ADR | [ADR-002](../adr/ADR-002.md): porta pura, fake e JPA com as mesmas pré-condições. |
| C4 | [Component do Core](diagramas.md): entrada, fachada transacional, aplicação, domínio, porta e adapters. |
| Evidência | [D2](../evidencias/desafio-2/README.md): contrato comum, JDK isolado, verificação de dependências, commit/rollback, SQL observado e regressões D1. |

A porta atualiza somente cursos se o snapshot lido ainda corresponder ao banco. O adapter elimina a segunda chamada de busca e usa UPDATE parcial. A fachada mantém a transação do D1; a escrita exige transação existente. Os testes de conflito são determinísticos por snapshot obsoleto, sem alegação de validação de carga ou concorrência global do legado.

## 2.3 Validação posterior de cobertura — sem nova implementação arquitetural

Após D1/D2, `mvn clean verify` aprovou **75 testes**, com JaCoCo global em **100% de instruções (669/669), branches (16/16) e linhas (160/160)**. Foram preservados os testes anteriores e acrescentados cenários do código existente: cliente/controller IA, aluno ausente, atualização de média e inicialização pelo main real. Nenhuma regra ou fonte de produção foi alterado, e não houve exclusões JaCoCo.

O núcleo `domain` + `gamificacao.*` já estava coberto antes; esta ampliação melhora a cobertura global, sem comprovar o laboratório real Spring/FastAPI ou concluir D3. [Comparação por classe, comandos e limites](../evidencias/cobertura/README.md). Os totais históricos de 25 testes D1 e 51 D2 continuam identificando suas entregas originais.

## 2.4 Auditoria final dos mini-labs D1/D2

A nova execução de `mvn clean verify` confirmou 75 testes e 100% de instruções/branches/linhas JaCoCo, sem exclusões. Os mesmos 11 casos do contrato passaram no fake e no JPA/H2, mais três testes transacionais JPA. [Checklist dos critérios do enunciado e caminhos de comprovação](../evidencias/auditoria-final/README.md).

O registro cumulativo, os ADRs e as evidências permitem avaliar as decisões sem executar o sistema; o README principal orienta a leitura. Naquela auditoria histórica, publicação e identificação B1 ainda eram pendências; a situação atual da entrega está na seção 7. D1 (`8bf8785`), D2 (`34206c7`), documentação (`ada5768`) e testes/cobertura (`20b933c`) permanecem separados no histórico. A auditoria/documentação final fica em commit próprio.

<a name="d3-validado"></a>

## 2.5 Desafio 3 — serviço Python e IA (validado em 2026-11-03)

| Elemento | Registro |
|---|---|
| RF | RF03 recomendação e RF04 assistente: contratos demonstrativos; sem ML/RAG real. |
| RNF | Deploy independente, escalabilidade, tolerância a falhas, interoperabilidade, desempenho e manutenibilidade. Classificação e justificativas no ADR-003. |
| ASR 1 | Tolerância a falhas → timeout/fallback e ausência de dependência de IA nos fluxos essenciais. |
| ASR 2 | Deploy independente → processo/artefato Python separado, reiniciado sem reiniciar Core no laboratório. |
| ASR 3 | Escalabilidade → fronteira permite dimensionar Python separadamente; capacidade/réplicas não medidas. |
| RPC | Core Java/Spring, ML/RAG Python e IA indisponível sem bloquear operação essencial. |
| Alternativas | IA embutida no Core × módulo acoplado × serviço Python independente. Escolhida a terceira; ML/RAG juntos inicialmente para evitar operação adicional sem necessidade medida. |
| ADR | [ADR-003](../adr/ADR-003.md): alternativas, consequências, contrato e limites. |
| C4 | [Container D3](diagramas.md#c4-container--estado-validado-do-desafio-3): protocolos e fronteira da IA; Component D2 preservado. |
| Mini-lab | Spring → FastAPI real; Python interrompido; fallback dos dois endpoints; criação/conclusão/fórum/Premium sem Python; recuperação sem reinício do Core. |
| Evidências | [18 verificações HTTP, logs e roteiro](../evidencias/desafio-3/README.md); timeout de leitura observado em 1.206,55/1.206,38 ms, com conclusão simultânea em 7,10/6,85 ms. |

O teste Maven desta etapa aprovou 75 testes, sem falhas/erros/ignorados; os 18 checks reais são separados dessa contagem. Timeout de conexão de 700 ms está configurado, mas não foi medido em rede real. O de leitura é 1.200 ms; os valores não são um prazo total garantido. Chamadas IA são síncronas e ocupam threads; não há prova de isolamento sob saturação. HTTP 200 degradado e falta de validação de schema são comportamentos existentes preservados.

A evidência atende ao mini-lab oficial de fronteira e falha; não conclui os modelos funcionais RF03/RF04. D1/D2 mantêm seus fontes/testes; D4–D6 evoluem nas seções seguintes. Publicação no Git depende de autorização para commit/merge/push.

## 2.6 Desafio 4 — comunicação e eventos

| Elemento | Rastreabilidade |
|---|---|
| RF | RF02: preservar recompensas; RF05: notificação demonstrativa da operação processada. Sem histórico de curso identificado. |
| RNF | Tolerância a falhas, disponibilidade, latência, consistência, desacoplamento e recuperabilidade analisados no ADR-004. |
| Até três ASRs | Tolerância a falhas → capturar falhas/rejeição; latência → executor dedicado sem CallerRunsPolicy; consistência → encaminhar somente após commit. |
| RPC | Consumidor externo não impede operação essencial; broker exige custo operacional justificado; Java/Spring e regras preservados. |
| Alternativas | REST síncrono × assíncrono em memória × broker externo. Escolhido mecanismo em memória para mini-lab, com REST síncrono de comparação. |
| ADR | [ADR-004](../adr/ADR-004.md): melhor esforço, sem persistência/retry/replay; broker/outbox somente se recuperação se tornar requisito. |
| C4 | [Container D4 e sequência](diagramas.md#c4-container--desafio-4): executor interno e consumidor HTTP separado. |
| Evidências | [D4](../evidencias/desafio-4/README.md): testes transacionais, indisponibilidade, saturação, HTTP real e regressão D1–D3. |

A fachada mantém o endpoint e a transação; domínio, caso de uso, `AlunoStore` e adapter JPA não foram reescritos. O evento puro inclui UUID, aluno, saldo e instante. O listener AFTER_COMMIT não escreve no banco. Notificações são desabilitadas por padrão; quando habilitadas, o modo padrão é assíncrono, com uma thread e fila limitada. Fila cheia descarta a notificação com log, preservando a conclusão. Em modo síncrono, a tentativa HTTP termina antes da resposta, mas falhas também são capturadas.

A decisão aceita perda de eventos em falha/reinício e ausência de recuperação automática. No fluxo D4 não há broker nem garantia de entrega. O ADR-004 inicial propunha RabbitMQ/redelivery condicionalmente; a evidência atual fundamenta uma solução menor e deixa recuperação durável para uma decisão futura. D5 evolui a entrada IoT na seção seguinte; D6 é registrado na seção 2.8.

## 2.7 Desafio 5 — IoT/MQTT validado no mini-lab

| Elemento | Rastreabilidade |
|---|---|
| RF | RF06: receber e normalizar eventos para uso demonstrativo no Core; sem chamar conclusão de curso ou alterar recompensas. |
| RNF | Interoperabilidade, eficiência de comunicação, disponibilidade, conectividade intermitente, escalabilidade e segurança analisados no ADR-005. |
| Até três ASRs | Eficiência → tópicos e payload pequeno; conectividade intermitente → QoS 1/sessão/reconexão; segurança → autenticação/ACL, validação e token HTTP. |
| RPC | Dispositivo limitado, conexão instável e custo operacional do protocolo/broker; Core e regras existentes preservados. |
| Alternativas | REST direto × MQTT com gateway × MQTT seguido de mensageria interna. Escolhida segunda; sem necessidade de RabbitMQ/Kafka adicional. |
| ADR | [ADR-005](../adr/ADR-005.md): contrato, ACK condicionado ao HTTP e limites de persistência/deduplicação. |
| C4 | [Container/fluxo D5](diagramas.md#c4-container--desafio-5): dispositivo, broker, gateway e Core. Trecho MQTT exercitado em Mosquitto real. |
| Evidência obtida | [D5](../evidencias/desafio-5/README.md): 96 testes Maven, 14 Python, 12 checks HTTP, 15 cenários MQTT reais e regressões D3/D4. |
| Fechamento | Publicação/subscrição, autenticação/ACL, sessão persistente, falhas de Core/broker e reentrega observadas. D5 consolidado em `a3b350e`; integra a entrega B1 autorizada. |

O endpoint opcional `/iot/eventos` é desabilitado por padrão; perfil de laboratório usa loopback e token. Aceita evento normalizado, detecta repetição idêntica e rejeita conflito. A janela FIFO é em memória: reinício/evicção permitem nova aceitação. Não há alteração de aluno ou histórico de curso.

O gateway foi implementado com MQTT 3.1.1, clientId estável e clean_session=False; ACK manual somente após aceitação/duplicata HTTP. Falha transitória exige reconexão para tentar retomada; payload permanente fica sem ACK e exige intervenção. Persistência do broker não é persistência do Paho nem transação ponta a ponta. Sessão persistente e reentrega com DUP foram observadas no laboratório real, sem garantia de exatamente uma vez. D6 é registrado na seção 2.8.

## 2.8 Desafio 6 — CI e observabilidade

| Elemento | Rastreabilidade |
|---|---|
| RF | RF03/RF04: observar fallback; RF02 preservado; demais contratos mantidos. RF01 permanece futuro. |
| RNF | Observabilidade, testabilidade, confiabilidade, deploy independente, recuperabilidade e desempenho. |
| ASR 1 | Testabilidade → Maven e Python aprovam o snapshot; teste falhando bloqueia aprovação do JAR. |
| ASR 2 | Observabilidade → contador separado por operação revela degradação mesmo com HTTP 200; coleta e painel. |
| ASR 3 | Confiabilidade → SHA-256 liga o JAR arquivado pelo Jenkins ao JAR implantado; recuperação IA sem reiniciar Core. |
| RPC | Jenkins, Prometheus/Grafana obrigatórios; Java 17; sem Docker socket no Jenkins; preservar D1–D5. |
| Alternativas | Pipeline único × pipelines por componente; logs isolados × instrumentação/coleta temporal. Único escolhido para reduzir coordenação no mini-lab. |
| ADR/C4 | [ADR-006](../adr/ADR-006.md) e [Deployment D6](diagramas.md#c4-deployment--desafio-6). |
| Evidência | [Resultados reais e limitações](../evidencias/desafio-6/README.md); builds #1 e #5 aprovados, #4 bloqueado por JUnit, falha/recuperação IA com métricas e capturas reais; limites documentados. |

Jenkins executa CI; implantação local é controlada pelo operador e não representa CD de produção. Actuator é bloqueado por padrão, com porta interna exclusiva no perfil de laboratório. `up` mede coleta, não SLA; o contador de fallback complementa o HTTP 200. D4/D5 mantêm suas garantias e limites, sem novo broker ou alterações de regras.

## 3. Alternativas, consequências, ADRs e C4

Os seis ADRs estão em [`docs/adr/`](../adr/). Cada um contém contexto, RF/RNF, no máximo três ASRs, RPC, alternativas, trade-offs e consequências. Diagramas Container, Component e Deployment ficam em [`diagramas.md`](diagramas.md). Container e Component distinguem o estado implementado; a visão alvo histórica é identificada como proposta; o Deployment D6 representa o laboratório operacional.

## 4. Estado atual e evidências, por desafio

| D | O que mostrar ao professor | Artefatos e ação pendente |
|---|---|---|
| 1 | Contrato público do módulo, dependências e preservação funcional | [Evidências D1](../evidencias/desafio-1/README.md), ADR-001 e C4 Container atualizados |
| 2 | Mesma regra e contrato com fake/JPA; sem dependências técnicas no núcleo | [Testes de contrato, execução isolada e integração H2](../evidencias/desafio-2/README.md) |
| 3 | Spring chama FastAPI, derrubar FastAPI, Core mantém serviço essencial | [Laboratório real concluído: logs, respostas e tempos](../evidencias/desafio-3/README.md); ML/RAG real fora do mini-lab |
| 4 | Comparar espera REST síncrona e tentativa assíncrona pós-commit; consumidor parado e fila cheia | [Evidências D4](../evidencias/desafio-4/README.md); sem broker/entrega durável |
| 5 | Simulador MQTT → broker → gateway → entrada IoT demonstrativa; sem pontuação | [15 cenários MQTT reais, HTTP e regressões](../evidencias/desafio-5/README.md); commit D5 `a3b350e`; integra a entrega B1 |
| 6 | Jenkins passa/falha e painel Prometheus/Grafana reage a falha induzida | [Execução e evidências D6](../evidencias/desafio-6/README.md); mini-lab executado; D6 consolidado em `42128fa`; publicação autorizada na branch B1 |

### Evidências que NÃO contam sozinhas

Um diagrama de tecnologia, um `docker-compose.yml` que nunca foi executado ou um screenshot sem falha simulada **não provam** a arquitetura. Salvar comandos, resultados, versões, tempos e prints reais com referência ao ADR.

## 5. Execução e verificação do mini-lab 1/2

A partir da raiz do projeto:

```bash
./mvnw test
# Windows PowerShell: .\mvnw.cmd test

# Teste puro de porta + fake, também possível sem Maven/Spring:
mkdir -p /tmp/b1-domain
javac -d /tmp/b1-domain \
  src/main/java/org/example/tdd/domain/Aluno.java \
  src/main/java/org/example/tdd/gamificacao/application/*.java \
  src/test/java/org/example/tdd/gamificacao/FakeAlunoStore.java \
  docs/arquitetura/labs/LabDomainSmoke.java
java -cp /tmp/b1-domain LabDomainSmoke
```

Para comparar com o JPA, subir `docker compose up --build -d`, criar aluno via `POST /alunos` com média 8.0 e depois `POST /alunos/{id}/concluir-curso`; confirmar +3 cursos. **Repetir com média 7.0:** não ganha cursos. Registrar `docker compose logs app` e print do Swagger. H2 foi validado automaticamente nos D1/D2. Este roteiro manual PostgreSQL/Docker permanece pendente e não foi usado como evidência de execução.

### Mini-lab 3 — fronteira de IA opcional, evidência obrigatória

```bash
docker compose up --build -d app ia postgres
curl http://localhost:8080/ia/recomendacoes/1
docker compose stop ia
curl http://localhost:8080/ia/recomendacoes/1
# Esperado: status "indisponivel" e recomendacoes vazias; Core continua respondendo.
# Reativar para o ensaio de recuperação:
docker compose start ia
```

O roteiro Docker acima continua sem execução. O laboratório real foi executado com JAR Spring/H2 e Uvicorn locais, incluindo falha/recuperação e servidor lento. [Comandos reproduzíveis, logs e resultados D3](../evidencias/desafio-3/README.md).

## 6. Entrega final e plano da AF

- [x] Confirmar equipe B1: Felipe, Isadora e Maria Julia, mesma equipe registrada na AC1.
- [x] Executar testes Maven D1 e registrar saída, data e commit-base; consultar limites no registro D1.
- [x] Registrar fontes, diffs e dependências antes/depois D1; comprovar regressão HTTP/JPA em H2.
- [x] Revisar e consolidar D1: limpeza `a25b2b2` e implementação `8bf8785`; registro histórico do D1.
- [x] Testar fake/JPA D2, execução JDK isolada e transações; registrar outputs.
- [x] Executar e provar FastAPI, fallback, recuperação e timeout de leitura D3; [evidências](../evidencias/desafio-3/README.md).
- [x] Implementar comparação síncrona/assíncrona D4; resultados e limites nas [evidências](../evidencias/desafio-4/README.md).
- [x] Executar e provar MQTT real D5, incluindo falhas/ACL e sessão persistente. [Resultados e roteiro](../evidencias/desafio-5/README.md).
- [x] Executar CI Jenkins, bloqueio JUnit e experimento Prometheus/Grafana D6; [resultados e limites](../evidencias/desafio-6/README.md).
- [x] Atualizar Container/Component para o estado D1/D2; Deployment futuro explicitamente identificado.
- [x] Versionar documentação e evidências D1/D2; histórico preservado na entrega B1.

[Síntese das decisões para a PoC da AF](sintese-af.md): capacidades demonstradas, limites e evolução condicionada a novos requisitos. MQTT validado no D5; D6 distingue CI Jenkins de implantação local.

## 7. Auditoria final do B1

A [auditoria dos seis desafios](../evidencias/auditoria-final-b1/README.md) confronta o enunciado oficial com ADRs, código, diagramas e evidências. Jenkins #5 aprovou o fluxo normal final em uma única tentativa (101 Maven + 14 Python), preservando #1–#4. D6 foi consolidado em `42128fa`, e a equipe B1 foi confirmada como a mesma da AC1. A publicação foi autorizada exclusivamente na [branch B1](https://github.com/MjDutil/DevOps-Project/tree/feature/architecture-lab-b1), sem merge em main; o histórico e os resultados reais permanecem preservados.
