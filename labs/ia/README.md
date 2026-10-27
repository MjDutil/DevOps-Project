# Mini-lab 3 — API Python (FastAPI)

Este protótipo isola um contrato de recomendação/assistente; **não implementa ML nem RAG reais**.

```bash
docker build -t b1-ia ./labs/ia
docker run --rm --name b1-ia -p 8000:8000 b1-ia
# Em outro terminal:
curl http://localhost:8000/health
curl http://localhost:8000/recomendacoes/1
```

**Para concluir o desafio:** o cliente REST com timeout/fallback foi incluído em `ia/IaClient.java` e `ia/IaController.java`. Execute `docker compose up --build -d app ia postgres`, teste `/ia/recomendacoes/1`, pare a IA com `docker compose stop ia`, repita e registre o comportamento e prints. O sistema integrado ainda **não foi executado** durante a preparação deste material.
