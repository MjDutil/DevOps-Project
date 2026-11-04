# Architecture Lab B1 — Educação Continuada Gamificada

Evolução arquitetural de uma plataforma de educação, com decisões justificadas e evidências técnicas para avaliação acadêmica.

[Sobre](#sobre-o-projeto) · [Tecnologias](#tecnologias-utilizadas) · [Arquitetura](#arquitetura) · [Desafios](#desafios) · [Execução](#como-executar-e-testar) · [Documentação e equipe](#documentação-e-equipe)

## Sobre o projeto

Na **AC1**, construímos um MVP de gamificação em Java/Spring Boot e Vue.js. O **B1 — Architecture Lab** evolui esse mesmo produto em seis desafios, analisando como organizar o Core, integrar serviços e preparar sua operação sem reescrever as regras existentes.

**Funcionalidades implementadas:** média acima de 7 libera três cursos; participação elegível no fórum libera um curso; a partir de 12 cursos, o aluno recebe Premium, voucher e três moedas.

**Integração demonstrativa:** o Core consulta recomendação e assistente em uma API Python. O D3 validou comunicação, fallback, timeout de leitura e recuperação; as respostas são demonstrativas, sem ML, RAG ou LLM reais.

O D4 acrescenta **notificação HTTP demonstrativa após commit**, assíncrona em memória, com modo síncrono para comparação. Fica desabilitada por padrão e não oferece entrega durável. Ranking, badges, email real e integração IoT ainda não estão implementados. **D1–D4 estão implementados/validados no escopo dos mini-labs; D5/D6 permanecem pendentes.**

## Tecnologias utilizadas

| Situação | Tecnologias e uso |
|---|---|
| Implementadas no Core | Java 17, Spring Boot, Spring Data JPA e API REST. H2 utilizado nas validações automáticas. |
| Presentes na interface | Vue.js, Vite e Axios. Interface herdada da AC1; navegador não validado nesta etapa B1. |
| Integração demonstrativa validada | Python, FastAPI e Uvicorn, consumidos pelo Core por HTTP/JSON. |
| Testes e build utilizados | Maven, JUnit, Mockito, MockMvc e JaCoCo. |
| Configuradas, sem execução validada nesta etapa | Docker Compose, PostgreSQL, PgAdmin e Nginx. |
| Comunicação demonstrativa D4 | Executor em memória no Core e consumidor HTTP Python padrão; sem broker e sem persistência de eventos. |
| Planejadas para evolução/D5–D6 | MQTT, Jenkins, Prometheus e Grafana. Broker externo somente se surgir necessidade de retenção/reentrega. |

## Arquitetura

O Core é um **monólito modular incremental**. Na conclusão de curso, uma API pública delimita a capacidade e **Ports & Adapters** mantém a regra de domínio independente de Spring e JPA. O serviço Python tem processo próprio e é opcional: sua indisponibilidade não impede as operações essenciais testadas do Core. No D4, o envio de notificações também é opcional e ocorre somente após commit; fila cheia ou falha HTTP não desfazem a conclusão.

```mermaid
flowchart LR
    aluno["Aluno"] --> frontend["Frontend Vue / Nginx"]
    frontend -->|HTTP REST / JSON| core["Core Java / Spring Boot<br/>Monólito modular + Ports & Adapters<br/>Commit da conclusão antes da notificação<br/>Executor em memória: 1 thread e fila limitada"]
    core -->|JPA / JDBC| banco[("Banco relacional<br/>H2 validado; PostgreSQL configurado")]
    core -.->|HTTP REST / JSON opcional| ia["FastAPI Python<br/>D3 demonstrativo preservado"]
    core -->|"HTTP POST /notificacoes / JSON<br/>Assíncrono por padrão quando habilitado<br/>Síncrono para comparação; ambos após commit"| consumidor["Consumidor demonstrativo Python<br/>Processo separado; sem email real<br/>Pode falhar sem desfazer conclusão"]
```

Consulte o [C4 completo](docs/arquitetura/diagramas.md), a [fonte Mermaid atual](docs/evidencias/desafio-4/c4-container.mmd) e o [registro das decisões](docs/arquitetura/README.md). A modularização é parcial; domínio e persistência ainda são compartilhados com fluxos legados.

## Desafios

| Desafio e objetivo | Decisão | Estado real | Documentação e evidências |
|---|---|---|---|
| **D1 — Evolução do Core:** delimitar uma capacidade existente | Monólito modular na conclusão de curso | Implementado; comparação antes/depois e 25 testes na entrega D1 | [ADR-001](docs/adr/ADR-001.md) · [C4 Container](docs/arquitetura/diagramas.md#c4-container--estado-do-desafio-1) · [Evidências](docs/evidencias/desafio-1/README.md) |
| **D2 — Organização interna:** proteger as regras das tecnologias | Ports & Adapters, com contrato comum fake/JPA | Implementado; núcleo isolado e 51 testes na entrega D2 | [ADR-002](docs/adr/ADR-002.md) · [C4 Component](docs/arquitetura/diagramas.md#c4-component--core-estado-do-desafio-2) · [Evidências](docs/evidencias/desafio-2/README.md) |
| **D3 — Serviço Python e IA:** definir fronteira e tratar falhas | FastAPI independente, com timeout e fallback | Mini-lab validado; 18 verificações HTTP reais; sem modelos de IA | [ADR-003](docs/adr/ADR-003.md) · [C4 Container](docs/arquitetura/diagramas.md#c4-container--estado-validado-do-desafio-3) · [Evidências](docs/evidencias/desafio-3/README.md) |
| **D4 — Comunicação e eventos:** preservar conclusão com consumidor indisponível | Notificação pós-commit, assíncrona em memória; REST síncrono para comparação | Mini-lab validado; 25 verificações HTTP reais; sem entrega durável | [ADR-004](docs/adr/ADR-004.md) · [C4 e sequência](docs/arquitetura/diagramas.md#c4-container--desafio-4) · [Evidências](docs/evidencias/desafio-4/README.md) |
| **D5 — IoT e MQTT:** receber eventos de dispositivos | Proposta: MQTT com consumidor/gateway | Pendente; mini-lab e C4 específico ainda não produzidos | [ADR-005 proposto](docs/adr/ADR-005.md) · [Visão futura](docs/arquitetura/diagramas.md#c4-deployment--visão-proposta-para-desafio-6) · [Evidência prevista e pendente](docs/adr/ADR-005.md) |
| **D6 — CI/CD e observabilidade:** construir, entregar e observar | Proposta: Jenkins e métricas Prometheus/Grafana | Pendente; pipeline e monitoramento ainda não implementados | [ADR-006 proposto](docs/adr/ADR-006.md) · [Deployment proposto](docs/arquitetura/diagramas.md#c4-deployment--visão-proposta-para-desafio-6) · [Evidência prevista e pendente](docs/adr/ADR-006.md) |

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

A última execução D4 aprovou **85 testes Maven**, sem falhas, erros ou ignorados. JaCoCo Maven: 97.87% de instruções, 95.45% de branches e 98.54% de linhas. Os **25 checks HTTP reais D4 e 18 checks de regressão D3** são ensaios separados. Veja os [resultados D4](docs/evidencias/desafio-4/README.md) e os [resultados históricos de cobertura](docs/evidencias/cobertura/README.md); os percentuais atuais não incluem a instrumentação do JAR do laboratório.

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

Para executar o Python ou reproduzir interrupção, recuperação e timeout, siga o [laboratório integrado D3](docs/evidencias/desafio-3/README.md#reprodução). Para habilitar o laboratório de notificações e comparar os modos, siga o [roteiro D4](docs/evidencias/desafio-4/README.md#reprodução). Relatórios Maven ficam em `target/surefire-reports/` e cobertura em `target/site/jacoco/index.html`.

**Limites da validação:** PostgreSQL/Docker e navegador não foram exercitados nesta etapa. Não há teste de carga, comprovação de 100.000 usuários ou controle global de concorrência. O timeout de leitura foi medido; o limite de conexão não foi medido em rede real. Esses resultados não representam uma validação completa de produção.

## Documentação e equipe

- [Registro arquitetural e matriz cumulativa de ASRs](docs/arquitetura/README.md)
- [Diagramas C4](docs/arquitetura/diagramas.md) e [ADRs](docs/adr/)
- [Evidências D1](docs/evidencias/desafio-1/README.md), [D2](docs/evidencias/desafio-2/README.md) e [D3](docs/evidencias/desafio-3/README.md) e [D4](docs/evidencias/desafio-4/README.md)
- [Cobertura e resultados detalhados](docs/evidencias/cobertura/README.md)
- [Auditoria histórica D1/D2](docs/evidencias/auditoria-final/README.md)
- [Histórico AC1: histórias, BDD e evidências TDD/ATDD](docs/ac1/README.md)

**Equipe:** Felipe, Isadora e Maria Julia são os integrantes registrados na AC1. A identificação da equipe B1 ainda precisa de confirmação; a equipe de seis desenvolvedores é uma premissa do case acadêmico.

**Repositório:** [MjDutil/DevOps-Project](https://github.com/MjDutil/DevOps-Project). A implementação D4 ainda aguarda commit e publicação autorizados; sua presença local não significa disponibilidade na branch pública. D5/D6 serão desenvolvidos na continuidade do projeto.
