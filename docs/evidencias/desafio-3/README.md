# Evidências D3 — fronteira Spring Boot / FastAPI

Data: 2026-11-03. Base Git: `514e699`, branch `feature/architecture-lab-b1`. Nenhum commit, merge ou push realizado. D4 não iniciado.

## Fonte e critérios oficiais

Enunciado: `/Users/majuudutil/Downloads/Exercicio_B1_Architecture_Lab_6_Desafios_Reavaliado.docx`. SHA-256 de `word/document.xml`: `9475a86aeb9e0d8c6d3d6e1b8c9c51cdd1a7b5e0c0433981b279c10f00898d05`, igual ao consultado nos D1/D2.

O mini-lab exige Spring Boot consumindo FastAPI, interrupção do Python e observação/tratamento da falha. Não exige treinar modelo ML ou implementar RAG completo. O laboratório demonstra a hipótese: a fronteira opcional pode falhar e recuperar sem impedir operações essenciais do Core.

| Requisito | Comprovação |
|---|---|
| RF03/RF04 | Contratos demonstrativos de recomendação e assistente; sucesso, falha e recuperação de ambos em HTTP real. Inteligência real continua futura. |
| RNFs | Os seis RNFs analisados estão no [ADR-003](../../adr/ADR-003.md). |
| Até três ASRs | Tolerância a falhas, deploy independente e escalabilidade; impacto estrutural explicado no ADR. Escala não medida. |
| RPCs | Java/Spring no Core, Python na IA e operações essenciais independentes da IA. |
| Alternativas | Embutida × módulo acoplado × serviço independente; ML/RAG juntos inicialmente, com gatilhos para separação. |
| ADR | [ADR-003](../../adr/ADR-003.md), decisão e consequências sustentadas pelo ensaio. |
| C4 Container | [Diagrama D3](../../arquitetura/diagramas.md#c4-container--estado-validado-do-desafio-3), protocolos HTTP/JSON e JPA/JDBC. |
| Mini-lab | [Roteiro executável](executar-lab.py), [saída nominal](execucao-lab.txt), [JSON com respostas/tempos](resultados-lab.json) e logs. |
| Git | [Estado anterior e hashes](git-antes.json), [preservação posterior](preservacao.json). Arquivos locais prontos para revisão; versionamento/publicação aguardam autorização. |

## Execução e resultados reais

Ambiente: Java 17.0.20.1, Maven 3.9.11, Python 3.9.6, FastAPI 0.128.8, Uvicorn 0.39.0; [ambiente](ambiente.txt) e [dependências Python resolvidas](python-freeze.txt). Instalação em venv temporário, conforme `labs/ia/requirements.txt`; nenhuma dependência de produção foi adicionada.

1. `mvn clean verify`: **75 testes, zero falhas/erros/ignorados, BUILD SUCCESS**, 40,755 s. [Log](mvn-clean-verify.txt), [contadores Surefire/JaCoCo](resumo-testes.json). JaCoCo: 669 instruções, 16 branches, 160 linhas, 62 métodos, todos cobertos. Esses percentuais são dos testes Maven; o JAR do ensaio real não foi instrumentado para cobertura.
2. `sh docs/evidencias/desafio-2/verificar-nucleo.sh`: [núcleo somente JDK](nucleo-isolado.txt), passou.
3. `python3 docs/evidencias/desafio-1/verificar-fronteiras.py`: [fronteiras](fronteiras.txt), passou.
4. `/tmp/b1-d3-venv/bin/python docs/evidencias/desafio-3/executar-lab.py`: **18 verificações HTTP reais aprovadas**, distintas dos 75 testes Maven. O roteiro sobe o JAR com H2, Uvicorn/FastAPI e servidor lento em portas livres de loopback e encerra somente seus próprios processos.

| Etapa HTTP | Resultado observado |
|---|---|
| Health Python e recomendação/assistente via Spring | 3 checks, todos HTTP 200 com conteúdo esperado. |
| Interrupção real de Uvicorn | 2 checks, fallback nos dois endpoints; 49,52 ms e 4,45 ms. |
| Operações essenciais sem Python | 7 checks: criar aluno, quatro conclusões (+3 cada), fórum (+1), Premium com voucher e três moedas; saldo 13. |
| Reinício de Uvicorn, mesmo processo Core | 2 checks, recomendação e assistente recuperados. |
| Servidor HTTP lento (3 segundos) | 2 checks, fallback em 1.206,55 e 1.206,38 ms. |
| Core durante chamadas lentas | 2 checks, conclusão em 7,10 e 6,85 ms, saldos 16 e 19. |

[Log Core](core-log.txt) e [log FastAPI](fastapi-log.txt) registram os processos reais, requisições e desligamentos. Dados de teste são fictícios e H2 em memória é descartado ao encerrar.

## Reprodução

Na raiz do projeto, com JDK 17, Maven e Python com suporte a `list[str]` (o ambiente usado foi 3.9.6):

```bash
python3 -m venv /tmp/b1-d3-venv
/tmp/b1-d3-venv/bin/pip install -r labs/ia/requirements.txt
mvn clean verify
/tmp/b1-d3-venv/bin/python docs/evidencias/desafio-3/executar-lab.py
```

O roteiro regrava `core-log.txt`, `fastapi-log.txt` e `resultados-lab.json`. Para capturar também a saída nominal, redirecionar stdout/stderr para `execucao-lab.txt`. Para repetir exatamente as versões Python observadas, instalar `-r docs/evidencias/desafio-3/python-freeze.txt` no mesmo Python/plataforma. Os intervalos do arquivo original permitem versões diferentes em instalações futuras.

A primeira instalação sob sandbox falhou por acesso à rede; a instalação autorizada fora dele passou ([log](python-install.txt)). A primeira tentativa do laboratório não pôde abrir portas locais ([tentativa](tentativa-sandbox.txt)); a execução autorizada fora do sandbox terminou com código zero. Essas restrições não são falhas da aplicação.

## Conferência documental

[Resultado da verificação](validacao-documentacao.txt): 141 links relativos/âncoras em seis documentos com destinos existentes, Container D3 sincronizado entre README e diagramas, `git diff --check` sem erros. Esse registro descreve a validação inicial; o fechamento posterior abaixo acrescenta parser/renderização Mermaid D3. Os 32 arquivos anteriores em `src/main` e `src/test` mantiveram seus hashes, e `pom.xml` não mudou.

## Limites e pendências

- RF03/RF04 têm respostas demonstrativas, sem personalização baseada em dados reais, ML, RAG ou LLM.
- Timeout de conexão **700 ms configurado**, mas não medido em rede real; conexão recusada não é timeout. O de leitura **1.200 ms** foi exercitado. Timeouts são por fase e não um prazo total garantido.
- HTTP 200 com `status: indisponivel` foi preservado. Cliente não valida schema do mapa; contrato inesperado com JSON válido pode ser repassado.
- Chamadas IA são síncronas e ocupam threads. O ensaio simultâneo comprova duas operações no cenário observado; não prova isolamento sob saturação, carga, SLA ou 100.000 usuários.
- Python foi reiniciado independentemente; não foi comprovado deploy de modelos em ambiente externo nem escalabilidade horizontal.
- PostgreSQL/Compose, frontend/navegador e capturas de tela não foram executados. As evidências são logs e respostas reais, suficientes para ler o ensaio sem executar; não há prints fabricados.
- D1/D2 preservados; documentação histórica de suas entregas permanece como histórico. D4–D6, identificação B1 e commit/publicação autorizados continuam pendentes.

Decisão para a AF: manter fronteira opcional REST com timeout/fallback e avaliar separação ML/RAG apenas com necessidade demonstrada. Eventos (D4), IoT (D5) e observabilidade/esteira (D6) exigem suas próprias decisões e evidências.

## Fechamento documental da primeira entrega

Após a simplificação do README principal, o validador `docs/evidencias/revisao-readme/validar-readme.mjs` verifica os fontes e os rótulos dos SVGs D1/D2/D3 em seus arquivos próprios, confere o Container D3 em `docs/arquitetura/diagramas.md` e exige sua apresentação como bloco Mermaid no README. Os três blocos Mermaid não precisam ser duplicados na página inicial. O modo padrão exige destinos no índice; `--working-tree` admite arquivos novos não ignorados para revisão antes de git add/commit.

C4 D3: [fonte Mermaid](c4-container.mmd), [SVG renderizado](c4-container.svg) e [log do renderizador](mermaid-render.txt). A renderização usa Mermaid CLI temporário em `/tmp/b1-readme-tools`, sem dependências adicionadas à aplicação. [Resultado documental atualizado](validacao-final.txt).

Comandos de reprodução, com as ferramentas editoriais disponíveis em `/tmp/b1-readme-tools`:

```bash
/tmp/b1-readme-tools/node_modules/.bin/mmdc -i docs/evidencias/desafio-3/c4-container.mmd -o docs/evidencias/desafio-3/c4-container.svg
node docs/evidencias/revisao-readme/validar-readme.mjs /tmp/b1-readme-tools --working-tree
git diff --check
```

### Credenciais demonstrativas do Compose

A senha literal `tdd123` em `docs/evidencias/desafio-1/integracao-b1.diff:36` é contexto preservado do Compose local herdado: usuário/banco `tdd`, host interno `postgres` e senha correspondente ao serviço PostgreSQL de laboratório. É uma **credencial demonstrativa destinada exclusivamente ao ambiente local acadêmico**, não um token nem uma credencial de infraestrutura externa identificada. O Compose também contém `admin123` para o PgAdmin demonstrativo. Esses valores são públicos no repositório e não devem ser reutilizados em ambientes externos. A inspeção do repositório não permite provar se alguém os reutilizou fora dele.

O D3 executou H2 descartável com usuário `sa` e senha vazia, sem usar essas credenciais PostgreSQL. O diff histórico D1 e o Compose foram preservados; esta nota identifica explicitamente a natureza dos valores, sem reescrever a evidência original.
