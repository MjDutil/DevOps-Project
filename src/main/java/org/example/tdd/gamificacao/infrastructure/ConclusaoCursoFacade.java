package org.example.tdd.gamificacao.infrastructure;

import org.example.tdd.gamificacao.api.ConclusaoCurso;
import org.example.tdd.gamificacao.application.ConcluirCursoUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Entrada transacional: mantém a tecnologia fora do contrato e do caso de uso. */
@Service
public class ConclusaoCursoFacade implements ConclusaoCurso {
    private final ConcluirCursoUseCase concluirCurso;

    public ConclusaoCursoFacade(ConcluirCursoUseCase concluirCurso) {
        this.concluirCurso = concluirCurso;
    }

    @Override
    @Transactional
    public Resultado concluir(Long alunoId) {
        var aluno = concluirCurso.executar(alunoId);
        return new Resultado(aluno.id(), aluno.media(), aluno.cursosLiberados());
    }
}
