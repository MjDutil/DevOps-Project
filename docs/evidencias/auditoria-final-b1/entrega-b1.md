# Fechamento da entrega B1

A usuária confirmou que a equipe B1 é a mesma da AC1. O [registro preservado](../../ac1/README.md#integrantes), também conferido no README original do commit AC1 `153cdfb`, informa exatamente:

- Felipe
- Isadora
- Maria Julia

Sobrenomes completos e RAs não constam nessa fonte; nenhuma informação adicional foi inventada. O histórico AC1 permanece sem alterações. A premissa de seis desenvolvedores do case não substitui a equipe real confirmada.

## Conteúdo da entrega

[README principal](../../../README.md) com seis seções; [registro arquitetural e matriz cumulativa](../../arquitetura/README.md); [ADRs 001–006](../../adr/); [diagramas C4](../../arquitetura/diagramas.md); evidências [D1](../desafio-1/README.md), [D2](../desafio-2/README.md), [D3](../desafio-3/README.md), [D4](../desafio-4/README.md), [D5](../desafio-5/README.md) e [D6](../desafio-6/README.md); [síntese para a AF](../../arquitetura/sintese-af.md).

O [build #5](../desafio-6/validacao-final/resultado.json) aprovou o fluxo normal final (101 Maven + 14 Python). Nesta etapa final de identificação/publicação não há alteração de código ou regras de negócio nem nova declaração de testes funcionais executados. As verificações são documentais e de Git/segurança. [Validação documental desta publicação](validacao-entrega.txt).

## Publicação autorizada

Destino exclusivo: [feature/architecture-lab-b1](https://github.com/MjDutil/DevOps-Project/tree/feature/architecture-lab-b1) no repositório `https://github.com/MjDutil/DevOps-Project.git`.

Commits técnicos preservados: D4 `59cb7d1`, D5 `a3b350e`, D6 `42128fa`. A identificação/documentação final recebe commit próprio; `git log -1` e a referência remota após o push identificam o último pacote publicado, sem criar referência circular ao hash deste documento.

A publicação exige confirmação de histórico sem divergência, conferência dos arquivos de saída, validação dos links e `git diff --check`/`git diff --cached --check`. Credenciais geradas, `.runtime/`, JARs, caches e arquivos temporários ficam fora do conteúdo versionado. Merge em main não foi autorizado. O resultado efetivo do push, o hash remoto e a árvore de trabalho são conferidos após a publicação e informados à usuária.

Os seis mini-labs estão documentados no escopo oficial. Limitações de IA demonstrativa, eventos voláteis, deduplicação em memória e implantação local permanecem explícitas; isso não declara uma solução de produção. A avaliação definitiva cabe ao professor.
