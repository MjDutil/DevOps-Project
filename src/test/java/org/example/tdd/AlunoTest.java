package org.example.tdd;

import org.example.tdd.domain.Aluno;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlunoTest {

    //Isadora
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

    //Felipe

    @Test
    void deveTornarAlunoPremiumAoConquistarDozeCursos() {
        var aluno = new Aluno("Felipe");

        aluno.adicionarCursos(12);
        aluno.verificarPlanoPremium();

        assertEquals("Premium", aluno.getPlano());
    }

    @Test
    void deveReceberVoucherAoSeTornarPremium() {
        var aluno = new Aluno("Felipe");

        aluno.adicionarCursos(12);
        aluno.verificarPlanoPremium();

        assertTrue(aluno.isVoucherProjetosReais());
    }

    @Test
    void deveReceberTresMoedasAoSeTornarPremium() {
        var aluno = new Aluno("Felipe");

        aluno.adicionarCursos(12);
        aluno.verificarPlanoPremium();

        assertEquals(3, aluno.getMoedas());
    }

    @Test
    void naoDeveTornarAlunoPremiumComMenosDeDozeCursos() {
        var aluno = new Aluno("Felipe");

        aluno.adicionarCursos(11);
        aluno.verificarPlanoPremium();

        assertEquals("Basico", aluno.getPlano());
        assertFalse(aluno.isVoucherProjetosReais());
        assertEquals(0, aluno.getMoedas());
    }
}