package org.example.tdd.notificacoes;

import org.example.tdd.gamificacao.api.ConclusaoCursoProcessada;

/** Saída opcional; o contrato não promete entrega durável. */
public interface NotificadorConclusao {
    void enviar(ConclusaoCursoProcessada evento);
}
