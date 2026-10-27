package org.example.tdd.gamificacao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.tdd.gamificacao.application.AlunoSnapshot;
import org.example.tdd.gamificacao.application.AlunoStore;
import org.example.tdd.gamificacao.application.ConcluirCursoUseCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConcluirCursoUseCaseTest {
    /** Fake adapter: nenhum banco de dados é necessário no teste do caso de uso. */
    static class FakeAlunoStore implements AlunoStore {
        final Map<Long, AlunoSnapshot> dados = new HashMap<>();
        public Optional<AlunoSnapshot> buscar(Long id) { return Optional.ofNullable(dados.get(id)); }
        public AlunoSnapshot salvar(AlunoSnapshot aluno) { dados.put(aluno.id(), aluno); return aluno; }
    }

    @Test void deveLiberarTresCursosAcimaDeSeteSemJPA() {
        FakeAlunoStore fake = new FakeAlunoStore();
        fake.dados.put(1L, new AlunoSnapshot(1L, 8.0, 2));
        var saida = new ConcluirCursoUseCase(fake).executar(1L);
        assertEquals(5, saida.cursosLiberados());
    }

    @Test void naoDeveLiberarCursosComMediaSete() {
        FakeAlunoStore fake = new FakeAlunoStore();
        fake.dados.put(1L, new AlunoSnapshot(1L, 7.0, 2));
        var saida = new ConcluirCursoUseCase(fake).executar(1L);
        assertEquals(2, saida.cursosLiberados());
    }

    @Test void deveRetornarErroParaAlunoNaoEncontrado() {
        FakeAlunoStore fake = new FakeAlunoStore();
        assertThrows(IllegalArgumentException.class, () -> new ConcluirCursoUseCase(fake).executar(22L));
    }

    @Test void naoDeveLiberarCursosAbaixoDeSete() {
        FakeAlunoStore fake = new FakeAlunoStore();
        fake.dados.put(1L, new AlunoSnapshot(1L, 6.5, 2));
        var saida = new ConcluirCursoUseCase(fake).executar(1L);
        assertEquals(2, saida.cursosLiberados());
        assertEquals(saida, fake.dados.get(1L));
    }

    @Test void deveAcumularCursosEmConclusoesSucessivas() {
        FakeAlunoStore fake = new FakeAlunoStore();
        fake.dados.put(1L, new AlunoSnapshot(1L, 8.0, 0));
        var casoDeUso = new ConcluirCursoUseCase(fake);
        for (int i = 0; i < 4; i++) casoDeUso.executar(1L);
        assertEquals(12, fake.dados.get(1L).cursosLiberados());
    }
}
