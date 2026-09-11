package org.example.tdd.dto;

public record ParticipacaoForumRequestDto(
        Integer topicosCriados,
        Integer maiorQuantidadeTopicosDosOutros,
        Integer comentariosDeAjuda
) {
}