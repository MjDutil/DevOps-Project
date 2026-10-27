package org.example.tdd.ia;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** B1 D3: contrato opcional com FastAPI; erro de IA nunca bloqueia Core. */
@Component
public class IaClient {
    private final RestClient client;

    public IaClient(@Value("${ia.base-url:http://localhost:8000}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(700);
        factory.setReadTimeout(1200);
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> recomendar(Long alunoId) {
        try {
            Map<String, Object> resposta = client.get()
                    .uri("/recomendacoes/{alunoId}", alunoId)
                    .retrieve().body(Map.class);
            return resposta != null ? resposta : fallbackRecomendacao(alunoId);
        } catch (RestClientException ex) {
            return fallbackRecomendacao(alunoId);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> assistente(String pergunta) {
        try {
            Map<String, Object> resposta = client.get()
                    .uri(uri -> uri.path("/assistente").queryParam("pergunta", pergunta).build())
                    .retrieve().body(Map.class);
            return resposta != null ? resposta : Map.of("status", "indisponivel", "resposta", "Assistente temporariamente indisponível");
        } catch (RestClientException ex) {
            return Map.of("status", "indisponivel", "resposta", "Assistente temporariamente indisponível");
        }
    }

    private Map<String, Object> fallbackRecomendacao(Long alunoId) {
        return Map.of("aluno_id", alunoId, "recomendacoes", List.of(), "status", "indisponivel");
    }
}
