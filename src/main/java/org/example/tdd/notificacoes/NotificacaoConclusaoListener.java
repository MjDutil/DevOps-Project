package org.example.tdd.notificacoes;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.RejectedExecutionException;
import org.example.tdd.gamificacao.api.ConclusaoCursoProcessada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Entrega de melhor esforço somente após commit. Nenhuma escrita JPA aqui. */
public class NotificacaoConclusaoListener {
    private static final Logger log = LoggerFactory.getLogger(NotificacaoConclusaoListener.class);
    private final NotificadorConclusao notificador;
    private final ThreadPoolExecutor executor;
    private final boolean sincrono;

    public NotificacaoConclusaoListener(NotificadorConclusao notificador,
                                       ThreadPoolExecutor executor, String modo) {
        if (!modo.equals("sincrono") && !modo.equals("assincrono")) {
            throw new IllegalArgumentException("notificacoes.modo deve ser sincrono ou assincrono");
        }
        this.notificador = notificador;
        this.executor = executor;
        this.sincrono = modo.equals("sincrono");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aposCommit(ConclusaoCursoProcessada evento) {
        if (sincrono) {
            tentarEnviar(evento);
            return;
        }
        try {
            executor.execute(() -> tentarEnviar(evento));
            log.info("D4 ENFILEIRADA evento={} aluno={}", evento.eventoId(), evento.alunoId());
        } catch (RejectedExecutionException ex) {
            log.warn("D4 DESCARTADA evento={} aluno={} motivo=fila-cheia-ou-executor-encerrado",
                    evento.eventoId(), evento.alunoId());
        }
    }

    private void tentarEnviar(ConclusaoCursoProcessada evento) {
        try {
            notificador.enviar(evento);
            log.info("D4 ENTREGUE evento={} aluno={} thread={}", evento.eventoId(),
                    evento.alunoId(), Thread.currentThread().getName());
        } catch (RuntimeException ex) {
            log.warn("D4 FALHA evento={} aluno={} tipo={} thread={}", evento.eventoId(),
                    evento.alunoId(), ex.getClass().getSimpleName(), Thread.currentThread().getName());
        }
    }
}
