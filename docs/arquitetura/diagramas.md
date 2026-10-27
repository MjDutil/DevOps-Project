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

## C4 Component — Core, estado do Desafio 2

```mermaid
flowchart LR
    subgraph core["Core Spring Boot — mesmo processo e deploy"]
        http["AlunoController — adapter HTTP"]
        subgraph modulo["Capacidade de conclusão / gamificacao"]
            entrada["ConclusaoCurso — contrato público de entrada"]
            fachada["ConclusaoCursoFacade — adapter de entrada transacional"]
            uc["ConcluirCursoUseCase — aplicação pura"]
            saida["AlunoStore — porta de saída
buscar / atualizarCursos esperado"]
            jpa["JpaAlunoStore — adapter de saída
escrita exige transação"]
            fachada -. implementa .-> entrada
            fachada --> uc
            uc --> saida
            jpa -. implementa .-> saida
        end
        dom["domain.Aluno — regra pura compartilhada"]
        repo["AlunoRepository / AlunoEntity — infraestrutura compartilhada
UPDATE parcial com pré-condições"]
        legado["AlunoService — criação, fórum, Premium preservados"]
        uc --> dom
        jpa --> repo
        legado --> repo
        legado --> dom
    end
    http --> entrada
    http --> legado
    repo --> db[("H2 nos testes / PostgreSQL configurado")]
    fake["FakeAlunoStore — somente nos testes; sem Spring/banco"] -. implementa .-> saida
```

Setas contínuas representam dependências de uso; “implementa” explicita inversão de dependências. O caso de uso depende da porta; os adapters implementam a porta definida pela aplicação. O fake não é bean nem serviço/container de produção. Na composição real, `GamificacaoConfiguration` injeta `AlunoStore` no caso de uso e o Spring seleciona o adapter JPA.

A chamada pública entra pela fachada transacional; o contrato de saída atualiza somente cursos, rejeitando snapshot obsoleto. Domínio, entidade e repository seguem compartilhados com o legado, sem migração global. A mesma regra foi exercitada com os dois adapters nos [testes D2](../evidencias/desafio-2/README.md). O nível Container continua inalterado: uma aplicação Core, sem novos serviços.

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
