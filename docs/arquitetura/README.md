# Architecture Lab B1 — Registro arquitetural cumulativo

**Produto:** Educação Continuada Gamificada, evolução incremental do MVP da AC1.
**Repositório:** https://github.com/MjDutil/DevOps-Project
**Equipe registrada no README da AC1:** Felipe, Isadora e Maria Julia (confirmar membros desta entrega B1).

> Atualização D2 (2026-10-27): D1 consolidado em commits separados de limpeza e implementação. D2 valida Ports & Adapters na conclusão de curso com contrato comum fake/JPA e execução pura sem Spring/banco. [Evidências D1](../evidencias/desafio-1/README.md) e [D2](../evidencias/desafio-2/README.md). Desafios 3–6 não executados nesta etapa.

> Nota de honestidade do pacote inicial (estado anterior): este registro reúne **decisões propostas** e um mini-lab de código para D1/D2. As demais evidências marcadas PENDENTES não foram executadas; não devem ser declaradas prontas antes dos testes e prints. O projeto real possui cursos liberados, recompensas no fórum e Premium, mas ainda não tem ranking, badges, serviço de IA ou IoT.

## Avaliação diretamente pelo README principal

O [README da raiz](../../README.md) apresenta o conteúdo essencial dos Desafios 1 e 2: RF/RNF, três ASRs por desafio, RPCs, alternativas, resumo dos ADRs, diagramas Container/Component e resultados reais. Este arquivo continua sendo o registro cumulativo dos seis desafios, conforme o enunciado. A [revisão editorial](../evidencias/revisao-readme/README.md) registra a nova execução dos testes e a validação de links/diagramas. Merge/publicação em `main` aguardam aprovação.

## 1. RFs e RNFs do case

- **RF01 Ranking:** previsto no enunciado; não implementado na AC1 fornecida.
- **RF02 Conquistas:** parcialmente representado por liberação de cursos, bônus de fórum e Premium; badges não implementados.
- **RF03 Recomendação inteligente:** futuro.
- **RF04 Assistente educacional:** futuro.
- **RF05 Notificações/eventos:** futuro.
- **RF06 Receber eventos IoT:** futuro.

RNF/FURPS+: Performance (desempenho/escalabilidade), Reliability (disponibilidade/recuperação), Supportability (manutenção/testabilidade/deploy), Functionality/Security (autorização de dispositivos), Usability (mantida na experiência do aluno) e outras restrições do case.

## 2. Matriz cumulativa de ASRs

| Desafio | RF | ASR 1 | ASR 2 | ASR 3 | RPC principal | ADR | Evidência |
|---|---|---|---|---|---|---|---|
| 1 | 01 futuro, 02 exercitado | Manutenibilidade | Escalabilidade | Complexidade operacional | Spring Boot/6 devs/JPA/legado AC1 | [001](../adr/ADR-001.md) | [Fronteira pública, comparação e testes HTTP/JPA antes/depois](../evidencias/desafio-1/README.md) |
| 2 | 02 exercitado, 01 futuro | Testabilidade | Modificabilidade | Manutenibilidade | Spring Boot, regras reaproveitadas, sem reescrita total | [002](../adr/ADR-002.md) | [Contrato comum fake/JPA, JDK isolado, transação e regressões](../evidencias/desafio-2/README.md) |
| 3 | 03, 04 | Tolerância a falhas | Deploy independente | Escala da IA | Python + Core Java | [003](../adr/ADR-003.md) | FastAPI + cliente REST criados; execução/falha pendentes |
| 4 | 02, 05 | Disponibilidade | Recuperabilidade | Consistência | Core não bloqueia | [004](../adr/ADR-004.md) | PENDENTE |
| 5 | 06 | Comunicação eficiente | Intermitência | Segurança | Hardware limitado | [005](../adr/ADR-005.md) | PENDENTE |
| 6 | 01–06 | Testabilidade | Confiabilidade | Observabilidade | Jenkins, Prometheus, Grafana | [006](../adr/ADR-006.md) | PENDENTE |

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

## 3. Alternativas, consequências, ADRs e C4

Os seis ADRs estão em [`docs/adr/`](../adr/). Cada um contém contexto, RF/RNF, no máximo três ASRs, RPC, alternativas, trade-offs e consequências. Diagramas Container, Component e Deployment ficam em [`diagramas.md`](diagramas.md). Container e Component distinguem o estado implementado; Deployment permanece uma visão **alvo** com componentes planejados.

## 4. Estado atual e evidências, por desafio

| D | O que mostrar ao professor | Artefatos e ação pendente |
|---|---|---|
| 1 | Contrato público do módulo, dependências e preservação funcional | [Evidências D1](../evidencias/desafio-1/README.md), ADR-001 e C4 Container atualizados |
| 2 | Mesma regra e contrato com fake/JPA; sem dependências técnicas no núcleo | [Testes de contrato, execução isolada e integração H2](../evidencias/desafio-2/README.md) |
| 3 | Spring chama FastAPI, derrubar FastAPI, Core mantém serviço essencial | `labs/ia` e cliente `ia/IaClient` criados; falta executar, testar falha e salvar prints |
| 4 | REST síncrono falha x entrega assíncrona preserva conclusão | Implementar consumer/broker e salvar log/prints |
| 5 | Simulador MQTT → broker → consumidor → Core, duplicado não pontua duas vezes | Implementar publisher, gateway, dedupe e capturar mensagens |
| 6 | Jenkins passa/falha e painel Prometheus/Grafana reage a falha induzida | Criar pipeline e métricas, obter screenshots reais |

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

### Mini-lab 3 (integração opcional)

```bash
docker compose up --build -d app ia postgres
curl http://localhost:8080/ia/recomendacoes/1
docker compose stop ia
curl http://localhost:8080/ia/recomendacoes/1
# Esperado: status "indisponivel" e recomendacoes vazias; Core continua respondendo.
# Reativar para o ensaio de recuperação:
docker compose start ia
```

O resultado em Docker ainda não foi observado nesta preparação; o cliente está preparado para demonstrar o comportamento com timeout/fallback.

## 6. Entrega final e plano da AF

- [ ] Confirmar integrantes B1 (o link do GitHub na raiz já foi corrigido).
- [x] Executar testes Maven D1 e registrar saída, data e commit-base; consultar limites no registro D1.
- [x] Registrar fontes, diffs e dependências antes/depois D1; comprovar regressão HTTP/JPA em H2.
- [x] Revisar e consolidar D1: limpeza `a25b2b2` e implementação `8bf8785`; sem push.
- [x] Testar fake/JPA D2, execução JDK isolada e transações; registrar outputs.
- [ ] Executar e provar FastAPI e fallback D3.
- [ ] Completar e provar comunicação síncrona/assíncrona D4.
- [ ] Completar e provar gateway MQTT D5.
- [ ] Completar Jenkins, Prometheus, Grafana e registrar incidentes D6.
- [x] Atualizar Container/Component para o estado D1/D2; Deployment futuro explicitamente identificado.
- [x] Versionar documentação e evidências D1/D2 em commits locais; publicação continua pendente.

**Síntese provisória para a AF:** levar monólito modular com Ports & Adapters incremental e testes; levar a fronteira FastAPI se o fallback estiver comprovado; decidir RabbitMQ, MQTT e automação de entrega a partir dos resultados práticos. Nenhum desses componentes deve ser descrito como operacional antes da evidência.
