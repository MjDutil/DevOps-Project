# Integração proposta — aguarda aprovação

Repositório: https://github.com/MjDutil/DevOps-Project.

Consulta remota realizada em 2026-10-27 com `git ls-remote --symref origin HEAD refs/heads/main`; referências atualizadas com `git fetch origin`.

- Branch padrão confirmada: **`main`** (`HEAD → refs/heads/main`).
- Commit remoto observado: `153cdfb750578c694b983e8e7ef8bcff9d6900ef`.
- Branch de trabalho: **`feature/architecture-lab-b1`**.
- Commits anteriores preservados: limpeza `a25b2b2`, D1 `8bf8785`, D2 `34206c7`.
- A revisão do README é um commit adicional nessa feature.
- `git merge-base --is-ancestor origin/main HEAD` confirmou que a base remota é ancestral da feature. Com esse estado, a integração pode ser **fast-forward**, sem resolução de conflitos ou reescrita do histórico.

## Conteúdo a publicar

README principal completo com Desafios 1 e 2; código e testes D1/D2; ADRs; diagramas; evidências reais; registro cumulativo; histórico AC1 preservado. Os artefatos iniciais das outras propostas B1 permanecem no histórico, identificados como não validados; esta revisão não implementa D3.

A limpeza remove do versionamento somente 1.151 dependências em `frontend/node_modules`, três arquivos de `frontend/dist` e dois logs Vite. Nenhum código de `frontend/src`, fonte Java, configuração necessária ou evidência AC1 foi removido.

## Procedimento proposto após aprovação

Os comandos abaixo **não foram executados nesta etapa**:

```bash
# Atualizar e reconferir antes de publicar, pois o remoto pode mudar.
git fetch origin
git switch main
git merge --ff-only origin/main
git merge --ff-only feature/architecture-lab-b1
git push origin main
```

Se a branch padrão avançar com mudanças divergentes, interromper a integração e preparar nova comparação/testes; não forçar push. Nenhum merge nem push em `main` foi executado durante esta preparação. Também não foi feito push da feature.

## Conferência após a publicação aprovada

Abrir a página principal do repositório e confirmar README, índice, Mermaid, links relativos, ADRs e evidências no commit publicado. A validação local usa os destinos e caminhos que estarão no mesmo commit; não se declara que os novos links já existem na `main` remota antes da publicação.
