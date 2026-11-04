package org.example.tdd.ia;

import java.util.List;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final Counter fallbackRecomendacao;
    private final Counter fallbackAssistente;

    /** Mantém o construtor usado pelos testes D3, sem dependência de contexto Spring. */
    public IaClient(String baseUrl) {
        this(baseUrl, new SimpleMeterRegistry());
    }

    @Autowired
    public IaClient(@Value("${ia.base-url:http://localhost:8000}") String baseUrl, MeterRegistry registry) {
        fallbackRecomendacao = Counter.builder("ia.fallback").tag("operacao", "recomendacao")
                .description("Resposta degradada da IA, sem dados pessoais nas etiquetas").register(registry);
        fallbackAssistente = Counter.builder("ia.fallback").tag("operacao", "assistente")
                .description("Resposta degradada da IA, sem dados pessoais nas etiquetas").register(registry);
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
            return resposta != null ? resposta : fallbackAssistente();
        } catch (RestClientException ex) {
            return fallbackAssistente();
        }
    }

    private Map<String, Object> fallbackAssistente() {
        fallbackAssistente.increment();
        return Map.of("status", "indisponivel", "resposta", "Assistente temporariamente indisponível");
    }

    private Map<String, Object> fallbackRecomendacao(Long alunoId) {
        fallbackRecomendacao.increment();
        return Map.of("aluno_id", alunoId, "recomendacoes", List.of(), "status", "indisponivel");
    }
}
