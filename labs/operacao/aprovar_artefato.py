"""Arquiva somente o JAR que passou pelas etapas anteriores."""
import hashlib
import json
import os
from pathlib import Path
import shutil

if __name__=="__main__":
    jar=Path("target/TDD-0.0.1-SNAPSHOT.jar")
    source=json.loads(Path("source-manifest.json").read_text())
    dest=Path("aprovado");dest.mkdir(exist_ok=True)
    shutil.copyfile(jar,dest/"app.jar")
    r={"base_commit":source["base_commit"],"snapshot_sha256":source["snapshot_sha256"],
       "jar_sha256":hashlib.sha256(jar.read_bytes()).hexdigest(),"jenkins_build":os.environ["BUILD_NUMBER"]}
    (dest/"manifest.json").write_text(json.dumps(r,indent=2)+"\n")
    print(json.dumps(r))
