# Auditoria final — Architecture Lab B1, D1 a D6

Revisão contra o enunciado oficial `Exercicio_B1_Architecture_Lab_6_Desafios_Reavaliado.docx`. [Identificação e SHA do conteúdo oficial](fonte-oficial.json). Avaliação de potencial de atendimento, **sem atribuir nota definitiva**. O escopo é o mini-lab arquitetural de cada desafio; não se declara implementação integral de ranking, badges, ML/RAG, email ou operação de produção.

A [lista dos arquivos preparados](arquivos-preparados.txt) registra o conteúdo revisto para o commit.

O fechamento local do D6 foi autorizado na branch `feature/architecture-lab-b1`. Esta auditoria integra esse pacote de commit; o hash efetivo deve ser consultado em `git log`. Publicação remota não foi autorizada. Os resultados históricos são associados à execução original, sem tratá-los como testes novamente executados nesta auditoria.

## Matriz dos cinco critérios oficiais

| Desafio | RF/RNF e até 3 ASRs | RPCs, alternativas/trade-offs | ADR e justificativa | C4 coerente | Mini-lab, evidências e Git local |
|---|---|---|---|---|---|
| D1 | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO |
| D2 | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO |
| D3 | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO |
| D4 | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO |
| D5 | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO |
| D6 | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO | ATENDIDO no fechamento local |

Git local não equivale a entrega publicada: D4, D5 e o commit D6 deste pacote precisam ser enviados à branch remota, após autorização. Enquanto isso, o link remoto não contém a entrega completa. A identificação da equipe também continua pendente de confirmação.

## Caminhos comprobatórios por desafio

O [registro cumulativo](../../arquitetura/README.md) contém RF → RNF → ASR → RPC → alternativas → ADR → C4 → evidência e a matriz com três ASRs por desafio. Os ADRs abaixo comprovam, individualmente, os três primeiros critérios da tabela; cada um contém requisitos, ASRs, restrições, alternativas e consequências.

