# Diagramas C4 — Architecture Lab B1

**Cuidado:** as visões abaixo descrevem a arquitetura **decidida/planejada**. O sistema atualmente entregue ainda é um MVP e não possui todos os componentes. O Core, frontend e banco existem. Os demais dependem dos laboratórios dos desafios 3–6.

## C4 Container — estado do Desafio 1

```mermaid
flowchart LR
    usuario["Aluno — pessoa"] --> fe["Frontend — Vue / Nginx"]
    fe -->|REST/JSON HTTP| core["Core — Java / Spring Boot
Um processo e um deploy; modularização incremental"]
    core -->|JPA / JDBC| db[("Banco relacional
H2 nos testes; PostgreSQL no Compose")]
    core -.->|HTTP; protótipo anterior, não validado neste desafio| ia["FastAPI — protótipo Python"]
```

Conclusão de curso é uma capacidade interna do Core, sem deploy, rede ou banco exclusivos. A seta para FastAPI registra código herdado do pacote inicial; D1 não valida essa integração. Broker, IoT e observabilidade permanecem apenas na visão futura abaixo.

## Mapa da fatia interna — apoio à leitura do D1

```mermaid
flowchart LR
    http[AlunoController] --> contrato["gamificacao.api.ConclusaoCurso"]
    fachada["ConclusaoCursoFacade — transação"] -. implementa .-> contrato
    fachada --> uc[ConcluirCursoUseCase]
    uc --> dom["domain.Aluno — compartilhado"]
    uc --> porta[AlunoStore]
    jpa[JpaAlunoStore] -. implementa .-> porta
    jpa --> repo["AlunoRepository / AlunoEntity — compartilhados"]
    repo --> db[(Banco relacional)]
    http --> legado["AlunoService — criação, fórum, Premium"]
    legado --> repo
    legado --> dom
```

Este mapa atualiza a documentação para corresponder ao código D1; o aprofundamento do C4 Component e da substituição de adapters continua no Desafio 2.

## C4 Deployment — visão proposta para desafio 6

```mermaid
flowchart TB
    j[Jenkins — pipeline a construir] -.-> core
    subgraph d[Ambiente Docker Compose]
       fe[Frontend]
       core[Core Spring Boot]
       pg[(PostgreSQL)]
       py[FastAPI — planejado]
       mqtt[MQTT — planejado]
       prom[Prometheus — planejado]
       graf[Grafana — planejado]
       fe --> core
       core --> pg
       core --> py
       mqtt -.-> core
       prom -.->|scrape| core
       graf --> prom
    end
```

A visão Deployment acima é proposta futura e não evidencia execução dos desafios 3–6. Nos diagramas anteriores, os rótulos distinguem implementação de integração não validada; setas “implementa” representam implementação de interface, não comunicação remota.
