package org.example.tdd;

import org.example.tdd.domain.Aluno;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlunoTest {

    @Test
    void deveLiberarTresCursosParaMediaSuperiorASete() {
        var aluno = new Aluno("Isadora");

        aluno.concluirCurso(8.0);

        assertEquals(3, aluno.getCursosLiberados());
    }

    @Test
    void naoDeveLiberarCursosParaMediaIgualASete() {
        var aluno = new Aluno("Isadora");

        aluno.concluirCurso(7.0);

        assertEquals(0, aluno.getCursosLiberados());
    }

    @Test
    void naoDeveLiberarCursosParaMediaInferiorASete() {
        var aluno = new Aluno("Isadora");

        aluno.concluirCurso(6.5);

        assertEquals(0, aluno.getCursosLiberados());
    }
}