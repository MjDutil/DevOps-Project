package org.example.tdd.gamificacao.application;

import java.util.Optional;

/**
 * Porta de saída independente de infraestrutura. Atualiza somente os cursos de um
 * aluno existente se média e saldo ainda correspondem ao snapshot lido.
 * Estado alterado/removido gera ConcurrentModificationException, sem sobrescrita.
 */
public interface AlunoStore {
    Optional<AlunoSnapshot> buscar(Long id);
    AlunoSnapshot atualizarCursos(AlunoSnapshot esperado, int cursosLiberados);
}
