# D6 — CI Jenkins e observabilidade local

Este laboratório usa Docker Desktop, Python 3 e rede para baixar imagens/dependências. Não usa o socket Docker dentro do Jenkins, nem altera os serviços/volumes do Compose principal. Java 21 executa o controller; JDK 17 compila o Core. [Decisão e limites](../../docs/adr/ADR-006.md).

## Preparação e CI

Na raiz do repositório, confirme `docker info`. As imagens estão fixadas por digest em `images.lock.json`; `jenkins/plugins.lock.txt` registra a resolução efetivamente validada; `plugins.txt` fixa as mesmas versões para a construção seguinte. Nunca publique `.runtime/`.

```bash
python3 labs/operacao/preparar_lab.py
python3 labs/operacao/lab.py up --build -d jenkins
python3 labs/operacao/registrar_versoes.py
python3 labs/operacao/preparar_lab.py --refresh-snapshot
python3 labs/operacao/executar_ci.py
```

A preparação cria credenciais aleatórias e portas livres, em `.runtime/` ignorado. Recusa sobrescrever um ambiente existente. O refresh atualiza somente os arquivos dentro do diretório do snapshot, preserva o ponto de montagem e deve ocorrer com Jenkins ocioso; não muda credenciais/volumes. O manifesto contém commit-base e hashes dos arquivos incluídos. Evidências históricas, credenciais, caches e builds não entram no snapshot.

O primeiro build executa Maven e Python e arquiva `aprovado/app.jar`, manifesto, JUnit e JaCoCo. O segundo injeta um teste falhando somente no workspace Jenkins e executa esse JUnit isolado como controle negativo: deve terminar FAILURE, pular Python/aprovação e não arquivar artefato aprovado. O caminho normal mantém Maven completo e Python obrigatórios. O teste injetado não existe nos fontes locais. O script baixa o JAR do primeiro build e confere SHA-256. Relatórios reais ficam nas [evidências D6](../../docs/evidencias/desafio-6/README.md).

## Implantação local e experimento

```bash
python3 labs/operacao/lab.py --profile observacao up --build -d core ia prometheus grafana
python3 labs/operacao/experimento.py
```

O Dockerfile de runtime copia o JAR aprovado, sem recompilar. Não substitua por um JAR de `target/`. O experimento registra contadores iniciais, para IA, provoca três fallbacks de recomendação e dois de assistente, conclui operação essencial do Core, religa IA e verifica estabilização. Falhas de prontidão são registradas no valor inicial; não se pressupõe contador zero.

Para capturar a fase indisponível, use `D6_PAUSAR_CAPTURA=1` e, em outro terminal, execute o capturador antes de criar `.runtime/continuar`. A captura requer Puppeteer já disponível em um diretório de ferramentas separado; não é dependência de produção:

```bash
D6_PAUSAR_CAPTURA=1 python3 labs/operacao/experimento.py
# Em outro terminal, enquanto .runtime/fase.txt indicar ia-indisponivel:
node labs/operacao/capturar.mjs /tmp/b1-readme-tools ia-indisponivel
touch labs/operacao/.runtime/continuar
# Após a recuperação:
node labs/operacao/capturar.mjs /tmp/b1-readme-tools recuperada
```

Portas efetivas estão em `.runtime/config.json`, todas publicadas em `127.0.0.1`. Jenkins/Grafana usam usuário `b1admin` e senha do respectivo arquivo local; não copie esses arquivos para evidências. Prometheus fica em loopback, sem login. Core publica somente a porta de aplicação; gerenciamento 9091 só é acessível na rede Docker. Fora do perfil, Actuator está bloqueado. Não há TLS/HA/alertas nem CD de produção.

Consultas principais: `up{job="core"}`, `ia_fallback_total`, `sum by (operacao) (ia_fallback_total)` e `http_server_requests_seconds_count`. `up` indica coleta, não SLA. HTTP 200 da IA pode estar degradado; use também o contador específico. Os contadores reiniciam com o Core; a série histórica fica no volume Prometheus do laboratório.

## Encerramento

```bash
python3 labs/operacao/lab.py --profile observacao down
```

Esse comando remove somente containers/rede deste projeto e preserva volumes para revisão. Não use prune, não remova volumes anteriores. Para repetir a experiência, preserve os resultados anteriores em outro diretório: os scripts usam nomes fixos de evidência e builds subsequentes recebem novos números. Não confunda a execução de código com publicação Git; commit/push/merge dependem de autorização.

## Validação única de fechamento

`validar_pipeline_final.py` executou somente o fluxo normal da configuração final (#5), com 115 testes aprovados. Salva resultados em `docs/evidencias/desafio-6/validacao-final/`, não sobrescreve #1–#4, não implanta o JAR e recusa novo acionamento se já existir o registro da tentativa. É um roteiro de fechamento, distinto do ensaio positivo/negativo repetível acima.
