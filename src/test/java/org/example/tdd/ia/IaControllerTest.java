package org.example.tdd.ia;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Executa os mappings e a serialização do controller real; substitui apenas o cliente externo. */
class IaControllerTest {
    private IaClient client;
    private MockMvc http;

    @BeforeEach
    void preparar() {
        client = mock(IaClient.class);
        http = MockMvcBuilders.standaloneSetup(new IaController(client)).build();
    }

    @Test
    void recomendacoesDeveRetornar200EJsonDoCliente() throws Exception {
        when(client.recomendar(37L)).thenReturn(Map.of("aluno_id", 37, "recomendacoes", List.of("Java")));
        http.perform(get("/ia/recomendacoes/37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aluno_id").value(37))
                .andExpect(jsonPath("$.recomendacoes[0]").value("Java"));
        verify(client).recomendar(37L);
    }

    @Test
    void assistenteDeveRepassarPerguntaERetornar200() throws Exception {
        when(client.assistente("Como estudar?")).thenReturn(Map.of("resposta", "Revise os conceitos"));
        http.perform(get("/ia/assistente").param("pergunta", "Como estudar?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resposta").value("Revise os conceitos"));
        verify(client).assistente("Como estudar?");
    }

    @Test
    void recomendacoesDegradadasDevemManter200ConformeContratoExistente() throws Exception {
        when(client.recomendar(37L)).thenReturn(Map.of("aluno_id", 37L,
                "status", "indisponivel", "recomendacoes", List.of()));
        http.perform(get("/ia/recomendacoes/37"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("indisponivel"))
                .andExpect(jsonPath("$.aluno_id").value(37))
                .andExpect(jsonPath("$.recomendacoes").isEmpty());
        verify(client).recomendar(37L);
    }

    @Test
    void assistenteDegradadoDeveManter200ConformeContratoExistente() throws Exception {
        when(client.assistente("teste")).thenReturn(Map.of("status", "indisponivel",
                "resposta", "Assistente temporariamente indisponível"));
        http.perform(get("/ia/assistente").param("pergunta", "teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("indisponivel"))
                .andExpect(jsonPath("$.resposta").value("Assistente temporariamente indisponível"));
        verify(client).assistente("teste");
    }

    @Test
    void perguntaAusenteDeveRetornar400SemChamarCliente() throws Exception {
        http.perform(get("/ia/assistente")).andExpect(status().isBadRequest());
        verifyNoInteractions(client);
    }

    @Test
    void idNaoNumericoDeveRetornar400SemChamarCliente() throws Exception {
        http.perform(get("/ia/recomendacoes/invalido")).andExpect(status().isBadRequest());
        verifyNoInteractions(client);
    }
}
