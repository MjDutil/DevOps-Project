package org.example.tdd.ia;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class IaMetricasTest {
    private SimpleMeterRegistry registry;
    private MockRestServiceServer server;
    private IaClient client;
    @BeforeEach void preparar() {
        registry=new SimpleMeterRegistry();
        RestClient.Builder builder=spy(RestClient.builder());
        server=MockRestServiceServer.bindTo(builder).build();
        doReturn(builder).when(builder).requestFactory(any(ClientHttpRequestFactory.class));
        try(MockedStatic<RestClient> factory=mockStatic(RestClient.class)) {
            factory.when(RestClient::builder).thenReturn(builder);
            client=new IaClient("http://ia.test",registry);
        }
    }
    @AfterEach void verificar() { server.verify(); registry.close(); }
    private double contador(String operacao) { return registry.get("ia.fallback").tag("operacao",operacao).counter().count(); }
    @Test void iniciaEmZeroComEtiquetasFixasSemIdentificacaoPessoal() {
        assertEquals(0,contador("recomendacao")); assertEquals(0,contador("assistente"));
        assertEquals(2,registry.getMeters().size());
        for(var meter:registry.getMeters()) {
            assertEquals(1,meter.getId().getTags().size());
            assertEquals("operacao",meter.getId().getTags().get(0).getKey());
        }
    }
    @Test void falhasIncrementamCadaOperacaoSemAlterarContrato() {
        server.expect(requestTo("http://ia.test/recomendacoes/37")).andRespond(withServerError());
        server.expect(requestTo("http://ia.test/assistente?pergunta=teste")).andRespond(withServerError());
        assertEquals("indisponivel",client.recomendar(37L).get("status"));
        assertEquals("indisponivel",client.assistente("teste").get("status"));
        assertEquals(1,contador("recomendacao")); assertEquals(1,contador("assistente"));
    }
    @Test void recuperacaoNaoZeraNemIncrementaContador() {
        server.expect(requestTo("http://ia.test/recomendacoes/37")).andRespond(withServerError());
        server.expect(requestTo("http://ia.test/recomendacoes/37")).andRespond(withSuccess("{\"recomendacoes\":[\"Java\"]}",MediaType.APPLICATION_JSON));
        client.recomendar(37L);assertEquals(1,contador("recomendacao"));
        assertNotNull(client.recomendar(37L).get("recomendacoes"));
        assertEquals(1,contador("recomendacao"));assertEquals(0,contador("assistente"));
    }
    @Test void corpoAusenteTambemContaFallbackUmaVez() {
        server.expect(requestTo("http://ia.test/recomendacoes/37")).andRespond(withNoContent());
        server.expect(requestTo("http://ia.test/assistente?pergunta=teste")).andRespond(withSuccess("null",MediaType.APPLICATION_JSON));
        client.recomendar(37L);client.assistente("teste");
        assertEquals(1,contador("recomendacao"));assertEquals(1,contador("assistente"));
    }
}
