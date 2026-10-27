package org.example.tdd.gamificacao.application;

import java.util.Optional;

/** Porta do caso de uso: contrato independente da infraestrutura. */
public interface AlunoStore {
    Optional<AlunoSnapshot> buscar(Long id);
    AlunoSnapshot salvar(AlunoSnapshot aluno);
}
