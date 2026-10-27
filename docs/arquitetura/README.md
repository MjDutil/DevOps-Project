# Architecture Lab B1 — Registro arquitetural cumulativo

**Produto:** Educação Continuada Gamificada, evolução incremental do MVP da AC1.  
**Repositório:** https://github.com/MjDutil/DevOps-Project  
**Equipe registrada no README da AC1:** Felipe, Isadora e Maria Julia (confirmar membros desta entrega B1).

> Atualização D1 (2026-10-27): monólito modular incremental implementado na conclusão de curso; [registro e evidências reais](../evidencias/desafio-1/README.md). Os desafios 2–6 não foram executados neste incremento.

> Nota de honestidade do pacote inicial (estado anterior): este registro reúne **decisões propostas** e um mini-lab de código para D1/D2. As demais evidências marcadas PENDENTES não foram executadas; não devem ser declaradas prontas antes dos testes e prints. O projeto real possui cursos liberados, recompensas no fórum e Premium, mas ainda não tem ranking, badges, serviço de IA ou IoT.

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
| 2 | 02 | Testabilidade | Modificabilidade | Manutenibilidade | Sem reescrita total | [002](../adr/ADR-002.md) | Fake adapter + Java smoke; integração pendente |
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

A migração preserva o legado e torna explícita apenas a fronteira selecionada. Não comprova capacidade para 100.000 usuários, eliminação de concorrência ou modularização completa do Core. O fake e a porta de persistência já existiam no pacote inicial; a comparação de adapters do Desafio 2 continua pendente.

## 3. Alternativas, consequências, ADRs e C4

Os seis ADRs estão em [`docs/adr/`](../adr/). Cada um contém contexto, RF/RNF, no máximo três ASRs, RPC, alternativas, trade-offs e consequências. Diagramas Container, Component e Deployment ficam em [`diagramas.md`](diagramas.md). Os diagramas exibem visão **alvo** e sinalizam componentes planejados; revisar após cada mini-lab.

## 4. Estado atual e evidências, por desafio

| D | O que mostrar ao professor | Artefatos e ação pendente |
|---|---|---|
| 1 | Contrato público do módulo, dependências e preservação funcional | [Evidências D1](../evidencias/desafio-1/README.md), ADR-001 e C4 Container atualizados |
| 2 | Mesma regra com fake x JPA, testes do domínio passando | `ConcluirCursoUseCaseTest.java`; executar testes Maven e integração com banco |
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
  docs/arquitetura/labs/LabDomainSmoke.java
java -cp /tmp/b1-domain LabDomainSmoke
```

Para comparar com o JPA, subir `docker compose up --build -d`, criar aluno via `POST /alunos` com média 8.0 e depois `POST /alunos/{id}/concluir-curso`; confirmar +3 cursos. **Repetir com média 7.0:** não ganha cursos. Registrar `docker compose logs app` e print do Swagger. O comportamento real do banco não foi testado automaticamente nesta preparação.

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

- [ ] Confirmar integrantes B1 e link correto no README de raiz (hoje há placeholder).
- [x] Executar testes Maven D1 e registrar saída, data e commit-base; consultar limites no registro D1.
- [x] Registrar fontes, diffs e dependências antes/depois D1; comprovar regressão HTTP/JPA em H2.
- [ ] Revisar D1 antes de iniciar D2; commit/publicação ainda não realizados.
- [ ] Testar fake/JPA D2 e registrar outputs.
- [ ] Executar e provar FastAPI e fallback D3.
- [ ] Completar e provar comunicação síncrona/assíncrona D4.
- [ ] Completar e provar gateway MQTT D5.
- [ ] Completar Jenkins, Prometheus, Grafana e registrar incidentes D6.
- [ ] Atualizar diagramas do estado real após os testes.
- [ ] Comitar pasta `/docs/arquitetura` e as evidências reais.

**Síntese provisória para a AF:** levar monólito modular com Ports & Adapters incremental e testes; levar a fronteira FastAPI se o fallback estiver comprovado; decidir RabbitMQ, MQTT e automação de entrega a partir dos resultados práticos. Nenhum desses componentes deve ser descrito como operacional antes da evidência.
