"""Protótipo da fronteira Python, sem modelo de ML/LLM real.
A finalidade é comprovar contrato HTTP e isolamento de falhas (B1 desafio 3).
"""
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="B1 - IA (laboratório)")

class Recomendacao(BaseModel):
    aluno_id: int
    recomendacoes: list[str]

@app.get("/health")
def health():
    return {"status": "ok"}

@app.get("/recomendacoes/{aluno_id}", response_model=Recomendacao)
def recomendacoes(aluno_id: int):
    return Recomendacao(aluno_id=aluno_id, recomendacoes=["Curso introdutório", "Atividade de revisão"])

@app.get("/assistente")
def assistente(pergunta: str):
    return {"resposta": f"Laboratório sem LLM: pergunta recebida ({len(pergunta)} caracteres)."}
