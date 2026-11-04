"""Verifica o snapshot declarado, inclusive Jenkinsfile, antes de compilar."""
import hashlib
import json
from pathlib import Path

if __name__=="__main__":
    r=json.loads(Path("source-manifest.json").read_text())
    for nome,expected in r["files"].items():
        assert hashlib.sha256(Path(nome).read_bytes()).hexdigest()==expected,nome
    digest=hashlib.sha256(json.dumps(r["files"],sort_keys=True,separators=(",",":")).encode()).hexdigest()
    assert digest==r["snapshot_sha256"]
    print("Base:",r["base_commit"],"Snapshot:",digest,"Arquivos:",len(r["files"]))
