package org.example.tdd.service;

import java.util.Optional;
import org.example.tdd.dto.ParticipacaoForumRequestDto;
import org.example.tdd.repository.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlunoServiceTest {
    private AlunoRepository repository;
    private AlunoService service;

    @BeforeEach
    void preparar() {
        repository = mock(AlunoRepository.class);
        service = new AlunoService(repository);
        when(repository.findById(99L)).thenReturn(Optional.empty());
    }

    @Test
    void forumDeAlunoAusenteDeveFalharSemPersistir() {
        var erro = assertThrows(RuntimeException.class, () -> service.recompensarParticipacaoForum(
                99L, new ParticipacaoForumRequestDto(10, 8, 3)));
        assertEquals("Aluno não encontrado", erro.getMessage());
        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void premiumDeAlunoAusenteDeveFalharSemPersistir() {
        var erro = assertThrows(RuntimeException.class, () -> service.verificarPlanoPremium(99L));
        assertEquals("Aluno não encontrado", erro.getMessage());
        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository);
    }
}
