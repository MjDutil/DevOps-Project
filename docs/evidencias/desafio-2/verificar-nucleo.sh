#!/bin/sh
# Compila/roda o núcleo com classpath vazio de dependências externas.
set -eu
cd "$(dirname "$0")/../../.."
b1_d2_classes=$(mktemp -d "${TMPDIR:-/tmp}/b1-d2-isolado.XXXXXX")
trap 'rm -rf "$b1_d2_classes"' EXIT
javac -classpath "$b1_d2_classes" -d "$b1_d2_classes" \
    src/main/java/org/example/tdd/domain/Aluno.java \
    src/main/java/org/example/tdd/gamificacao/api/ConclusaoCurso.java \
    src/main/java/org/example/tdd/gamificacao/application/*.java \
    src/test/java/org/example/tdd/gamificacao/FakeAlunoStore.java \
    docs/arquitetura/labs/LabDomainSmoke.java
java -cp "$b1_d2_classes" LabDomainSmoke
jdeps -summary -recursive "$b1_d2_classes" > "$b1_d2_classes/dependencias.txt"
cat "$b1_d2_classes/dependencias.txt"
python3 - "$b1_d2_classes/dependencias.txt" <<'PY'
import pathlib
import sys
linhas = pathlib.Path(sys.argv[1]).read_text().splitlines()
assert linhas and all(linha.endswith(" -> java.base") for linha in linhas), linhas
print("PASSOU: bytecode isolado depende somente de java.base.")
PY
