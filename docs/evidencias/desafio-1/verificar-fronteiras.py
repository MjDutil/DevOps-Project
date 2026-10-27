"""Executar da raiz: python3 docs/evidencias/desafio-1/verificar-fronteiras.py.

Inspeção estática de imports explícitos; não substitui isolamento Maven/JPMS.
"""
from pathlib import Path

raiz = Path(__file__).resolve().parents[3] / "src/main/java/org/example/tdd"
for arquivo in sorted(raiz.rglob("*.java")):
    pacote = arquivo.relative_to(raiz).parts
    imports = [linha for linha in arquivo.read_text().splitlines()
               if linha.startswith("import ")]
    if pacote[0] != "gamificacao":
        proibidos = [linha for linha in imports
                     if "org.example.tdd.gamificacao." in linha
                     and "org.example.tdd.gamificacao.api." not in linha]
        assert not proibidos, (arquivo, proibidos)
    if pacote[:2] in [("gamificacao", "application"), ("gamificacao", "api")] \
            or pacote[0] == "domain":
        proibidos = [linha for linha in imports if any(nome in linha for nome in (
            "org.springframework", "jakarta.persistence", "org.example.tdd.repository",
            "org.example.tdd.entity", "gamificacao.infrastructure"))]
        assert not proibidos, (arquivo, proibidos)

print("PASSOU: clientes externos ao módulo importam somente gamificacao.api.")
print("PASSOU: domínio, aplicação e API sem imports Spring/JPA/repository/entity/infraestrutura.")
print("Limite: inspeção dos imports explícitos; não garante isolamento Maven/JPMS/reflexão.")
