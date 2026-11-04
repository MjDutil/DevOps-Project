package org.example.tdd.notificacoes;

import org.example.tdd.gamificacao.api.ConclusaoCursoProcessada;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public class HttpNotificadorConclusao implements NotificadorConclusao {
    private final RestClient client;

    public HttpNotificadorConclusao(String url) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(700);
        factory.setReadTimeout(1200);
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    @Override
    public void enviar(ConclusaoCursoProcessada evento) {
        client.post().uri("/notificacoes").body(evento).retrieve().toBodilessEntity();
    }
}
