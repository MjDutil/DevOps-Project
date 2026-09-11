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

    //maju

    @Test
    void deveLiberarUmCursoParaQuemMaisCriouTopicosEAjudouComComentarios() {
        var aluno = new Aluno("Maria Julia");

        aluno.recompensarParticipacaoForum(10, 8, 3);

        assertEquals(1, aluno.getCursosLiberados());
    }

    @Test
    void naoDeveLiberarCursoSeAlunoNaoForQuemMaisCriouTopicos() {
        var aluno = new Aluno("Maria Julia");

        aluno.recompensarParticipacaoForum(8, 10, 3);

        assertEquals(0, aluno.getCursosLiberados());
    }

    @Test
    void naoDeveLiberarCursoSeAlunoNaoAjudouComComentarios() {
        var aluno = new Aluno("Maria Julia");

        aluno.recompensarParticipacaoForum(10, 8, 0);

        assertEquals(0, aluno.getCursosLiberados());
    }

    @Test
    void deveRetornarNomeDoAluno() {
        var aluno = new Aluno("Maria Julia");

        assertEquals("Maria Julia", aluno.getNome());
    }
}