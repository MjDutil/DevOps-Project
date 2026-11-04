# Architecture Lab B1 — Educação Continuada Gamificada

Evolução arquitetural de uma plataforma de educação, com decisões justificadas e evidências técnicas para avaliação acadêmica.

[Sobre](#sobre-o-projeto) · [Tecnologias](#tecnologias-utilizadas) · [Arquitetura](#arquitetura) · [Desafios](#desafios) · [Execução](#como-executar-e-testar) · [Documentação e equipe](#documentação-e-equipe)

## Sobre o projeto

Na **AC1**, construímos um MVP de gamificação em Java/Spring Boot e Vue.js. O **B1 — Architecture Lab** evolui esse mesmo produto em seis desafios, analisando como organizar o Core, integrar serviços e preparar sua operação sem reescrever as regras existentes.

**Funcionalidades implementadas:** média acima de 7 libera três cursos; participação elegível no fórum libera um curso; a partir de 12 cursos, o aluno recebe Premium, voucher e três moedas.

**Integração demonstrativa:** o Core consulta recomendação e assistente em uma API Python. O D3 validou comunicação, fallback, timeout de leitura e recuperação; as respostas são demonstrativas, sem ML, RAG ou LLM reais.

O D4 acrescenta **notificação HTTP demonstrativa após commit**, assíncrona em memória, com modo síncrono para comparação. Fica desabilitada por padrão e não oferece entrega durável. Ranking, badges e email real ainda não estão implementados. A entrada IoT demonstrativa é opcional e não altera recompensas; o caminho MQTT → gateway → Core foi validado com broker real. **D1–D5 estão validados no escopo dos mini-labs; D6 demonstrou CI Jenkins e observabilidade do fallback, com fechamento técnico validado.**

## Tecnologias utilizadas

| Situação | Tecnologias e uso |
|---|---|
| Implementadas no Core | Java 17, Spring Boot, Spring Data JPA e API REST. H2 utilizado nas validações automáticas. |
| Presentes na interface | Vue.js, Vite e Axios. Interface herdada da AC1; navegador não validado nesta etapa B1. |
| Integração demonstrativa validada | Python, FastAPI e Uvicorn, consumidos pelo Core por HTTP/JSON. |
| Testes e build utilizados | Maven, JUnit, Mockito, MockMvc e JaCoCo. |
| Legado configurado, sem execução validada nesta etapa | PostgreSQL, PgAdmin e Nginx do Compose principal. Compose isolado é utilizado nos labs D5/D6. |
| Comunicação demonstrativa D4 | Executor em memória no Core e consumidor HTTP Python padrão; sem broker e sem persistência de eventos. |
| D5 demonstrativo validado | Python/Paho, MQTT 3.1.1/QoS 1 e Mosquitto em Docker. Autenticação, ACL, sessão e encaminhamento HTTP exercitados. |
| Operação D6 demonstrada | Jenkins, Actuator/Micrometer, Prometheus e Grafana. CI e implantação local controlada; sem CD de produção. |

## Arquitetura

O Core é um **monólito modular incremental**. Na conclusão de curso, uma API pública delimita a capacidade e **Ports & Adapters** mantém a regra de domínio independente de Spring e JPA. O serviço Python tem processo próprio e é opcional: sua indisponibilidade não impede as operações essenciais testadas do Core. No D4, o envio de notificações também é opcional e ocorre somente após commit; fila cheia ou falha HTTP não desfazem a conclusão.

```mermaid
flowchart TB
    operador["Operador local<br/>Implantação controlada, sem CD de produção"]
    subgraph host["Docker Desktop / laboratório local isolado"]
        jenkins["Jenkins / Java 21<br/>Build JDK 17, Maven e Python<br/>Sem socket Docker"]
        fontes["Snapshot identificado<br/>Commit-base e SHA-256 por arquivo"]
        artefato["Arquivo aprovado pelo Jenkins<br/>JAR, SHA-256, JUnit e JaCoCo"]
        core["Core Spring Boot / Java 17<br/>Mesmo JAR aprovado<br/>Actuator interno 9091"]
        banco[("H2 em memória<br/>Laboratório sem persistência de negócio")]
        ia["FastAPI / Python<br/>RF03 e RF04 demonstrativos"]
        prom["Prometheus<br/>Coleta a cada 2 segundos"]
        graf["Grafana<br/>Datasource e dashboard provisionados"]
    end
    fontes -->|Cópia somente leitura e verificação| jenkins
    jenkins -->|Somente após testes aprovados| artefato
    artefato -->|Download autenticado e verificação SHA-256| operador
    operador -->|Docker local / sem recompilar JAR| core
    core -->|JPA / JDBC| banco
    core -->|HTTP REST / JSON opcional| ia
    prom -->|HTTP GET /actuator/prometheus / rede interna| core
    graf -->|HTTP API / PromQL| prom
    operador -->|HTTP local autenticado| jenkins
    operador -->|HTTP local autenticado| graf
    operador -->|HTTP local / consultas| prom
```

Consulte o [C4 completo](docs/arquitetura/diagramas.md), a [fonte Mermaid atual](docs/evidencias/desafio-6/c4-deployment.mmd) e o [registro das decisões](docs/arquitetura/README.md). O caminho IoT/MQTT foi exercitado no laboratório real; não utiliza o executor D4 como broker. A modularização é parcial; domínio e persistência ainda são compartilhados com fluxos legados.

## Desafios

| Desafio e objetivo | Decisão | Estado real | Documentação e evidências |
|---|---|---|---|
| **D1 — Evolução do Core:** delimitar uma capacidade existente | Monólito modular na conclusão de curso | Implementado; comparação antes/depois e 25 testes na entrega D1 | [ADR-001](docs/adr/ADR-001.md) · [C4 Container](docs/arquitetura/diagramas.md#c4-container--estado-do-desafio-1) · [Evidências](docs/evidencias/desafio-1/README.md) |
| **D2 — Organização interna:** proteger as regras das tecnologias | Ports & Adapters, com contrato comum fake/JPA | Implementado; núcleo isolado e 51 testes na entrega D2 | [ADR-002](docs/adr/ADR-002.md) · [C4 Component](docs/arquitetura/diagramas.md#c4-component--core-estado-do-desafio-2) · [Evidências](docs/evidencias/desafio-2/README.md) |
| **D3 — Serviço Python e IA:** definir fronteira e tratar falhas | FastAPI independente, com timeout e fallback | Mini-lab validado; 18 verificações HTTP reais; sem modelos de IA | [ADR-003](docs/adr/ADR-003.md) · [C4 Container](docs/arquitetura/diagramas.md#c4-container--estado-validado-do-desafio-3) · [Evidências](docs/evidencias/desafio-3/README.md) |
| **D4 — Comunicação e eventos:** preservar conclusão com consumidor indisponível | Notificação pós-commit, assíncrona em memória; REST síncrono para comparação | Mini-lab validado; 25 verificações HTTP reais; sem entrega durável | [ADR-004](docs/adr/ADR-004.md) · [C4 e sequência](docs/arquitetura/diagramas.md#c4-container--desafio-4) · [Evidências](docs/evidencias/desafio-4/README.md) |
| **D5 — IoT e MQTT:** receber eventos de dispositivos | MQTT 3.1.1/QoS 1, gateway e recepção HTTP opcional; sem recompensas | Mini-lab validado; 15 cenários MQTT reais aprovados, sem garantia de exatamente uma vez | [ADR-005](docs/adr/ADR-005.md) · [C4 e fluxo](docs/arquitetura/diagramas.md#c4-container--desafio-5) · [Evidências](docs/evidencias/desafio-5/README.md) |
| **D6 — CI/CD e observabilidade:** construir, entregar e observar | Pipeline único Jenkins, artefato identificado e métricas do fallback | Mini-lab executado; CI positiva, bloqueio JUnit e métricas reais; sem CD de produção | [ADR-006](docs/adr/ADR-006.md) · [C4 Deployment](docs/arquitetura/diagramas.md#c4-deployment--desafio-6) · [Evidências](docs/evidencias/desafio-6/README.md) |

A fundamentação de cada desafio segue **RF → RNF → ASR → RPC → alternativas → ADR → C4 → evidência** no [registro arquitetural cumulativo](docs/arquitetura/README.md), com até três ASRs por desafio.

## Como executar e testar

Pré-requisitos: **JDK 17 e Maven** (3.9.11 nas execuções registradas). Para a interface, Node.js compatível com Vite 7 e npm; para o laboratório IA, Python com ambiente virtual, conforme o [roteiro FastAPI](labs/ia/README.md).

No Terminal do macOS, entre na pasta do projeto; ajuste o caminho se necessário:

```bash
cd ~/Downloads/DevOps-Project-B1
java -version
mvn -version
mvn clean verify
```

A execução local D6 aprovou **101 testes Maven e 14 Python**. JaCoCo: 98,19% de instruções, 92,31% de branches e 98,81% de linhas. O Jenkins aprovou esses 115 testes no build #1 e novamente na configuração final no #5; o controle negativo #4 foi bloqueado por uma falha JUnit prevista. O experimento real confirmou fallback da IA, Core funcional e recuperação observados no Prometheus/Grafana. [Resultados e limitações D6](docs/evidencias/desafio-6/README.md). Os [15 cenários MQTT D5](docs/evidencias/desafio-5/README.md) e os mini-labs anteriores mantêm suas evidências históricas, separadas dessas contagens.

Para iniciar o backend com H2 em memória:

```bash
mvn spring-boot:run
```

Em outro terminal, para iniciar a interface:

```bash
cd ~/Downloads/DevOps-Project-B1/frontend
npm ci
npm run dev
```

Endereços esperados: interface `http://localhost:5173`, API `http://localhost:8080` e Swagger `http://localhost:8080/swagger-ui/index.html`. Exemplos dos endpoints de alunos estão em [requests.http](requests.http). O [Maven Wrapper](.mvn/wrapper/maven-wrapper.properties) está disponível; as evidências foram obtidas com Maven instalado.

Para executar o Python ou reproduzir interrupção, recuperação e timeout, siga o [laboratório integrado D3](docs/evidencias/desafio-3/README.md#reprodução). Para habilitar o laboratório de notificações e comparar os modos, siga o [roteiro D4](docs/evidencias/desafio-4/README.md#reprodução). Para IoT, consulte o [roteiro D5](labs/iot/README.md), com as etapas independentes e o ensaio MQTT real reproduzível. Para CI/observabilidade, consulte o [laboratório D6](labs/operacao/README.md). Relatórios Maven ficam em `target/surefire-reports/` e cobertura em `target/site/jacoco/index.html`.

**Limites da validação:** Docker foi utilizado nos labs MQTT e operação D6; Jenkins/Grafana/Prometheus tiveram capturas reais. PostgreSQL/Compose principal e interface Vue no navegador não foram exercitados nesta etapa. Não há teste de carga, comprovação de 100.000 usuários ou controle global de concorrência. O timeout de leitura foi medido; o limite de conexão não foi medido em rede real. Esses resultados não representam uma validação completa de produção.

## Documentação e equipe

- [Registro arquitetural e matriz cumulativa de ASRs](docs/arquitetura/README.md)
- [Diagramas C4](docs/arquitetura/diagramas.md) e [ADRs](docs/adr/)
- [Evidências D1](docs/evidencias/desafio-1/README.md), [D2](docs/evidencias/desafio-2/README.md), [D3](docs/evidencias/desafio-3/README.md), [D4](docs/evidencias/desafio-4/README.md) e [D5](docs/evidencias/desafio-5/README.md)
- [D6: CI e observabilidade](docs/evidencias/desafio-6/README.md) e [síntese para a AF](docs/arquitetura/sintese-af.md)
- [Cobertura e resultados detalhados](docs/evidencias/cobertura/README.md)
- [Auditoria final dos seis desafios](docs/evidencias/auditoria-final-b1/README.md)
- [Auditoria histórica D1/D2](docs/evidencias/auditoria-final/README.md)
- [Histórico AC1: histórias, BDD e evidências TDD/ATDD](docs/ac1/README.md)

**Equipe B1 confirmada:** Felipe, Isadora e Maria Julia, a mesma equipe da [AC1](docs/ac1/README.md#integrantes). Foram preservadas as grafias existentes; o registro original não informa sobrenomes completos ou RAs. A equipe de seis desenvolvedores é uma premissa do case acadêmico.

**Entrega:** [branch `feature/architecture-lab-b1`](https://github.com/MjDutil/DevOps-Project/tree/feature/architecture-lab-b1) no repositório [MjDutil/DevOps-Project](https://github.com/MjDutil/DevOps-Project). D4, D5 e D6 estão consolidados nos commits `59cb7d1`, `a3b350e` e `42128fa`. A publicação foi autorizada exclusivamente nessa branch; sem merge na `main`.
