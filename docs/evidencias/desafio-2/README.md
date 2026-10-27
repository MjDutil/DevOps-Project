# Desafio 2 — organização interna com Ports & Adapters

Data: 2026-10-27. Estado: implementação e validação concluídas, aguardando revisão do usuário. Não foi iniciado o Desafio 3 e nenhum push foi realizado.

## Consolidação do Git antes do D2

Branch preservada: `feature/architecture-lab-b1`.

- `a25b2b2`: limpeza do versionamento. Auditoria confirmou exatamente 1.151 dependências em `frontend/node_modules`, três arquivos gerados em `frontend/dist` e dois logs Vite; nenhum fonte, configuração necessária ou evidência removido.
- `8bf8785`: implementação/documentação/evidências D1, incluindo os arquivos B1 preexistentes que precisavam ser incorporados ao histórico. O protótipo IA preexistente foi preservado, sem evolução do D3.
- Antes de alterar D2, `mvn -o verify` reconfirmou os **25 testes do D1**, sem falhas/erros/ignorados, com BUILD SUCCESS.

[Auditoria Git](auditoria-git.txt) e [log da confirmação D1](../desafio-1/verify-confirmacao-pre-d2.txt). Os registros D1 anteriores à consolidação são evidências históricas, não foram reescritos para simular uma execução nova.

## Hipótese e comparação real antes/depois

A hipótese é que o mesmo caso de uso e regra de domínio podem operar com persistência substituível, sem conhecer Spring, JPA ou banco. O D1 já tinha porta e aplicação pura; D2 comprova e aperfeiçoa essa fronteira, sem atribuir a si a criação de toda a estrutura.

| Aspecto | D1 real | D2 |
|---|---|---|
| Regra central | `domain.Aluno`, puro | Exatamente o mesmo arquivo/algoritmo; SHA-256 idêntico |
| Caso de uso | Dependia de `AlunoStore` | Continua dependendo apenas da porta e domínio |
| Porta | `buscar` / `salvar(snapshot)` genérico | `buscar` / `atualizarCursos(esperado, novoSaldo)` com pré-condição explícita |
| Fake | Classe interna no teste do caso de uso | Adapter reutilizável em testes unitários, contrato e smoke somente JDK |
| JPA | Segunda chamada `findById` ao salvar; `save(entity)` | UPDATE parcial condicional, sem segunda busca nem escrita dos demais campos |
| Transação | Fachada `@Transactional` | Fachada preservada; escrita do adapter exige transação existente |
| Prova da substituição | Unitários com fake + regressão HTTP/JPA separada | Mesmos 11 testes de contrato executados com cada adapter |
| Dependências do núcleo | Imports puros inspecionados | Além da inspeção, compilação sem dependências externas e bytecode somente `java.base` |

[Diff de produção](mudancas-producao.diff), [domínio preservado](dominio-preservado.txt), [ADR-002](../../adr/ADR-002.md) e [C4 Component](../../arquitetura/diagramas.md).

## Implementação e inversão de dependências

```text
Controller → ConclusaoCurso (contrato de entrada)
                    ↑ implementa
           ConclusaoCursoFacade (transação Spring)
                    ↓
           ConcluirCursoUseCase → domain.Aluno
                    ↓ usa
                 AlunoStore (porta definida pela aplicação)
                    ↑ implementam
           FakeAlunoStore       JpaAlunoStore → JPA / H2
           somente testes       composição real Spring
```

A substituição ocorre no construtor `new ConcluirCursoUseCase(adapter)`. O contrato de teste é compartilhado por `FakeAlunoStoreContractTest` e `JpaAlunoStoreContractTest`; a implementação central não muda conforme o adapter. O fake está somente em `src/test`, não é bean de produção.

A porta atualiza apenas o saldo de cursos de um aluno existente se ID, média e saldo previamente lidos corresponderem. Não cria aluno. Alteração ou remoção do estado esperado causa `ConcurrentModificationException`, da própria JDK, sem dependência de exceção Spring/JPA.

## Persistência e transação

A segunda chamada `findById` do D1 não demonstrava por si só SELECT duplicado: uma transação pode reutilizar o cache JPA. D2 não inventa uma redução de consultas em relação ao baseline. A mudança elimina a segunda busca do código e restringe a escrita ao campo necessário, usando UPDATE com pré-condições.

`@Transactional` permanece na fachada. `JpaAlunoStore.atualizarCursos` usa propagação `MANDATORY` e rejeita uma escrita sem transação. O query faz flush e limpa o contexto para não manter entidade carregada com saldo obsoleto. O domínio não contém anotações de transação nem conhece JPA.

