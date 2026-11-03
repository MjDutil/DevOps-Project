# Mini-lab 3 — API Python (FastAPI)

Protótipo demonstrativo de recomendação/assistente; **não implementa ML nem RAG reais**. Integração Spring/FastAPI, interrupção, recuperação e timeout de leitura foram executados em 2026-11-03. [Resultados, logs e limitações](../../docs/evidencias/desafio-3/README.md).

## Executar somente Python

```bash
python3 -m venv /tmp/b1-d3-venv
/tmp/b1-d3-venv/bin/pip install -r labs/ia/requirements.txt
/tmp/b1-d3-venv/bin/python -m uvicorn main:app --app-dir labs/ia --host 127.0.0.1 --port 8000
```

Em outro terminal: `curl http://localhost:8000/health`. Endpoints: `/recomendacoes/{aluno_id}` e `/assistente?pergunta=...`. O Core usa `IA_BASE_URL`, padrão `http://localhost:8000`, e oferece `/ia/recomendacoes/{id}` e `/ia/assistente?pergunta=...`.

## Reproduzir o laboratório integrado

Na raiz, após `mvn clean verify`:

```bash
/tmp/b1-d3-venv/bin/python docs/evidencias/desafio-3/executar-lab.py
```

O roteiro inicia Core/H2 e Python em portas locais livres, interrompe/reinicia Python, verifica operações essenciais e usa servidor lento para testar leitura em 1.200 ms. Encerra seus processos e registra resultados. Não modifica regras ou dados externos.

O Compose existente é uma alternativa configurada, **não executada no ensaio D3**:

```bash
docker compose up --build -d app ia postgres
curl http://localhost:8080/ia/recomendacoes/1
docker compose stop ia
curl http://localhost:8080/ia/recomendacoes/1
# Degradado: HTTP 200, status indisponivel e lista vazia.
docker compose start ia
```

[ADR-003](../../docs/adr/ADR-003.md) explica alternativas, fronteira e trade-offs. Python pode falhar sem impedir criação, conclusão, fórum e Premium. Isso foi observado em rede local, sem prova de carga/escala ou timeout real de conexão.
