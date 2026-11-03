# Revisão do README principal — avaliação D1/D2

Data: 2026-10-27. Escopo: documentação, apresentação e preparação de integração; código de produção e testes D1/D2 não foram alterados.

## Conteúdo preparado

O README da raiz agora apresenta diretamente os dois desafios: contexto, RF/RNF/FURPS+, até três ASRs por desafio, RPCs, alternativas e trade-offs, decisão, resumo de ADR, Mermaid, resultados reais e rastreabilidade. Links adicionais permitem auditar os artefatos, sem substituir o conteúdo essencial da página principal.

O README anterior da AC1 foi preservado em `docs/ac1/README.md`, com caminhos relativos ajustados. O registro cumulativo em `docs/arquitetura/README.md` continua mantido e aponta para a apresentação principal. Ranking, 100.000 usuários e execução PostgreSQL/Docker não foram apresentados como implementados/validados.

## Revalidação Java

Comando executado: `mvn verify`.

Resultado observado: **BUILD SUCCESS**, **51 testes**, zero falhas, erros ou testes ignorados. O JAR Spring Boot e o relatório JaCoCo foram gerados. [Log integral](mvn-verify.txt).

Esta execução reconfirma D1/D2 para a revisão editorial; não cria novos resultados de desempenho. Os resultados históricos (25 testes D1 e 51 D2) continuam identificados por etapa no README.

## Validação de Markdown, links e Mermaid

O script [validar-readme.mjs](validar-readme.mjs) usa um parser Markdown e `github-slugger`, verifica destinos, capitalização, âncoras explícitas/GitHub e inclusão dos arquivos no índice Git. Examina a raiz, histórico AC1, registro cumulativo, ADRs 001/002 e registros de evidências ligados à entrega. Código em blocos não é tratado como hyperlink. As âncoras usam `<a name="...">`, formato documentado pelo [GitHub para navegação personalizada](https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#custom-anchors). A prévia local do índice e dos SVGs foi inspecionada visualmente; não é captura da página remota.

Os dois diagramas Mermaid do README foram extraídos para fontes `.mmd` e renderizados localmente em SVG com Mermaid CLI 11 / Chromium. Os arquivos SVG também estão versionados e vinculados abaixo de cada diagrama. O script verifica que o Mermaid do README corresponde aos fontes renderizados.

- [Fonte Container](c4-container.mmd) · [SVG Container](c4-container.svg).
- [Fonte Component](c4-component.mmd) · [SVG Component](c4-component.svg).
- [Resultado da renderização](mermaid-render.txt).
- [Resultado dos links/âncoras/índice Git](links-validacao.txt).
- [Versões das ferramentas](ferramentas.txt).

A tentativa inicial de abrir Chromium dentro do sandbox falhou antes de produzir SVG; [log preservado](mermaid-tentativa-sandbox.txt). A execução posterior com permissão gerou os diagramas. As ferramentas editoriais foram instaladas somente em `/tmp`; nenhuma dependência foi adicionada ao `pom.xml` ou ao frontend.

O README usa blocos `mermaid` e sintaxe básica `flowchart`, conforme a [documentação oficial do GitHub](https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/creating-diagrams). A sintaxe foi renderizada localmente; a conferência final do runtime/tema do GitHub depende da publicação aprovada. Não foi afirmado que o novo README já foi renderizado na `main` remota.

## Reprodução da validação editorial

Pré-requisitos adicionais somente para esta verificação: Node.js, ferramentas em diretório temporário e um Chromium local. A instalação não é necessária para executar a aplicação Java.

```bash
PUPPETEER_SKIP_DOWNLOAD=true npm install --prefix /tmp/b1-readme-tools --no-audit --no-fund @mermaid-js/mermaid-cli@11 mermaid@11 github-slugger marked

# Criar /tmp/b1-readme-puppeteer.json com executablePath do Chromium local.
# Criar /tmp/b1-readme-mermaid.json com theme neutral, securityLevel strict,
# flowchart.htmlLabels false. As configs são locais, não segredos do projeto.
/tmp/b1-readme-tools/node_modules/.bin/mmdc -i docs/evidencias/revisao-readme/c4-container.mmd -o docs/evidencias/revisao-readme/c4-container.svg -p /tmp/b1-readme-puppeteer.json -c /tmp/b1-readme-mermaid.json
/tmp/b1-readme-tools/node_modules/.bin/mmdc -i docs/evidencias/revisao-readme/c4-component.mmd -o docs/evidencias/revisao-readme/c4-component.svg -p /tmp/b1-readme-puppeteer.json -c /tmp/b1-readme-mermaid.json

# A validação exige que os destinos estejam incluídos no índice Git.
node docs/evidencias/revisao-readme/validar-readme.mjs /tmp/b1-readme-tools
```

## Integração preparada

A branch padrão foi consultada no remoto e confirmada como `main`, no commit `153cdfb750578c694b983e8e7ef8bcff9d6900ef`. `git fetch origin` atualizou as referências. A `main` é ancestral da feature, permitindo fast-forward no estado consultado.

[Plano concreto de integração e conferência posterior](integracao.md). [Comparação dos arquivos com main](comparacao-main-feature.txt).

Nenhum merge ou push foi executado. Os caminhos relativos foram conferidos contra os arquivos locais a publicar; não se declara que os novos destinos já existem em `main` no GitHub. A aprovação do usuário é necessária antes de integrar e publicar na branch padrão.
