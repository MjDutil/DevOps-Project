package org.example.tdd.gamificacao.api;

import java.time.Instant;
import java.util.UUID;

/** Retrato de uma operação processada; não é histórico de curso identificado. */
public record ConclusaoCursoProcessada(UUID eventoId, Long alunoId,
                                     Integer cursosLiberados, Instant ocorridoEm) {}
