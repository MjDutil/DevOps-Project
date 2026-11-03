# Auditoria final — Desafios 1 e 2

> Nota editorial posterior: o README principal foi simplificado como apresentação do projeto. Os links abaixo foram redirecionados aos ADRs e ao C4 existentes; resultados e logs desta auditoria histórica permanecem preservados.

Data: 2026-10-27. Escopo: critérios dos mini-labs D1/D2, apresentação no README principal, testes, cobertura e preparação Git. D3 não foi iniciado e nenhum merge/push foi realizado.

**Conclusão técnica:** os dois mini-labs estão implementados, documentados e sustentados por evidências estáticas que a professora pode consultar sem executar a aplicação. **Disponibilidade pública:** ainda depende da integração/publicação autorizada em `main`. A identificação dos integrantes B1 permanece sujeita à confirmação; os nomes atualmente registrados são os da AC1.

## Fonte da auditoria

Enunciado original consultado: `Exercicio_B1_Architecture_Lab_6_Desafios_Reavaliado.docx`, disponível em Downloads. SHA-256 do XML `word/document.xml`: `9475a86aeb9e0d8c6d3d6e1b8c9c51cdd1a7b5e0c0433981b279c10f00898d05`, igual ao documento usado nas decisões anteriores.

O D1 exige reorganizar uma funcionalidade existente e comparar dependências; não exige implementar ranking nem demonstrar 100.000 usuários. O D2 exige isolar uma regra e substituir pelo menos um adapter, mantendo os testes do domínio. Os valores abaixo reproduzem a distribuição dos critérios, não uma previsão de nota.

## Checklist dos critérios de avaliação

