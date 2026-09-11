package org.example.tdd.dto;

public record AlunoResponseDto(
        Long id,
        Double media,
        Integer cursosLiberados
) {
}