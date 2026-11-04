"""Falha real de JUnit somente no workspace descartável do Jenkins."""
import os
from pathlib import Path

if __name__=="__main__":
    assert os.environ.get("BUILD_NUMBER") and os.environ.get("WORKSPACE"), "Somente Jenkins"
    assert Path.cwd().resolve()==Path(os.environ["WORKSPACE"]).resolve() and Path("source-manifest.json").is_file() and not Path(".git").exists(), "Somente workspace descartável"
    p=Path("src/test/java/org/example/tdd/d6/PipelineBloqueioTest.java")
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text('''package org.example.tdd.d6;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PipelineBloqueioTest {
    @Test void deveBloquearPromocaoDoArtefato() {
        assertTrue(false, "Falha controlada D6: somente workspace Jenkins descartavel");
    }
}
''')
    print("Teste de bloqueio criado exclusivamente no workspace Jenkins.")
