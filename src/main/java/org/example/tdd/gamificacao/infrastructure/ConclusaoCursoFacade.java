package org.example.tdd.gamificacao.infrastructure;

import org.example.tdd.gamificacao.api.ConclusaoCurso;
import org.example.tdd.gamificacao.application.ConcluirCursoUseCase;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.example.tdd.gamificacao.api.ConclusaoCursoProcessada;
import java.time.Instant;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** Entrada transacional: mantém a tecnologia fora do contrato e do caso de uso. */
@Service
public class ConclusaoCursoFacade implements ConclusaoCurso {
    private final ConcluirCursoUseCase concluirCurso;
    private final ApplicationEventPublisher eventos;

    public ConclusaoCursoFacade(ConcluirCursoUseCase concluirCurso, ApplicationEventPublisher eventos) {
        this.concluirCurso = concluirCurso;
        this.eventos = eventos;
    }

    @Override
    @Transactional
    public Resultado concluir(Long alunoId) {
        var aluno = concluirCurso.executar(alunoId);
        eventos.publishEvent(new ConclusaoCursoProcessada(
                UUID.randomUUID(), aluno.id(), aluno.cursosLiberados(), Instant.now()));
        return new Resultado(aluno.id(), aluno.media(), aluno.cursosLiberados());
    }
}
