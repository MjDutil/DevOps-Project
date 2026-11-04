# Diagramas C4 — Architecture Lab B1

**Cuidado:** as visões abaixo descrevem a arquitetura **decidida/planejada**. O sistema atualmente entregue ainda é um MVP e não possui todos os componentes. O Core, frontend e banco existem; a integração FastAPI foi validada no D3 em rede local/H2. D4 acrescenta notificação demonstrativa pós-commit; D5/D6 continuam propostos.

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

## C4 Container — estado validado do Desafio 3

```mermaid
flowchart LR
    aluno["Aluno — pessoa"] --> frontend["Frontend Vue / Nginx"]
    frontend -->|HTTP REST / JSON| core["Core Java / Spring Boot<br/>Monólito modular + Ports & Adapters<br/>Operações essenciais sem dependência de IA"]
    core -->|JPA / JDBC| banco[("Banco relacional<br/>H2 no laboratório; PostgreSQL configurado")]
    core -->|"HTTP REST / JSON síncrono<br/>conexão 700 ms; leitura 1200 ms<br/>fallback em falha"| ia["Serviço Python / FastAPI<br/>Recomendação + assistente demonstrativos<br/>Processo e reinício independentes; sem ML/RAG real"]
```

O D3 validou Core → FastAPI e Core → H2 em processos locais, incluindo falha, recuperação e timeout de leitura. Frontend/navegador e PostgreSQL/Compose não foram exercitados. Não há acesso do Python ao banco do Core; a recomendação usa somente o ID no contrato demonstrativo. Falha da IA degrada apenas a resposta opcional; conclusão, fórum e Premium continuam locais ao Core. Esta visão evolui o Container D1 sem alterar o Component D2. [Logs, respostas e tempos](../evidencias/desafio-3/README.md).

## C4 Container — Desafio 4

```mermaid
flowchart LR
    aluno["Aluno"] --> frontend["Frontend Vue / Nginx"]
    frontend -->|HTTP REST / JSON| core["Core Java / Spring Boot<br/>Monólito modular + Ports & Adapters<br/>Commit da conclusão antes da notificação<br/>Executor em memória: 1 thread e fila limitada"]
    core -->|JPA / JDBC| banco[("Banco relacional<br/>H2 validado; PostgreSQL configurado")]
    core -.->|HTTP REST / JSON opcional| ia["FastAPI Python<br/>D3 demonstrativo preservado"]
    core -->|"HTTP POST /notificacoes / JSON<br/>Assíncrono por padrão quando habilitado<br/>Síncrono para comparação; ambos após commit"| consumidor["Consumidor demonstrativo Python<br/>Processo separado; sem email real<br/>Pode falhar sem desfazer conclusão"]
```

Notificações são desabilitadas por padrão. Quando habilitadas, o listener após commit usa executor de uma thread e fila limitada dentro do Core; não é um serviço ou broker adicional. O modo síncrono existe somente para comparação. Consumidor Python demonstrativo é processo separado; não é o serviço FastAPI do D3. Frontend/PostgreSQL são componentes existentes/configurados, sem nova validação de navegador/Docker. [Fonte e SVG](../evidencias/desafio-4/README.md).

### Sequência da conclusão e notificação

```mermaid
sequenceDiagram
    participant A as Cliente
    participant C as Core / fachada
    participant B as Banco
    participant L as Listener apos commit
    participant E as Executor em memoria
    participant N as Consumidor HTTP
    A->>C: POST concluir-curso
    C->>B: Ler snapshot e atualizar saldo
    C->>C: Publicar evento transacional
    alt rollback
        C->>B: ROLLBACK
        Note over L,N: Nao encaminhar notificacao
    else commit
        C->>B: COMMIT
        C->>L: AFTER_COMMIT
        alt modo assincrono
            L->>E: Tentar agendar
            alt fila cheia ou executor encerrado
                L->>L: Registrar descarte
            else tarefa aceita
                E->>N: POST notificacoes com timeout
                Note over E,N: Sucesso ou falha registrados na thread dedicada
            end
            C-->>A: HTTP 200 com saldo
        else modo sincrono de laboratorio
            L->>N: POST notificacoes com timeout
            Note over L,N: Aguarda tentativa, captura falha
            C-->>A: HTTP 200 com saldo
        end
    end
    Note over B,E: Sem outbox, falha do processo pode perder o evento apos commit
```

A tentativa em segundo plano pode iniciar antes ou depois da resposta HTTP, mas somente após commit. Não se garante ordem global nem persistência da fila. Rollback e snapshot obsoleto não encaminham evento de sucesso; falhas HTTP e saturação são registradas sem transformar a conclusão confirmada em erro.

## C4 Deployment — visão proposta para desafio 6

```mermaid
flowchart TB
    j[Jenkins — pipeline a construir] -.-> core
    subgraph d[Ambiente Docker Compose]
       fe[Frontend]
       core[Core Spring Boot]
       pg[(PostgreSQL)]
       py[FastAPI — integração local validada no D3]
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

A visão Deployment acima é proposta futura e não evidencia implantação Docker nem execução dos desafios 5–6. Nos diagramas anteriores, os rótulos distinguem implementação de integração não validada; setas “implementa” representam implementação de interface, não comunicação remota.