| Critério por desafio | Peso no enunciado | D1 — comprovação | D2 — comprovação | Auditoria |
|---|---:|---|---|---|
| RF/RNF e seleção de até três ASRs | 0,25 | [Requisitos D1](../../adr/ADR-001.md#rnfs-e-seleção-de-no-máximo-três-asrs); [ADR-001](../../adr/ADR-001.md). Manutenibilidade, escalabilidade e complexidade operacional; RF01 futuro e RF02 exercitado. | [Requisitos D2](../../adr/ADR-002.md#asrs-prioritários--no-máximo-três); [ADR-002](../../adr/ADR-002.md). Testabilidade, modificabilidade e manutenibilidade; consistência tratada como correção, sem quarto ASR. | Atendido no escopo do mini-lab |
| RPCs, alternativas e trade-offs | 0,20 | [Decisão D1](../../adr/ADR-001.md#alternativas-e-trade-offs): Spring Boot, seis desenvolvedores no case, banco relacional e legado; camadas × modular × serviços. | [Decisão D2](../../adr/ADR-002.md#alternativas-e-trade-offs): regras reaproveitadas e proibição de reescrita global; estado D1 parcial × Ports & Adapters × migração global. | Atendido |
| ADR e justificativa | 0,20 | [docs/adr/ADR-001.md](../../adr/ADR-001.md): contexto, decisão, consequências e gatilho de revisão; resumo também no README. | [docs/adr/ADR-002.md](../../adr/ADR-002.md): inversão da dependência, adapters, atualização condicional, limites e consequências. | Atendido |
| C4 coerente | 0,15 | [Container D1](../../arquitetura/diagramas.md#c4-container--estado-do-desafio-1); [SVG](../revisao-readme/c4-container.svg). Core permanece um processo/deploy. | [Component D2](../../arquitetura/diagramas.md#c4-component--core-estado-do-desafio-2); [SVG](../revisao-readme/c4-component.svg); [visão detalhada](../../arquitetura/diagramas.md). | Atendido; fontes Mermaid e SVGs versionados |
| Microsolução, evidência e Git | 0,20 | [API pública](../../../src/main/java/org/example/tdd/gamificacao/api/ConclusaoCurso.java); [fachada](../../../src/main/java/org/example/tdd/gamificacao/infrastructure/ConclusaoCursoFacade.java); [diff antes/depois](../desafio-1/refatoracao-d1.diff); [testes anteriores](../desafio-1/testes-integracao-antes.txt). Commit D1 `8bf8785`. | [Contrato comum](../../../src/test/java/org/example/tdd/gamificacao/AlunoStoreContract.java); [fake](../../../src/test/java/org/example/tdd/gamificacao/FakeAlunoStoreContractTest.java); [JPA/H2](../../../src/test/java/org/example/tdd/gamificacao/JpaAlunoStoreContractTest.java); [mesmos cenários aprovados](substituicao-adapters.md). Commit D2 `34206c7`. | Implementação e evidência locais prontas; publicação autorizada pendente |

## Evidência específica de substituição dos adapters

`FakeAlunoStoreContractTest` e `JpaAlunoStoreContractTest` herdam os mesmos métodos de `AlunoStoreContract`. Esse contrato cria `new ConcluirCursoUseCase(store())`; somente `store()` e a fixture mudam entre as implementações. A regra permanece em `domain.Aluno`, com hash preservado desde D1.

Na execução limpa desta auditoria, **os mesmos 11 cenários passaram com ambos os adapters**, mais três cenários específicos de transação JPA. A [tabela nominal extraída do Surefire](substituicao-adapters.md) permite verificar cada caso sem executar o sistema.

O [adapter fake](../../../src/test/java/org/example/tdd/gamificacao/FakeAlunoStore.java) fica somente em testes. O [adapter JPA](../../../src/main/java/org/example/tdd/gamificacao/infrastructure/JpaAlunoStore.java) usa H2 nas integrações. A [porta](../../../src/main/java/org/example/tdd/gamificacao/application/AlunoStore.java) pertence à aplicação; o [caso de uso](../../../src/main/java/org/example/tdd/gamificacao/application/ConcluirCursoUseCase.java) e o [domínio](../../../src/main/java/org/example/tdd/domain/Aluno.java) não dependem de Spring/JPA.

## Validação final repetida

Comando executado: `mvn clean verify`.

| Verificação | Resultado observado |
|---|---|
| Suíte completa | **75 testes**, zero falhas, erros ou testes ignorados; **BUILD SUCCESS** |
| Instruções JaCoCo | **669/669 — 100%** |
| Branches JaCoCo | **16/16 — 100%** |
| Linhas JaCoCo | **160/160 — 100%** |
| Métodos | **62/62 — 100%** |
| Classes executáveis | Mesmo conjunto das 17 classes anteriores; nenhuma exclusão adicionada |
| Domínio/aplicação/fake isolados | Compilação e execução sem dependências externas; bytecode somente `java.base` |
| Fronteira do módulo | Clientes externos importam somente API pública; núcleo sem imports técnicos proibidos |

Artefatos legíveis no GitHub: [log integral](mvn-clean-verify.txt), [contadores e suites em JSON](resultados.json), [verificação automatizada](validacao-resultados.txt), [execução somente JDK](nucleo-isolado.txt), [fronteiras](fronteiras.txt) e [comparação por classe antes/depois](../cobertura/comparacao.md). O [script](validar-resultados.py) reproduz as verificações a partir do resultado local, caso alguém queira auditar também por execução.

Os 25 testes do D1 e 51 do D2 são totais históricos, preservados nas respectivas evidências. Os 75 atuais incluem validações adicionais do código existente. Cobertura global não equivale à completude funcional e não prova FastAPI real, carga, PostgreSQL/Docker ou concorrência global. Os testes do cliente IA são simulados; D3 continua pendente.

## README e rastreabilidade sem execução local

O [README principal](../../../README.md) orienta a apresentação do projeto. A fundamentação obrigatória dos dois desafios permanece no [registro cumulativo](../../arquitetura/README.md), nos ADRs e nas evidências; os diagramas estão no documento C4 vinculado acima.

Em ambos os desafios, é possível seguir RF → RNF → ASR + RPC → alternativas → ADR → C4 → evidência. O código de testes e os logs estão versionados; a professora pode ler os resultados no GitHub sem instalar Java, Docker, banco ou ferramentas de renderização.

Os links são relativos à árvore do commit a publicar, respeitando capitalização e âncoras. [Resultado da validação de toda a documentação](links-documentacao.txt) e [integridade dos diagramas](diagramas.txt). Os diagramas já foram renderizados em Mermaid 11; seus fontes permanecem sincronizados com o README e os SVGs foram verificados nesta auditoria. Não é afirmado que o novo conteúdo já foi observado na página pública: a conferência na `main` remota ocorre após a publicação aprovada.

## Git e publicação

- Limpeza anterior: `a25b2b2` — 1.151 dependências, três builds frontend e dois logs retirados somente do versionamento; sem remoção de fontes/evidências.
- D1: `8bf8785`.
- D2: `34206c7`.
- README completo: `ada5768`.
- Testes e cobertura global: `20b933c`.
- Esta auditoria e a consolidação documental ficam em commit próprio, sem reescrever os anteriores.

[Auditoria de arquivos rastreados e exclusões](git-arquivos.txt). Somente fontes, configuração necessária, documentação, imagens e snapshots intencionais de evidências são versionados. `target`, dependências, caches, arquivos temporários e credenciais locais permanecem excluídos.

A branch padrão remota foi reconfirmada como `main`, em `153cdfb750578c694b983e8e7ef8bcff9d6900ef`. A feature contém essa base como ancestral, permitindo fast-forward no estado consultado. [Preparação atual da integração](publicacao.md).

**Pendências antes de dizer “entrega disponível na página principal”:** confirmar identificação B1, obter autorização de merge/push e publicar em `main`. PostgreSQL/Docker, rede real, ranking e 100.000 usuários não são evidências exigidas para comprovar estes dois mini-labs e permanecem explicitamente fora da declaração de conclusão.
