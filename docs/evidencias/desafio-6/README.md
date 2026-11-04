# D6 — CI/CD e observabilidade

Mini-lab executado em 2026-11-04. Resultados abaixo distinguem CI, implantação local, controles negativos e tentativas sem conclusão. Registro original pré-commit, posteriormente consolidado em `42128fa`; a publicação B1 foi autorizada no fechamento. Os resultados históricos não foram reclassificados.

[ADR-006](../../adr/ADR-006.md) · [C4 Deployment](../../arquitetura/diagramas.md#c4-deployment--desafio-6) · [Roteiro reproduzível](../../../labs/operacao/README.md) · [Jenkinsfile](../../../Jenkinsfile).

## Origem e preservação

O [estado inicial Git](git-antes.json) registra a base `a3b350e` (D5). Naquele registro inicial, D6 ainda não tinha commit. D6 foi consolidado em `42128fa`; a publicação B1 foi autorizada posteriormente, sem merge em main. [Estado Docker inicial](docker-antes.json): containers e volumes anteriores devem ser preservados. A conferência final compara fontes/testes históricos com os hashes iniciais.

## Execução

- Primeira tentativa Maven na sandbox bloqueada por acesso ao cache: [log](mvn-tentativa-sandbox.txt).
- Primeiro Maven com acesso ao cache executou 101 testes e identificou uma falha no teste novo: lista vazia ainda expunha health. [Log preservado](mvn-primeira-configuracao.txt). Corrigido com exclusão explícita; nova execução aprovou os 101 testes.
- [Maven completo após correção](mvn-clean-verify.txt): **101 testes, zero falhas/erros/ignorados**, [resumo e cobertura](testes-locais-resumo.json).
- [14 testes Python aprovados](python-tests.txt).
- [Construção da imagem Jenkins](jenkins-image-build.txt), [imagens fixadas](../../../labs/operacao/images.lock.json) e [validação/renderização Mermaid](mermaid.txt).

CI e experimento reais concluídos conforme os registros abaixo. Os testes locais são contados separadamente dos builds Jenkins.

## Tentativas e diagnóstico

O build Jenkins #1 aprovou 115 testes (101 Java + 14 Python) e seu JAR teve o hash verificado no Core. O #2 falhou antes dos testes porque a proteção do gerador usava `JENKINS_URL`, ausente na instalação local; **não conta como bloqueio por teste JUnit**. [Console #1](primeira-ci/jenkins-aprovado-console.txt), [console #2](primeira-ci/jenkins-bloqueado-console.txt) e [manifesto #1](primeira-ci/artefato-aprovado.json). A proteção foi corrigida para conferir o workspace Jenkins.

A atualização inicial do diretório do snapshot invalidou a montagem somente leitura; foi necessário recriar apenas o container Jenkins deste laboratório, preservando o volume e o histórico. O atualizador agora preserva o diretório montado e troca somente seu conteúdo, com o Jenkins ocioso. Um reacionamento também identificou que jobs já parametrizados exigem a rota `buildWithParameters`; o acionador foi corrigido. [Log](ci-reacionamento-tentativa.txt).

Uma regressão HTTP adicional D3 excedeu a prontidão de 90 segundos antes de qualquer cenário; não foi aprovada e D4 não chegou a iniciar nessa tentativa. [Resultado](regressao-d3-tentativa-inicial/resultados-lab.json) e [log](regressoes-tentativa-inicial.txt). Os testes Maven de regressão passaram separadamente. A primeira captura Chrome excedeu seu prazo de lançamento de 30 segundos; [log](captura-inicial.txt), sem evidência visual válida nessa tentativa.

## Pipeline real

| Execução Jenkins | Resultado real | Significado |
|---|---|---|
| #1 | SUCCESS; 101 Maven + 14 Python, zero falhas | JAR aprovado, manifesto e relatórios JUnit/JaCoCo arquivados |
| #2 | FAILURE antes de JUnit | Proteção inicial do gerador; não é o controle negativo solicitado |
| #3 | ABORTED pelo operador do laboratório | Repetição redundante da suíte já aprovada, encerrada durante inicialização lenta do Spring; não conta como teste aprovado ou falha de regra |
| #4 | FAILURE; 1 JUnit, 1 falha esperada | `PipelineBloqueioTest.deveBloquearPromocaoDoArtefato` falha; Python e aprovação pulados, sem JAR aprovado |
| #5 | SUCCESS; 101 Maven + 14 Python, zero falhas | Única tentativa de fechamento; configuração final e hash do Jenkinsfile conferidos, JAR arquivado sem nova implantação |

[Resultados](pipeline-resultados.json), [positivo JSON](jenkins-aprovado.json), [JUnit positivo](jenkins-aprovado-junit.json), [console positivo](jenkins-aprovado-console.txt), [controle negativo JSON](jenkins-bloqueado.json), [JUnit negativo](jenkins-bloqueado-junit.json) e [console negativo](jenkins-bloqueado-console.txt). [Captura positiva](jenkins-aprovado.png) e [captura negativa](jenkins-bloqueado.png) são telas reais; a segunda conserva o rótulo com problema de codificação da submissão XML, corrigido no acionador posterior, sem editar a evidência.

O fluxo normal completo foi executado na configuração inicial do Jenkinsfile. A evolução restringiu o modo de simulação ao JUnit negativo isolado, sem mudar os comandos normais Maven/Python ou os fontes Core. [Comparação dos snapshots](comparacao-snapshots.json): produção, testes e POM idênticos. Os manifestos [aprovado](snapshot-aprovado.json) e [negativo](snapshot-bloqueado.json) identificam as diferenças de documentação/roteiros/pipeline. A injeção é uma variação controlada posterior à conferência do snapshot, somente no workspace. Não há teste artificial nos fontes originais.

[Versões reais](versoes-jenkins.txt), [plugins](plugins-versoes.json), [isolamento sem Docker socket](jenkins-isolamento.json) e [reconstrução com plugins fixados](jenkins-image-pinned-build.txt). O controller usa Java 21, o console #1 confirma JDK 17/Maven na etapa de build. O executor local do controller não é uma arquitetura de produção para código não confiável.

## Artefato e implantação

O JAR do **build #1** tem SHA-256 `123d9fa6e289735b0d6607e26ef4cb485347c7d2697db31f20dc71042745cdf1`. [Manifesto](artefato-aprovado.json) e [conferência dentro do Core](implantacao-verificada.json) comprovam os mesmos bytes no Jenkins, contexto e container. O Dockerfile de runtime não compila fontes. Jenkins não acessa Docker; o operador executa a implantação local. Não é CD de produção.

A repetição #3 foi abortada e continua inconclusiva. No fechamento, o **build #5 aprovou o fluxo normal final** em uma única tentativa: [resultado](validacao-final/resultado.json), [console](validacao-final/console.txt), [JUnit](validacao-final/junit.json), [snapshot](validacao-final/snapshot.json) e [manifesto do JAR #5](validacao-final/artefato.json). Esse novo JAR não foi implantado; o experimento e a conferência dentro do Core permanecem associados ao #1. Não atribuir ao #3 os resultados de #1 ou #5.

## Experimento real Prometheus/Grafana

| Fase | Recomendação | Assistente | Coleta Core |
|---|---|---|---|
| Inicial, FastAPI disponível | 0 | 0 | up = 1 |
| FastAPI interrompido, 3 + 2 chamadas degradadas | 3 | 2 | up = 1 |
| FastAPI recuperado, 3 chamadas normais por operação | 3 | 2 | up = 1 |

[Resumo das três fases](experimento-resumo.json), [16 respostas HTTP reais](http-experimento.json), [fase inicial](metricas-inicial.json), [fase indisponível](metricas-ia-indisponivel.json), [recuperação](metricas-recuperada.json), [log do experimento](experimento.txt) e [Core sem reinício](core-sem-reinicio.json). A criação/conclusão de aluno retornou saldo de 0 para 3 e Premium continuou acessível com IA indisponível. Os eventos IoT e as notificações continuaram desabilitados neste perfil; suas regressões são cobertas pelos testes Maven existentes, sem atribuir novos ensaios MQTT ao D6.

Consultas reais: `up{job="core"}`, `ia_fallback_total`, `sum(ia_fallback_total)` e `http_server_requests_seconds_count`. O contador possui somente `operacao` como etiqueta definida pela aplicação, além de job/instance do scrape. Não contém aluno/pergunta. O `up` mede coleta, enquanto as respostas de fallback continuam HTTP 200.

- [Grafana durante a falha](grafana-ia-indisponivel.png) e [consultas/respostas reais do painel](grafana-consultas-ia-indisponivel.json).
- [Prometheus durante a falha](prometheus-ia-indisponivel.png).
- [Grafana recuperado](grafana-recuperada.png) e [consultas/respostas após recuperação](grafana-consultas-recuperada.json).
- [Prometheus recuperado](prometheus-recuperada.png), [targets](prometheus-targets.json) e [dashboard consultado pela API](grafana-dashboard-real.json).
- [Versão Prometheus](prometheus-versao.json), [versão Grafana](grafana-versao.json), [logs dos serviços](servicos-logs.txt) e [prontidão/restrição da porta pública](prontidao-operacional.json).

As imagens são capturas de interfaces reais, inspecionadas visualmente. O capturador aguarda valores/legendas, usa viewport fixo e refresh de um minuto durante a captura para evitar registrar somente carregamento; o dashboard provisionado continua com refresh de cinco segundos. A primeira imagem incompleta foi substituída por nova captura da UI, sem editar pixels/dados. A lacuna anterior no gráfico de coleta corresponde à pausa deliberada dos serviços para executar CI, **antes** do experimento; as três consultas de fase retornam up = 1.

## Auditoria pelos cinco critérios oficiais

Avaliação de potencial, sem pontuação definitiva:

| Critério | Potencial | Comprovação e pendência |
|---|---|---|
| RF/RNF e até três ASRs | ATENDIDO | [Matriz cumulativa](../../arquitetura/README.md#28-desafio-6--ci-e-observabilidade), [ADR-006](../../adr/ADR-006.md): testabilidade, observabilidade e confiabilidade |
| RPCs, alternativas e trade-offs | ATENDIDO | ADR-006 compara pipeline único/independentes e logs/métricas; restrições Jenkins/Java/rede/contratos |
| ADR e justificativa arquitetural | ATENDIDO | ADR-006, resultado de fallback HTTP 200 e identidade SHA-256 do artefato |
| C4 coerente com a decisão | ATENDIDO | [Deployment](c4-deployment.svg), [fonte](c4-deployment.mmd), [parser dos nove diagramas](mermaid-todos.txt) |
| Mini-lab, evidências técnicas e Git | ATENDIDO no pacote local | CI #1/#4/#5, consultas/capturas e JAR idêntico comprovados; fechamento local autorizado. Publicação autorizada na branch B1 |

O mini-lab obrigatório foi demonstrado e a revisão técnica preliminar aprovada. A configuração final foi validada pelo #5; o #3 continua abortado no histórico. A [auditoria final dos seis desafios](../auditoria-final-b1/README.md) registra o fechamento local, a equipe confirmada e a autorização de publicação da entrega B1. O laboratório não representa CD de produção, teste de carga, HA, SLA, alertas ou recuperação de dados. H2 e contadores são voláteis; métricas históricas residem no volume Prometheus com retenção de um dia.

A regressão HTTP adicional D3 não chegou aos cenários nesta tentativa; isso não é registrado como aprovação. As 96 regressões Maven anteriores passaram junto com cinco testes novos de instrumentação/exposição, totalizando 101. O experimento D6 voltou a comprovar comunicação, falha, operação essencial e recuperação IA com o JAR aprovado. As evidências anteriores D1–D5 não foram sobrescritas.

## Fechamento e revisão

[Fontes atuais idênticos aos aprovados](fontes-atuais-vs-aprovados.json), [preservação/segredos](auditoria-preservacao.json), [Git final](git-status-final.txt), [lista completa dos arquivos D6](arquivos-d6.txt), [diff --check](git-diff-check.txt), [links/âncoras/SVGs](validacao-documental.txt) e [preservação Docker](docker-preservacao-final.json).

Os 46 arquivos históricos sem alteração incluem domínio, módulos, persistência, endpoints e testes anteriores. Os dois arquivos históricos alterados são `IaClient.java` (somente instrumentação) e `application.properties` (bloqueio de gerenciamento). Dependências autorizadas foram acrescentadas ao POM; cinco testes novos cobrem a instrumentação e a restrição de exposição. As evidências D1–D5 não foram reescritas.

A busca por valores das credenciais geradas e chaves privadas nos arquivos publicáveis não encontrou vazamento; credenciais permanecem nos diretórios `.runtime/` ignorados. Senhas fixas de testes são demonstrativas, não credenciais geradas do laboratório. Logs D6 tiveram espaços finais removidos e caminhos pessoais do repositório substituídos por `<REPO>` no fechamento, preservando resultados. Arquivos arquivados pelo Jenkins, hashes de fontes, JAR e imagens não foram alterados.

Os serviços D6 foram pausados após as capturas; containers e volumes próprios foram mantidos. Nenhum container/volume anterior foi removido, iniciado ou modificado. Para rever o painel, use o roteiro e inicie os serviços deste projeto, consultando suas portas/credenciais locais. Reiniciar Core zera seus contadores/H2; Prometheus conserva as amostras anteriores no volume, respeitando retenção de um dia. Jenkins conserva builds/relatórios no seu volume.

Equipe B1 confirmada: Felipe, Isadora e Maria Julia, conforme AC1. O fechamento local D6 foi concluído em `42128fa`; a publicação da entrega foi autorizada exclusivamente na branch B1. Merge em main não foi autorizado. A [síntese para a AF](../../arquitetura/sintese-af.md) mantém limites dos seis desafios. Os registros anteriores de Git desta pasta são fotografias pré-commit da implementação; o fechamento local é registrado na auditoria final. A execução original ocorreu sem push/merge; a autorização posterior de publicação não altera os resultados históricos.

## Fechamento final

[Comparação exata Jenkinsfile #1/final](../auditoria-final-b1/jenkinsfile-1-vs-final.diff), [conferência por SHA-256](../auditoria-final-b1/comparacao-pipeline.json) e [histórico #1–#5 conferido via API](../auditoria-final-b1/historico-jenkins.json). O código original do Jenkinsfile #1 foi reconstruído e seu SHA conferido contra o manifesto arquivado; a comparação não depende de lembrança do plano. O #5 executou o fluxo normal completo com a lista final de plugins fixada. Nenhum teste artificial foi adicionado a `src/test` local.
