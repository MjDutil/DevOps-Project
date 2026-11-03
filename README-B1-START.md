# Como usar este pacote de apoio B1

Este arquivo é um complemento ao `README.md` existente da AC1. Para o B1, consulte **`docs/arquitetura/README.md`**. Contém matriz cumulativa, seis ADRs, diagramas Mermaid, roadmap e checklist de evidências.

**Mudanças reais de código:** refatoração incremental apenas de `concluirCurso` para um caso de uso isolado com porta `AlunoStore` e adapter JPA; tests para fake. **Sem outros endpoints removidos**. O fluxo que já existia continua integrado à API.

**Mini-lab adicional:** `labs/ia` possui FastAPI e foi adicionado um cliente Spring `IaClient` com timeout/fallback; isto não constitui ML/RAG real e ainda precisa ser executado. O desafio 3 tem protótipo de integração sem teste de execução; os desafios 4–6 ainda precisam de implementação/testes reais. Todas as decisões de arquitetura desses desafios estão como propostas, não como entregas comprovadas.

Este material foi preparado a partir do ZIP enviado e **não foi publicado no seu GitHub**. Mantenha a evolução incremental e valide com Docker/Maven antes de entregar.

## Incremento D1 — 2026-10-27

A conclusão agora é acessada por `gamificacao.api.ConclusaoCurso`, sem dependência do serviço legado para o caso de uso interno. Consulte [o registro D1](docs/evidencias/desafio-1/README.md) para testes HTTP/JPA, comparação antes/depois e limites. O texto acima descreve a preparação original; os desafios 2–6 não foram executados neste incremento.

## Incremento D2 — 2026-10-27

Ports & Adapters foi consolidado na conclusão de curso, com contrato comum fake/JPA, núcleo compilado e executado somente com JDK e validação transacional H2. Consulte [as evidências D2](docs/evidencias/desafio-2/README.md). Os desafios 3–6 continuam fora desta etapa.
