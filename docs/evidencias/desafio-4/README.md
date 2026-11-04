# Evidências D4 — Comunicação e Eventos

Data: 2026-11-04. Base: `8986178`, branch `feature/architecture-lab-b1`, árvore limpa antes da implementação. [Git anterior](git-antes.json) e [preservação](preservacao.json). Nenhum commit, push ou merge realizado nesta etapa.

## Hipótese e critérios oficiais

Fonte: `Exercicio_B1_Architecture_Lab_6_Desafios_Reavaliado.docx`, em Downloads. SHA-256 de `word/document.xml`: `9475a86aeb9e0d8c6d3d6e1b8c9c51cdd1a7b5e0c0433981b279c10f00898d05`, mesmo enunciado D1–D3. O D4 exige interação síncrona e assíncrona, consumidor indisponível, observação do efeito, ADR e C4 Container/fluxo. Não exige broker ou entrega durável.

Hipótese: notificação demonstrativa enviada após commit pode falhar, aguardar timeout ou ser descartada por saturação sem impedir a conclusão essencial. A execução assíncrona evita esperar pelo HTTP na thread da requisição. O modo síncrono existe para comparação e também captura a falha depois do commit.

| Critério | Evidência |
|---|---|
| RF02/RF05, RNFs e até três ASRs | [ADR-004](../../adr/ADR-004.md): tolerância a falhas, latência, consistência transacional. Demais RNFs analisados, sem quarto ASR. |
| RPCs, alternativas e trade-offs | REST síncrono × assíncrono em memória × broker. Escolha menor para o mini-lab; recuperar eventos exigiria nova decisão. |
| Implementação | [Fachada](../../../src/main/java/org/example/tdd/gamificacao/infrastructure/ConclusaoCursoFacade.java), [evento puro](../../../src/main/java/org/example/tdd/gamificacao/api/ConclusaoCursoProcessada.java), [listener](../../../src/main/java/org/example/tdd/notificacoes/NotificacaoConclusaoListener.java), [configuração](../../../src/main/java/org/example/tdd/notificacoes/NotificacoesConfiguration.java), [cliente HTTP](../../../src/main/java/org/example/tdd/notificacoes/HttpNotificadorConclusao.java). |
| C4 | [Container fonte](c4-container.mmd) / [SVG](c4-container.svg), [sequência fonte](sequencia.mmd) / [SVG](sequencia.svg), [visão cumulativa](../../arquitetura/diagramas.md#c4-container--desafio-4). |
| Mini-lab real | [Roteiro](executar-lab.py), [consumidor](consumidor.py), [25 respostas/checks](execucao-http.txt), [JSON](resultados-http.json), logs [síncrono Core](sincrono-core.txt) / [consumidor](sincrono-consumidor.txt), [assíncrono Core](assincrono-core.txt) / [consumidor](assincrono-consumidor.txt). |
| Regressão | [Maven completo](mvn-clean-verify.txt), [resumo extraído](resumo-testes.json), [núcleo JDK](nucleo-isolado.txt), [fronteiras](fronteiras.txt), [regressão HTTP D3](regressao-d3.txt) / [JSON](regressao-d3/resultados-lab.json). |

## Resultados efetivamente obtidos

Ambiente: [versões Java/Maven/Python](ambiente.txt). H2 em memória; processos Spring e consumidor Python separados, em portas livres de loopback. Sem Docker, PostgreSQL ou infraestrutura externa.

| Execução | Resultado |
|---|---|
| Testes direcionados D4 | **10 testes aprovados**, zero falhas/erros/ignorados; [log](testes-direcionados.txt). |
| `mvn clean verify` | **85 testes aprovados**, zero falhas/erros/ignorados; BUILD SUCCESS, 31,433 s. Os 75 testes anteriores foram preservados. |
| HTTP real D4 | **25 verificações aprovadas**, nos dois modos. Não somadas à contagem Surefire. |
| Consumidor lento por 3 s | Síncrono: conclusão 200 em **1.210,58 ms**, falha de leitura registrada. Assíncrono: conclusão 200 em **8,27 ms**, falha posterior registrada no worker. Ambos mantiveram saldo lido por outra requisição. |
| Consumidor parado | Conclusão 200 em **8,24 ms** (síncrono) e **6,51 ms** (assíncrono); `FALHA` registrada; saldo preservado. |
| Gate do consumidor | Modo síncrono comprovadamente pendente até liberação do gate. Modo assíncrono respondeu com consumidor ainda bloqueado. O teste usa estado/sinalização, não somente comparação de tempos. |
| Fila limitada a 1 no laboratório | Com worker bloqueado, segundo evento enfileirado e terceiro descartado; conclusão do terceiro passou em **7,67 ms**, saldo 15; somente dois dos três eventos chegaram ao consumidor. |
| Recuperação do consumidor | Após registrar falha e reiniciar o consumidor, novo evento entregue; estado observado não continha replay do evento falhado. |
| Regressão HTTP D3 | **18 verificações aprovadas**, incluindo FastAPI parado/recuperado e timeout de leitura. Evidências anteriores D3 preservadas; novos logs em `regressao-d3/`. |
| Isolamento D2 e fronteira D1 | Compilação/execução JDK e bytecode somente `java.base`; imports externos somente API pública; ambos passaram. |

Os tempos são observações pontuais, não SLA, benchmark ou teste de carga. O timeout do cliente é de conexão 700 ms e leitura 1.200 ms por fase; o limite real de conexão não foi medido.

### Prova transacional e saturação automatizadas

[NotificacoesIntegrationTest](../../../src/test/java/org/example/tdd/notificacoes/NotificacoesIntegrationTest.java) tem sete testes: envio após commit externo, rollback sem envio, sucesso na thread dedicada, aluno ausente sem evento, média sete sem bônus, falha assíncrona e saturação. Usa H2, consultas JDBC, consumidor controlado e latches, sem sleeps para coordenar a fila. O teste de rollback verifica saldo zero após rollback e ausência de tentativa depois de drenar o executor.

[NotificacoesSincronasIntegrationTest](../../../src/test/java/org/example/tdd/notificacoes/NotificacoesSincronasIntegrationTest.java) tem dois testes: consumidor chamado na thread original e falha capturada sem erro da conclusão, com saldo consultado por JDBC. [NotificacoesDesabilitadasTest](../../../src/test/java/org/example/tdd/notificacoes/NotificacoesDesabilitadasTest.java) confirma que a configuração padrão não cria cliente, listener nem executor.

Esses testes transacionais usam consumidor Java controlado para coordenar comportamento; o laboratório separado exercita o cliente HTTP real e o consumidor Python. Rollback foi validado na integração Spring/JPA, não por um endpoint HTTP artificial de laboratório. Nenhum endpoint novo de controle foi introduzido no Core.

### Cobertura Maven atual

JaCoCo: instruções **875/894 — 97,87%**, branches **21/22 — 95,45%**, linhas **202/205 — 98,54%**, métodos **74/75**. O percentual global mudou porque há código novo; não se manteve a alegação histórica de 100%. O cliente HTTP novo é exercitado no JAR do laboratório, que não foi instrumentado; seus envios não incrementam os contadores Maven. Não foram adicionadas exclusões ou meta artificial de cobertura. [Contadores](resumo-testes.json).

## Reprodução

Na raiz do repositório, JDK 17, Maven e Python 3 (3.9.6 usado no ensaio):

```bash
mvn clean verify
python3 docs/evidencias/desafio-4/executar-lab.py
sh docs/evidencias/desafio-2/verificar-nucleo.sh
python3 docs/evidencias/desafio-1/verificar-fronteiras.py
```

O roteiro inicia processos próprios, configura H2 descartável, habilita notificações e testa ambos os modos com fila de capacidade 1. Encerra seus processos no bloco de limpeza. Regrava os logs e o JSON D4; para capturar também a saída nominal, usar `> docs/evidencias/desafio-4/execucao-http.txt 2>&1`. Os resultados existentes são um snapshot da execução descrita acima.

Para execução manual no macOS, em terminais separados:

```bash
# Consumidor demonstrativo, exclusivamente local
python3 docs/evidencias/desafio-4/consumidor.py --port 8091

# Core: padrão assíncrono quando habilitado
NOTIFICACOES_HABILITADAS=true NOTIFICACOES_URL=http://127.0.0.1:8091 mvn spring-boot:run
# Para comparar, substituir pela execução:
NOTIFICACOES_HABILITADAS=true NOTIFICACOES_MODO=sincrono NOTIFICACOES_URL=http://127.0.0.1:8091 mvn spring-boot:run
```

Use os endpoints de [requests.http](../../../requests.http). Pare o processo consumidor e conclua novamente; consulte `/alunos/{id}/verificar-premium` para observar o saldo por outra requisição. O consumidor oferece `/estado` e `/controle?bloquear=1`, `?liberar=1`, `?atraso=3` apenas para o laboratório local; esses controles não existem no Core.

Configuração: `NOTIFICACOES_HABILITADAS=false` por padrão; `NOTIFICACOES_MODO=assincrono`; `NOTIFICACOES_URL=http://127.0.0.1:8091`; `NOTIFICACOES_FILA_CAPACIDADE=16`. O pool tem uma thread; capacidade deve ser positiva. Não há necessidade de instalar pacote Python para D4.

## Garantias e limitações

- Melhor esforço em memória: sem persistência, retry deliberado, reentrega garantida, replay ou recuperação automática. Crash entre commit e agendamento pode perder evento; encerramento/reinício perde tarefas pendentes. Reinício do Core com fila pendente não foi simulado neste ensaio; a limitação decorre da implementação volátil.
- UUID é correlação, não deduplicação. Timeout pode ocorrer após recebimento/efeito remoto; não se promete exatamente uma vez nem ausência de duplicatas.
- `ENTREGUE` confirma somente HTTP de sucesso. Não há email, analytics durável ou histórico de conclusão de curso específico. O evento retrata processamento da operação e o saldo, sem `cursoId`.
- Notificações desabilitadas não fazem chamadas externas nem criam executor. Ao habilitar, falhas HTTP e rejeições esperadas são tratadas; não se promete resiliência a erro fatal da JVM ou falha do banco.
- `shutdownNow` não garante drenagem. Fila cheia descarta com log sem CallerRunsPolicy; não prova capacidade sob carga de produção.
- Não foram executados navegador, PostgreSQL/Compose, broker, D5 ou D6. Não há garantias globais de concorrência/idempotência para chamadas repetidas de conclusão.
- Testes existentes, regra de domínio, caso de uso, porta, adapter JPA e fontes/evidências D3 foram preservados. A única mudança em classe Java anterior foi na fachada para publicar o record; `application.properties` ganhou configuração opcional. Nenhuma dependência Maven foi adicionada.

## Tentativas e validação documental

A [primeira tentativa HTTP](primeira-tentativa-http.txt) falhou porque o consumidor demonstrativo esperava Content-Length e não aceitava corpo chunked enviado pelo Spring. O consumidor foi corrigido para aceitar ambos, sem mudar o cliente/Core para contornar HTTP válido; o roteiro completo foi repetido com sucesso. Os testes de aplicação já haviam passado; o erro era na ferramenta demonstrativa.

Container e sequência foram renderizados com o Mermaid CLI temporário já disponível: [log Container](mermaid-container.txt), [log sequência](mermaid-sequencia.txt). A primeira sequência continha ponto-e-vírgula em nota, rejeitado pelo parser; a [tentativa](primeira-tentativa-mermaid.txt) foi preservada e apenas a pontuação corrigida.

[Verificação documental final](validacao-documentacao.txt) confere links, âncoras, fontes/SVGs e diff. Logs contêm caminhos locais/nome de usuário e dados fictícios de laboratório, sem credenciais externas identificadas. O H2 usa SA e senha vazia. A publicação dos novos arquivos continua pendente de autorização.
