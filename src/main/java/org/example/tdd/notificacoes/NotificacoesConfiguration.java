package org.example.tdd.notificacoes;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "notificacoes.habilitadas", havingValue = "true")
public class NotificacoesConfiguration {
    @Bean(destroyMethod = "shutdownNow")
    ThreadPoolExecutor notificacoesExecutor(@Value("${notificacoes.fila-capacidade:16}") int capacidade) {
        return new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(capacidade), tarefa -> {
                    var thread = new Thread(tarefa, "notificacoes-d4");
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.AbortPolicy());
    }

    @Bean
    NotificadorConclusao notificadorConclusao(@Value("${notificacoes.url:http://127.0.0.1:8091}") String url) {
        return new HttpNotificadorConclusao(url);
    }

    @Bean
    NotificacaoConclusaoListener notificacaoConclusaoListener(NotificadorConclusao notificador,
            ThreadPoolExecutor notificacoesExecutor,
            @Value("${notificacoes.modo:assincrono}") String modo) {
        return new NotificacaoConclusaoListener(notificador, notificacoesExecutor, modo);
    }
}
