package org.example.tdd;

import org.example.tdd.domain.Aluno;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlunoTest {

    @Test
    void deveLiberarTresCursosQuandoMediaForSuperiorASete() {

        Aluno aluno = new Aluno(8.0);

        aluno.concluirCurso();

        assertEquals(3, aluno.getCursosLiberados());
    }

    @Test
    void naoDeveLiberarCursosQuandoMediaForIgualASete() {

        Aluno aluno = new Aluno(7.0);

        aluno.concluirCurso();

        assertEquals(0, aluno.getCursosLiberados());
    }

    @Test
    void naoDeveLiberarCursosQuandoMediaForInferiorASete() {

        Aluno aluno = new Aluno(6.5);

        aluno.concluirCurso();

        assertEquals(0, aluno.getCursosLiberados());
    }
}