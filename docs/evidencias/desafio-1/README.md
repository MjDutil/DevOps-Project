# Desafio 1 — monólito modular incremental

Data: 2026-10-27, America/Sao_Paulo. Resultado: fronteira pública da conclusão de curso implementada e comportamento preservado nos cenários executados. **Desafio 2 não iniciado.**

## Fontes e preparação do Git

- Enunciado lido: `Exercicio_B1_Architecture_Lab_6_Desafios_Reavaliado.docx`, localizado em Downloads. SHA-256 de `word/document.xml`: `9475a86aeb9e0d8c6d3d6e1b8c9c51cdd1a7b5e0c0433981b279c10f00898d05`. As três cópias encontradas têm o mesmo conteúdo XML.
- Exemplo orientativo D1 lido: `Exemplo_Solucao_Desafio_1_Architecture_Lab_B1.docx`. O enunciado permite reorganizar uma funcionalidade existente; ranking não precisa ser criado neste mini-lab.
- Clone completo da origem em `/tmp/devops-b1-original-clone`, preservando 25 commits e HEAD `153cdfb750578c694b983e8e7ef8bcff9d6900ef`.
- Antes de qualquer refatoração: comparação por conteúdo/SHA-256 e backup dos arquivos locais, sem builds/IDE, em `/tmp/devops-b1-local-before-d1`. Esse diretório recebeu posteriormente somente a nova classe de teste de regressão para executá-la contra os fontes anteriores.
- Nenhum arquivo de trabalho foi substituído pelo clone. Apenas os metadados `.git` foram incorporados à pasta atual; os arquivos locais B1 permaneceram como alterações em relação à origem.
- Branch: `feature/architecture-lab-b1`. Nenhum commit novo ou push foi executado. A cópia em `/tmp` é temporária; fontes anteriores relevantes e diffs também estão nesta pasta de evidências.
- A origem versionava 1.151 arquivos de `frontend/node_modules`, três de `frontend/dist` e dois logs. Os 1.156 arquivos foram retirados somente do índice Git. O histórico foi mantido e os logs existentes no disco foram preservados. As exclusões aparecem preparadas no índice; as outras alterações ficam disponíveis para revisão.
- `.gitignore` passou a cobrir dependências, caches, logs, `.env`, credenciais AWS, chaves/certificados e configurações locais. `.env.example` continua permitido. Isso protege novos arquivos; não remove conteúdo de commits históricos.

Artefatos: [comparação completa](comparacao-clone-local.txt), [diff do pacote B1 versus origem](integracao-b1.diff), [verificações Git/ignore](preparacao-git.txt) e [ambiente](ambiente.txt).

## Três estados distintos

| Estado | Dependência da conclusão |
|---|---|
| Origem AC1 no commit-base | `AlunoController → AlunoService → AlunoRepository/AlunoEntity`; a regra `media > 7` estava duplicada no serviço e domínio. |
| Pacote B1 local recebido — antes deste incremento | `AlunoController → AlunoService → ConcluirCursoUseCase → AlunoStore ← JpaAlunoStore`. A regra já usava o domínio puro, mas o serviço legado conhecia o caso de uso interno. |
| Após D1 | `AlunoController → gamificacao.api.ConclusaoCurso ← ConclusaoCursoFacade → ConcluirCursoUseCase → AlunoStore ← JpaAlunoStore`. `AlunoService` deixa de depender do interior do módulo. |

A melhoria do pacote B1 não é atribuída a este incremento. O contrato público e a fachada transacional são a mudança D1 feita nesta etapa.

## Estrutura antes/depois

```text
Antes (B1 recebido)                 Depois (D1)
controller/AlunoController          controller/AlunoController → gamificacao.api
service/AlunoService →             service/AlunoService (criar/fórum/Premium)
  gamificacao.application          gamificacao/
gamificacao/                         api/ConclusaoCurso.java        [novo]
  application/                       application/                  [preservado]
    AlunoSnapshot                      AlunoSnapshot
    AlunoStore                         AlunoStore
    ConcluirCursoUseCase               ConcluirCursoUseCase
  infrastructure/                    infrastructure/
    GamificacaoConfiguration           ConclusaoCursoFacade.java    [novo]
    JpaAlunoStore                      GamificacaoConfiguration
                                       JpaAlunoStore
```

## Decisão e acoplamentos

A API pública tem um resultado próprio, sem DTO HTTP, JPA ou Spring. O controller traduz esse resultado para o JSON anterior. A fachada Spring delimita a transação na entrada do módulo e chama o caso de uso puro já existente.

| Aspecto | Antes local B1 | Depois D1 |
|---|---|---|
| Entrada da capacidade | Serviço legado compartilhado | Contrato público do módulo |
| Serviço legado → aplicação interna | Dependência direta | Eliminada |
| Controller → módulo | Indireto pelo serviço legado | Somente `gamificacao.api` |
| Snapshot de persistência na integração externa | Usado no serviço legado | Restrito ao módulo |
| Limite transacional da conclusão | Sem transação explícita do fluxo | Fachada com `@Transactional` |
| Domínio/framework | Domínio já puro | Preservado |
| Domínio, entidade, repository, banco | Compartilhados | Permanecem compartilhados |
| Processo/deploy | Um Core | Um Core |