Nos testes H2, a chamada da fachada carregou **uma entidade** e preparou **dois statements (SELECT + UPDATE)**, medidos por estatísticas Hibernate habilitadas somente no teste. Uma consulta JDBC posterior confirmou o commit. Outros testes confirmaram rollback e rejeição de escrita fora de transação. A regressão D1 verifica preservação de plano, voucher e moedas após nova conclusão.

Conflitos foram simulados por snapshots obsoletos, incluindo saldo e média. Isso não é teste multithread, carga ou garantia de concorrência global. Não há retry automático; controle de concorrência do fórum/Premium e mapeamento HTTP específico de conflitos continuam fora deste escopo. A pré-condição por valores não é versionamento global nem controle de todas as escritas legadas.

## Resultados reais

| Verificação | Resultado |
|---|---|
| D1 antes do D2 — `mvn -o verify` | 25 testes passaram; BUILD SUCCESS |
| Testes direcionados D2 | 31 testes passaram; BUILD SUCCESS |
| Suíte completa — `mvn verify` | **51 testes**, zero falhas, erros ou ignorados; **BUILD SUCCESS** |
| Contrato com fake | 11 cenários passaram, sem Spring/banco |
| Contrato com JPA/H2 | Os mesmos 11 cenários passaram, mais três testes transacionais |
| Compilação/execução isolada | Domínio, aplicação, contrato público, fake e smoke compilados sem dependências externas; executados em processo Java somente com essas classes |
| `jdeps` do bytecode isolado | Somente `java.base` |
| Inspeção de imports | Núcleo sem Spring/JPA/repository/entity/infraestrutura; clientes externos só importam API do módulo |

Composição da suíte final: domínio 11; caso de uso 5; regressão HTTP/JPA D1 8; contexto Spring 1; contrato fake 11; contrato JPA/transações 14; compilação/execução isolada 1 = **51**.

Cenários comuns aos adapters: busca existente/ausente; conclusão com médias 8, 7 e 6,5; quatro conclusões acumuladas; erro do caso de uso para ausente; atualização preservando ID/média; rejeição de saldo obsoleto; rejeição de média obsoleta; atualização não cria aluno inexistente.

A suíte completa gerou o JAR Spring Boot e relatório JaCoCo. Não foi definida meta nem afirmada cobertura integral.

## Comandos executados e reprodução

Da raiz do projeto:

```bash
# Antes do D2; log em docs/evidencias/desafio-1:
mvn -o verify

# Durante o D2:
mvn -o -Dtest=ConcluirCursoUseCaseTest,FakeAlunoStoreContractTest,JpaAlunoStoreContractTest,DominioIsoladoTest test
sh docs/evidencias/desafio-2/verificar-nucleo.sh
python3 docs/evidencias/desafio-1/verificar-fronteiras.py
mvn verify

# Revisão de fontes/documentação; logs/diffs crus preservam espaços da ferramenta:
git diff --check -- . ':!docs/evidencias/**'
```

O script de núcleo usa `javac` com classpath somente de um diretório temporário inicialmente vazio, executa o smoke em novo processo e verifica `jdeps`. `DominioIsoladoTest` incorpora a mesma comprovação de compilação/execução à suíte Maven, também sem bibliotecas externas no processo compilado.

Artefatos: [testes direcionados](testes-direcionados.txt), [verify completo](mvn-verify.txt), [resumo dos relatórios](resumo-testes.txt), [núcleo isolado e bytecode](nucleo-isolado.txt), [verificação de imports](verificacao-imports.txt), [script reproduzível](verificar-nucleo.sh) e [inventário](arquivos-do-incremento.txt). Os relatórios textuais Surefire de cada classe também estão nesta pasta; o HTML de cobertura fica em `target/site/jacoco/index.html`.

## Rastreabilidade, justificativa e limites

RF02 (conclusão/recompensa, preservação do legado) → RNFs de testabilidade, modificabilidade, manutenibilidade → três ASRs + RPCs de Spring Boot, regras reaproveitadas e evolução sem reescrita → manter estado D1 parcial × consolidar Ports & Adapters × migrar globalmente → [ADR-002](../../adr/ADR-002.md) → [C4 Component](../../arquitetura/diagramas.md) → contrato comum, JDK isolado, transações e regressões.

O algoritmo do domínio, endpoints, regras de fórum e Premium foram preservados. Nenhuma dependência de produção, tabela, microserviço ou ranking foi acrescentado. Domínio/entidade/repository/banco ainda são compartilhados com o legado. As fronteiras são de pacote, não de módulo Maven/JPMS.

Pendências: revisão da entrega; PostgreSQL/Docker, navegador e transporte HTTP real não executados neste incremento; controle global de concorrência e tratamento HTTP de conflitos continuam sem implementação. Desafios 3–6 seguem pendentes. Nenhum resultado de desempenho ou capacidade para 100.000 usuários foi inferido.