| Desafio | RF/RNF/ASRs, RPCs, alternativas e decisão | C4 | Código, testes e evidências que sustentam o mini-lab |
|---|---|---|---|
| D1 | [ADR-001](../../adr/ADR-001.md): camadas × monólito modular × serviços | [Container D1](../../arquitetura/diagramas.md#c4-container--estado-do-desafio-1) | [API pública](../../../src/main/java/org/example/tdd/gamificacao/api/ConclusaoCurso.java), [controller](../../../src/main/java/org/example/tdd/controller/AlunoController.java), [diff antes/depois](../desafio-1/refatoracao-d1.diff), [dependências](../desafio-1/dependencias-antes.txt), [25 testes originais](../desafio-1/resumo-testes.txt) e [mini-lab](../desafio-1/README.md) |
| D2 | [ADR-002](../../adr/ADR-002.md): manter fatia parcial × Ports & Adapters × migração global | [Component D2](../../arquitetura/diagramas.md#c4-component--core-estado-do-desafio-2) | [Caso de uso puro](../../../src/main/java/org/example/tdd/gamificacao/application/ConcluirCursoUseCase.java), [porta](../../../src/main/java/org/example/tdd/gamificacao/application/AlunoStore.java), [JPA](../../../src/main/java/org/example/tdd/gamificacao/infrastructure/JpaAlunoStore.java), [contrato comum](../../../src/test/java/org/example/tdd/gamificacao/AlunoStoreContract.java), [núcleo JDK](../desafio-2/nucleo-isolado.txt), [51 testes originais](../desafio-2/resumo-testes.txt) e [mini-lab](../desafio-2/README.md) |
| D3 | [ADR-003](../../adr/ADR-003.md): IA embutida × módulo acoplado × serviço Python | [Container D3](../../arquitetura/diagramas.md#c4-container--estado-validado-do-desafio-3) | [Cliente com timeout/fallback](../../../src/main/java/org/example/tdd/ia/IaClient.java), [FastAPI](../../../labs/ia/main.py), [18 cenários reais](../desafio-3/resultados-lab.json), [roteiro e limites](../desafio-3/README.md) |
| D4 | [ADR-004](../../adr/ADR-004.md): REST × memória × broker externo justificado | [Container/sequência D4](../../arquitetura/diagramas.md#c4-container--desafio-4) | [Fachada transacional](../../../src/main/java/org/example/tdd/gamificacao/infrastructure/ConclusaoCursoFacade.java), [AFTER_COMMIT](../../../src/main/java/org/example/tdd/notificacoes/NotificacaoConclusaoListener.java), [fila limitada](../../../src/main/java/org/example/tdd/notificacoes/NotificacoesConfiguration.java), [rollback/falha/saturação](../../../src/test/java/org/example/tdd/notificacoes/NotificacoesIntegrationTest.java), [25 cenários HTTP](../desafio-4/resultados-http.json) e [mini-lab](../desafio-4/README.md) |
| D5 | [ADR-005](../../adr/ADR-005.md): REST × MQTT/gateway × mensageria adicional | [Container/fluxo D5](../../arquitetura/diagramas.md#c4-container--desafio-5) | [Gateway e ACK](../../../labs/iot/gateway.py), [broker/ACL](../../../labs/iot/acl), [deduplicação volátil](../../../src/main/java/org/example/tdd/iot/RegistroEventosIot.java), [15 cenários MQTT reais](../desafio-5/resultados-mqtt.json), [12 HTTP](../desafio-5/resultados-http.json), [auditoria ACK/PUBACK](../desafio-5/auditoria-mqtt.txt) e [mini-lab](../desafio-5/README.md) |
| D6 | [ADR-006](../../adr/ADR-006.md): pipeline único × independente; logs × métricas | [Deployment D6](../../arquitetura/diagramas.md#c4-deployment--desafio-6) | [Jenkinsfile](../../../Jenkinsfile), [infraestrutura/roteiro](../../../labs/operacao/README.md), [build final #5](../desafio-6/validacao-final/resultado.json), [JUnit #5](../desafio-6/validacao-final/junit.json), [controle negativo #4](../desafio-6/jenkins-bloqueado-junit.json), [JAR implantado #1](../desafio-6/implantacao-verificada.json), [métricas/falha/recuperação](../desafio-6/experimento-resumo.json), [Grafana](../desafio-6/grafana-recuperada.png), [Prometheus](../desafio-6/prometheus-recuperada.png) e [mini-lab](../desafio-6/README.md) |

A [inspeção de fronteiras repetida nesta auditoria](fronteiras.txt) confirma que clientes externos importam somente a API do módulo e que domínio/aplicação/API não importam Spring/JPA/repository/entity/infraestrutura. A [conferência dos JSONs históricos](evidencias-conferidas.json) verificou flags individuais de sucesso e rastreamento Git, sem inferir aprovação a partir de README.

## Fechamento do pipeline e preservação

[Diff exato do Jenkinsfile #1/final](jenkinsfile-1-vs-final.diff), [SHA do original reconstruído](comparacao-pipeline.json) e [histórico #1–#5 consultado no Jenkins](historico-jenkins.json). A única diferença de fluxo é executar somente o JUnit artificial no modo de simulação; o modo normal mantém Maven completo e Python. O #5 testou efetivamente a configuração final em uma única tentativa.

| Build | Resultado | Classificação correta |
|---|---|---|
| #1 | SUCCESS — 101 Java + 14 Python | Positivo original; JAR usado na implantação e no experimento |
| #2 | FAILURE antes de JUnit | Falha do gerador inicial; não é evidência de bloqueio por teste |
| #3 | ABORTED | Repetição encerrada por lentidão; não é aprovação nem falha de regra |
| #4 | FAILURE — um JUnit, uma falha esperada | Controle negativo válido; não aprova JAR |
| #5 | SUCCESS — 101 Java + 14 Python | Positivo final; JAR arquivado/conferido, sem nova implantação |

O JAR #5 tem hash diferente do #1 porque é outro build. Não se afirma que ele foi implantado. A demonstração da identidade aprovado/implantado e do fallback continua vinculada ao #1, com fontes de negócio idênticos. Os dois manifestos registram o snapshot e o commit-base anterior ao fechamento; a documentação posterior à CI não é apresentada como parte daquele snapshot.

## Limites reais dos seis mini-labs

- D1: modularização parcial por pacotes, sem prova de 100.000 usuários ou ranking implementado.
- D2: núcleo isolado e contrato comum comprovados; JPA/H2 não substitui ensaio PostgreSQL/carga/concorrência global.
- D3: IA demonstrativa, sem ML/RAG real; timeout de leitura observado, limite de conexão configurado sem medição real equivalente.
- D4: operação confirmada não depende do consumidor; eventos/fila em memória podem se perder, sem retry/replay durável ou histórico de curso específico.
- D5: MQTT/QoS 1/sessão/ACK observados com autenticação/ACL; deduplicação limitada ao processo/janela, sem exatamente uma vez ou TLS de produção.
- D6: CI real e implantação local controlada; não CD de produção. `up` mede coleta; contadores/H2 reiniciam com Core. Histórico #1–#4 e tentativas incompletas permanecem distintos do #5 aprovado.

## Documentação, segurança, Git e pendências da entrega

- [README principal](../../../README.md): seis seções, estado real e links por desafio.
- [Registro cumulativo](../../arquitetura/README.md), [ADRs 001–006](../../adr/) e [C4](../../arquitetura/diagramas.md) presentes; [síntese para a PoC AF](../../arquitetura/sintese-af.md) inclui as seis decisões e seus limites.
- Os READMEs de evidências antigos descrevem o momento de cada execução. Termos históricos como “D6 pendente” não substituem o registro cumulativo atual; os ADRs 003–005 receberam somente notas de continuidade/status Git.
- Equipe B1: **PENDENTE** de confirmação. Felipe, Isadora e Maria Julia estão identificados apenas como histórico da AC1; seis desenvolvedores são premissa do case, não lista real da equipe.
- [Verificação de segurança](seguranca.json), [links relativos](validacao-documental.txt) e [parser Mermaid](mermaid.txt) documentam as conferências. Credenciais geradas, JARs, caches e `.runtime/` ficam fora do índice. Caminhos absolutos pessoais nos logs novos D6 são substituídos por `<REPO>`; os logs históricos D1–D5 conservam sua proveniência e não são links necessários para execução. Credenciais fixas dos exemplos/testes são demonstrativas, conforme documentação histórica.
- Repositório confirmado pelo remote `origin`: [MjDutil/DevOps-Project](https://github.com/MjDutil/DevOps-Project). [Branch correta](https://github.com/MjDutil/DevOps-Project/tree/feature/architecture-lab-b1).
- Consulta remota no fechamento: a branch estava em `8986178`; D4 `59cb7d1`, D5 `a3b350e` e o novo commit local D6 precisam de push autorizado. Nenhum push ou merge foi executado.

Os **seis mini-labs têm evidências técnicas suficientes no escopo oficial**. A entrega final por link GitHub depende de confirmar a equipe e publicar a branch; até isso ocorrer, não está completa no remoto. Os resultados são potencial de atendimento, sem substituir a avaliação do professor.