A segunda busca no adapter foi preservada para limitar o escopo. A transação não equivale a controle de concorrência: atualização perdida permanece possível. Não há promessa de ganho de latência, throughput ou suporte a 100.000 usuários. Fronteiras Java são convenções de pacote, sem isolamento Maven/JPMS.

[Imports antes](dependencias-antes.txt), [imports depois](dependencias-depois.txt), [bytecode antes](dependencias-bytecode-antes.txt), [bytecode depois](dependencias-bytecode-depois.txt), [verificação da fronteira](verificacao-fronteira.txt) e [diff exclusivo deste incremento](refatoracao-d1.diff). Nos arquivos `jdeps`, bibliotecas Spring/JPA aparecem como `not found` porque a análise inclui somente as classes do projeto no classpath; isso não é resultado da compilação Maven.

Fontes anteriores exatos: [controller](antes-AlunoController.java.txt) e [serviço](antes-AlunoService.java.txt).

## Validação real

| Execução | Resultado observado |
|---|---|
| Suíte preexistente da análise inicial (`mvn -o test`) | 15 testes, zero falhas/erros/ignorados; relatórios `baseline-*.txt`. |
| Nova regressão executada contra fontes B1 anteriores | Oito cenários HTTP/JPA/H2, zero falhas/erros/ignorados. |
| `mvn -o clean verify` após refatoração | Os 25 testes passaram; empacotamento falhou por dependências do plugin ausentes no cache offline. A tentativa foi preservada. |
| `mvn verify` com acesso à rede para dependências | **BUILD SUCCESS**, 25 testes, zero falhas/erros/ignorados; JAR Spring Boot empacotado e relatório JaCoCo gerado. |
| Inspeção dos imports | Clientes externos só importam API pública; domínio/aplicação/API sem imports técnicos proibidos. |
| `git diff --check` e `git fsck --no-dangling` | Código de saída zero. |
| `docker compose config --quiet` | Código de saída zero; não inicia containers. |

Os oito cenários de integração usam `MockMvc` com `WebApplicationContext`, DispatcherServlet, beans reais e JPA/H2. Não há mocks de serviço/repository nem transação de teste ocultando o commit. Consultas JDBC independentes verificam dados gravados. Isso não é teste de transporte por socket, navegador ou PostgreSQL.

Cenários: médias 8/7/6,5; fórum válido, sem liderança e sem ajuda, todos combinados com conclusão; Básico com nove cursos; quatro conclusões até 12 cursos, verificação Premium repetida e nova conclusão preservando plano, voucher e moedas. A suíte unitária do caso de uso passou de três para cinco testes, acrescentando média inferior a sete e acumulação sucessiva.

Logs e resultados: [integração antes](testes-integracao-antes.txt), [tentativa offline](verify-offline-tentativa.txt), [verify final](verify-com-rede.txt) e [resumo dos relatórios finais](resumo-testes.txt). O relatório de cobertura está em `target/site/jacoco/index.html`; não foi estabelecida meta nem feita alegação de cobertura integral.

## Comandos principais

```bash
# Contra a cópia dos fontes anteriores, com a mesma nova classe de integração:
mvn -o -f /tmp/devops-b1-local-before-d1/pom.xml -Dtest=AlunoHttpJpaIntegrationTest test

# Estado final (Maven 3.9.11 instalado; primeira tentativa offline registrada):
mvn verify
python3 docs/evidencias/desafio-1/verificar-fronteiras.py
git diff --check
git fsck --no-dangling
docker compose config --quiet
```

`mvnw` recebeu permissão de execução. A distribuição configurada no wrapper não foi alterada nem foi usada como evidência: a validação utilizou o Maven instalado. As versões efetivas estão em `ambiente.txt`.

## Rastreabilidade e revisão

RF02 (conclusão/recompensa) e RF01 (driver futuro) → RNFs analisados → ASRs de manutenibilidade, escalabilidade e complexidade operacional + RPCs Java/Spring, equipe de seis, banco relacional e legado → camadas × monólito modular × serviços → [ADR-001](../../adr/ADR-001.md) → [C4 Container](../../arquitetura/diagramas.md) → testes/diffs/dependências desta pasta.

O registro cumulativo está em [docs/arquitetura/README.md](../../arquitetura/README.md). [Inventário das alterações desta etapa](arquivos-do-incremento.txt).

Pendências: revisão do D1 e posterior commit/publicação; confirmar integrantes B1; transporte HTTP real/navegador e PostgreSQL/Docker não validados nesta etapa; entradas inválidas/erros HTTP e concorrência permanecem limitações explícitas. Nenhuma dessas validações foi apresentada como realizada. Os desafios 2–6 permanecem fora do incremento.

## Consolidação Git antes do D2

A auditoria confirmou que as 1.156 remoções abrangem somente `frontend/node_modules` (1.151), `frontend/dist` (3) e logs Vite (2), sem fontes, configurações necessárias ou evidências. Limpeza isolada no commit `a25b2b2`. [Confirmação da suíte D1 antes do D2](verify-confirmacao-pre-d2.txt): `mvn -o verify`, 25 testes, zero falhas/erros/ignorados, BUILD SUCCESS. O commit seguinte consolida o D1 e os artefatos B1 preexistentes (incluindo o protótipo IA), sem implementar ou validar o D3. Os registros anteriores “sem commit novo” descrevem o fechamento original do D1; esta seção registra sua consolidação posterior. Nenhum push foi realizado.
