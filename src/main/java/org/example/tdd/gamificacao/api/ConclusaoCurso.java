package org.example.tdd.gamificacao.api;

/** Contrato público do módulo. Clientes não precisam conhecer persistência ou casos de uso. */
public interface ConclusaoCurso {
    Resultado concluir(Long alunoId);

    record Resultado(Long id, Double media, Integer cursosLiberados) {}
}
