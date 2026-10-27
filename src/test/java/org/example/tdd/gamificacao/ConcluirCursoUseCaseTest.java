package org.example.tdd.gamificacao;

import org.example.tdd.gamificacao.application.AlunoSnapshot;
import org.example.tdd.gamificacao.application.ConcluirCursoUseCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConcluirCursoUseCaseTest {
    @Test void deveLiberarTresCursosAcimaDeSeteSemJPA() {
        FakeAlunoStore fake = new FakeAlunoStore();
        fake.inserir(new AlunoSnapshot(1L, 8.0, 2));
        var saida = new ConcluirCursoUseCase(fake).executar(1L);
        assertEquals(5, saida.cursosLiberados());
    }

    @Test void naoDeveLiberarCursosComMediaSete() {
        FakeAlunoStore fake = new FakeAlunoStore();
        fake.inserir(new AlunoSnapshot(1L, 7.0, 2));
        var saida = new ConcluirCursoUseCase(fake).executar(1L);
        assertEquals(2, saida.cursosLiberados());
    }

    @Test void deveRetornarErroParaAlunoNaoEncontrado() {
        FakeAlunoStore fake = new FakeAlunoStore();
        assertThrows(IllegalArgumentException.class, () -> new ConcluirCursoUseCase(fake).executar(22L));
    }

    @Test void naoDeveLiberarCursosAbaixoDeSete() {
        FakeAlunoStore fake = new FakeAlunoStore();
        fake.inserir(new AlunoSnapshot(1L, 6.5, 2));
        var saida = new ConcluirCursoUseCase(fake).executar(1L);
        assertEquals(2, saida.cursosLiberados());
        assertEquals(saida, fake.buscar(1L).orElseThrow());
    }

    @Test void deveAcumularCursosEmConclusoesSucessivas() {
        FakeAlunoStore fake = new FakeAlunoStore();
        fake.inserir(new AlunoSnapshot(1L, 8.0, 0));
        var casoDeUso = new ConcluirCursoUseCase(fake);
        for (int i = 0; i < 4; i++) casoDeUso.executar(1L);
        assertEquals(12, fake.buscar(1L).orElseThrow().cursosLiberados());
    }
}
