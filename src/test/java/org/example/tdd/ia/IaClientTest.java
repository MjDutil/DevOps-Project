package org.example.tdd.ia;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/** HTTP simulado com conversão real: sem servidor Python, socket ou espera por timeout. */
class IaClientTest {
    private static final String BASE = "http://ia.test";
    private MockRestServiceServer server;
    private IaClient client;

    @BeforeEach
    void prepararTransporte() {
        RestClient.Builder builder = spy(RestClient.builder());
        server = MockRestServiceServer.bindTo(builder).build();
        // Preserva o transporte do MockRestServiceServer quando o construtor configura
        // sua factory. A aplicação e seu construtor permanecem inalterados.
        doReturn(builder).when(builder).requestFactory(any(ClientHttpRequestFactory.class));
        try (MockedStatic<RestClient> factory = mockStatic(RestClient.class)) {
            factory.when(RestClient::builder).thenReturn(builder);
            client = new IaClient(BASE);
        }
    }

    @AfterEach
    void verificarChamadas() {
        server.verify();
    }

    @Test
    void deveConverterRecomendacaoJsonEUsarIdNoCaminho() {
        server.expect(requestTo(BASE + "/recomendacoes/37"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"aluno_id\":37,\"recomendacoes\":[\"Java\"]}", MediaType.APPLICATION_JSON));
        assertEquals(Map.of("aluno_id", 37, "recomendacoes", List.of("Java")), client.recomendar(37L));
    }

    @Test
    void devePreservarPerguntaComEspacosAcentosECaracteresReservados() {
        String pergunta = "Como revisar equações & Java?";
        server.expect(request -> {
                    assertEquals(BASE + "/assistente", request.getURI().getScheme() + "://"
                            + request.getURI().getHost() + request.getURI().getPath());
                    String query = request.getURI().getRawQuery();
                    assertTrue(query.startsWith("pergunta="));
                    assertFalse(query.contains("&"), "& da pergunta deve estar codificado");
                    assertEquals(pergunta, URLDecoder.decode(query.substring("pergunta=".length()), StandardCharsets.UTF_8));
                })
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"resposta\":\"Revise os conceitos\"}", MediaType.APPLICATION_JSON));
        assertEquals(Map.of("resposta", "Revise os conceitos"), client.assistente(pergunta));
    }

    @Test
    void deveUsarFallbackQuandoNaoHaCorpoHttp() {
        esperarAmbos(withNoContent());
        verificarAmbosFallbacks();
    }

    @Test
    void deveUsarFallbackQuandoJsonRepresentaNull() {
        esperarAmbos(withSuccess("null", MediaType.APPLICATION_JSON));
        verificarAmbosFallbacks();
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {
            "BAD_REQUEST", "NOT_FOUND", "INTERNAL_SERVER_ERROR", "SERVICE_UNAVAILABLE"
    })
    void deveUsarFallbackParaErrosHttp(HttpStatus status) {
        esperarAmbos(withStatus(status));
        verificarAmbosFallbacks();
    }

    static Stream<IOException> falhasDeTransporte() {
        return Stream.of(new ConnectException("Connection refused"),
                new SocketTimeoutException("Read timed out"),
                new SocketTimeoutException("Connect timed out"));
    }

    @ParameterizedTest
    @MethodSource("falhasDeTransporte")
    void deveUsarFallbackEmIndisponibilidadeOuTimeout(IOException falha) {
        esperarAmbos(request -> { throw falha; });
        verificarAmbosFallbacks();
    }

    @Test
    void deveUsarFallbackParaJsonMalformado() {
        esperarAmbos(withSuccess("{invalido", MediaType.APPLICATION_JSON));
        verificarAmbosFallbacks();
    }

    @Test
    void deveUsarFallbackParaTipoDeConteudoIncompativel() {
        esperarAmbos(withSuccess("resposta inesperada", MediaType.TEXT_PLAIN));
        verificarAmbosFallbacks();
    }

    @Test
    void mapaSemCamposEsperadosERepassadoPoisNaoExisteValidacaoDeSchema() {
        esperarAmbos(withSuccess("{\"outro\":\"valor\"}", MediaType.APPLICATION_JSON));
        assertEquals(Map.of("outro", "valor"), client.recomendar(37L));
        assertEquals(Map.of("outro", "valor"), client.assistente("teste"));
    }

    @Test
    void chamadaPosteriorDeveFuncionarDepoisDeFalha() {
        server.expect(requestTo(BASE + "/recomendacoes/37")).andRespond(withServerError());
        server.expect(requestTo(BASE + "/recomendacoes/37"))
                .andRespond(withSuccess("{\"recomendacoes\":[\"Java\"]}", MediaType.APPLICATION_JSON));
        assertEquals(fallbackRecomendacao(), client.recomendar(37L));
        assertEquals(Map.of("recomendacoes", List.of("Java")), client.recomendar(37L));
    }

    private void esperarAmbos(ResponseCreator resposta) {
        server.expect(requestTo(BASE + "/recomendacoes/37"))
                .andExpect(method(HttpMethod.GET)).andRespond(resposta);
        server.expect(requestTo(BASE + "/assistente?pergunta=teste"))
                .andExpect(method(HttpMethod.GET)).andRespond(resposta);
    }

    private void verificarAmbosFallbacks() {
        assertEquals(fallbackRecomendacao(), client.recomendar(37L));
        assertEquals(Map.of("status", "indisponivel", "resposta", "Assistente temporariamente indisponível"),
                client.assistente("teste"));
    }

    private Map<String, Object> fallbackRecomendacao() {
        return Map.of("aluno_id", 37L, "recomendacoes", List.of(), "status", "indisponivel");
    }
}
