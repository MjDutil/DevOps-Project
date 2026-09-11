package org.example.tdd.dto;

public record PlanoPremiumResponseDto(
        Long id,
        Integer cursosLiberados,
        String plano,
        Boolean voucherProjetosReais,
        Integer moedas
) {
}