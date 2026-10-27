package org.example.tdd.gamificacao;

import java.util.ConcurrentModificationException;
import java.util.function.Supplier;
import org.example.tdd.gamificacao.application.AlunoSnapshot;
import org.example.tdd.gamificacao.application.AlunoStore;
import org.example.tdd.gamificacao.application.ConcluirCursoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/** Mesmos cenários para dois adapters; nenhuma dependência Spring/JPA no contrato. */
abstract class AlunoStoreContract {
    protected abstract AlunoStore store();
    protected abstract void limpar();
    protected abstract AlunoSnapshot inserir(double media, int cursos);

    protected <T> T executar(Supplier<T> operacao) {
        return operacao.get();
    }

    @BeforeEach
    void prepararContrato() {
        limpar();
    }

    @Test
    void deveBuscarSnapshotExistente() {
        var aluno = inserir(8.0, 2);
        assertEquals(aluno, executar(() -> store().buscar(aluno.id())).orElseThrow());
    }

    @Test
    void deveRetornarVazioParaAlunoAusente() {
        assertTrue(executar(() -> store().buscar(Long.MAX_VALUE)).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"8.0,5", "7.0,2", "6.5,2"})
    void mesmaRegraDeveFuncionarComQualquerAdapter(double media, int cursosEsperados) {
        var aluno = inserir(media, 2);
        var casoDeUso = new ConcluirCursoUseCase(store());
        var resultado = executar(() -> casoDeUso.executar(aluno.id()));
        assertEquals(new AlunoSnapshot(aluno.id(), media, cursosEsperados), resultado);
        assertEquals(resultado, executar(() -> store().buscar(aluno.id())).orElseThrow());
    }

    @Test
    void deveAcumularQuatroConclusoes() {
        var aluno = inserir(8.0, 0);
        var casoDeUso = new ConcluirCursoUseCase(store());
        for (int i = 0; i < 4; i++) executar(() -> casoDeUso.executar(aluno.id()));
        assertEquals(12, executar(() -> store().buscar(aluno.id())).orElseThrow().cursosLiberados());
    }

    @Test
    void deveFalharNoCasoDeUsoParaAlunoAusente() {
        var casoDeUso = new ConcluirCursoUseCase(store());
        assertThrows(IllegalArgumentException.class,
                () -> executar(() -> casoDeUso.executar(Long.MAX_VALUE)));
    }

    @Test
    void deveAtualizarSomenteSaldoPreservandoIdEMedia() {
        var aluno = inserir(9.0, 2);
        var salvo = executar(() -> store().atualizarCursos(aluno, 5));
        assertEquals(new AlunoSnapshot(aluno.id(), 9.0, 5), salvo);
        assertEquals(salvo, executar(() -> store().buscar(aluno.id())).orElseThrow());
    }

    @Test
    void deveRejeitarSaldoObsoletoSemPerderAtualizacaoAnterior() {
        var aluno = inserir(8.0, 2);
        executar(() -> store().atualizarCursos(aluno, 5));
        assertThrows(ConcurrentModificationException.class,
                () -> executar(() -> store().atualizarCursos(aluno, 99)));
        assertEquals(5, executar(() -> store().buscar(aluno.id())).orElseThrow().cursosLiberados());
    }

    @Test
    void deveRejeitarMediaObsoletaSemAlterarSaldo() {
        var aluno = inserir(8.0, 2);
        var obsoleto = new AlunoSnapshot(aluno.id(), 7.0, 2);
        assertThrows(ConcurrentModificationException.class,
                () -> executar(() -> store().atualizarCursos(obsoleto, 99)));
        assertEquals(aluno, executar(() -> store().buscar(aluno.id())).orElseThrow());
    }

    @Test
    void atualizarNaoDeveCriarAlunoAusente() {
        var ausente = new AlunoSnapshot(Long.MAX_VALUE, 8.0, 0);
        assertThrows(ConcurrentModificationException.class,
                () -> executar(() -> store().atualizarCursos(ausente, 3)));
        assertTrue(executar(() -> store().buscar(ausente.id())).isEmpty());
    }
}
