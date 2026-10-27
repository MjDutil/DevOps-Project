package org.example.tdd.gamificacao.application;

import org.example.tdd.domain.Aluno;

/** Fatia vertical incremental da capacidade de gamificação (B1 D1/D2). */
public class ConcluirCursoUseCase {
    private final AlunoStore store;

    public ConcluirCursoUseCase(AlunoStore store) {
        this.store = store;
    }

    public AlunoSnapshot executar(Long id) {
        AlunoSnapshot atual = store.buscar(id)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado: " + id));
        Aluno regra = new Aluno("Aluno " + id);
        regra.concluirCurso(atual.media());
        AlunoSnapshot atualizado = new AlunoSnapshot(
                atual.id(), atual.media(), atual.cursosLiberados() + regra.getCursosLiberados());
        return store.salvar(atualizado);
    }
}
