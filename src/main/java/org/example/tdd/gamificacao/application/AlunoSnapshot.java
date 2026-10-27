package org.example.tdd.gamificacao.application;

/** DTO puro da fronteira de persistência; não depende de JPA/Spring. */
public record AlunoSnapshot(Long id, Double media, Integer cursosLiberados) {}
