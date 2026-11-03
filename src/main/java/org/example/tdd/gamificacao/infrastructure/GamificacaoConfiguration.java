package org.example.tdd.gamificacao.infrastructure;

import org.example.tdd.gamificacao.application.AlunoStore;
import org.example.tdd.gamificacao.application.ConcluirCursoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GamificacaoConfiguration {
    @Bean
    ConcluirCursoUseCase concluirCursoUseCase(AlunoStore store) {
        return new ConcluirCursoUseCase(store);
    }
}
