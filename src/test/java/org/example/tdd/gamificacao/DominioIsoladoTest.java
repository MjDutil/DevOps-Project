package org.example.tdd.gamificacao;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DominioIsoladoTest {
    @TempDir Path classes;

    @Test
    void deveCompilarEExecutarDominioAplicacaoEFakeSomenteComJdk() throws Exception {
        var argumentos = new ArrayList<String>();
        // O classpath de compilação contém somente o diretório temporário vazio.
        argumentos.addAll(java.util.List.of("-classpath", classes.toString(), "-d", classes.toString()));
        argumentos.addAll(java.util.List.of(
                "src/main/java/org/example/tdd/domain/Aluno.java",
                "src/main/java/org/example/tdd/gamificacao/api/ConclusaoCurso.java",
                "src/main/java/org/example/tdd/gamificacao/application/AlunoSnapshot.java",
                "src/main/java/org/example/tdd/gamificacao/application/AlunoStore.java",
                "src/main/java/org/example/tdd/gamificacao/application/ConcluirCursoUseCase.java",
                "src/test/java/org/example/tdd/gamificacao/FakeAlunoStore.java",
                "docs/arquitetura/labs/LabDomainSmoke.java"));
        var erros = new ByteArrayOutputStream();
        var compilador = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compilador, "Este teste requer JDK");
        assertEquals(0, compilador.run(null, erros, erros, argumentos.toArray(String[]::new)),
                () -> erros.toString(StandardCharsets.UTF_8));
        String executavelJava = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process processo = new ProcessBuilder(executavelJava, "-cp", classes.toString(), "LabDomainSmoke")
                .redirectErrorStream(true).start();
        try {
            assertTrue(processo.waitFor(20, java.util.concurrent.TimeUnit.SECONDS),
                    "Processo isolado deve terminar");
            String saida = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, processo.exitValue(), saida);
            assertTrue(saida.contains("PASSOU:"), saida);
            System.out.print(saida);
        } finally {
            if (processo.isAlive()) processo.destroyForcibly();
        }
    }
}
