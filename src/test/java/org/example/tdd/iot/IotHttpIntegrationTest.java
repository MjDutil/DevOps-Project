package org.example.tdd.iot;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"iot.habilitado=true","iot.token=teste-iot-nao-producao",
        "spring.datasource.url=jdbc:h2:mem:d5;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
class IotHttpIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    private MockMvc http;
    @BeforeEach void preparar() { http=MockMvcBuilders.webAppContextSetup(context).build(); }
    private String corpo(UUID id) { return "{\"eventId\":\""+id+"\",\"dispositivoId\":\"dispositivo-01\",\"tipo\":\"PRESENCA\",\"ocorridoEm\":\"2026-11-04T12:00:00Z\",\"versao\":1}"; }
    @Test void aceitaEDetectaDuplicataSemAlterarAlunos() throws Exception {
        int antes=jdbc.queryForObject("select count(*) from alunos",Integer.class); var body=corpo(UUID.randomUUID());
        http.perform(post("/iot/eventos").header("Authorization","Bearer teste-iot-nao-producao").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("recebido"));
        http.perform(post("/iot/eventos").header("Authorization","Bearer teste-iot-nao-producao").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("duplicado"));
        assertEquals(antes,jdbc.queryForObject("select count(*) from alunos",Integer.class));
    }
    @Test void rejeitaTokenAusenteOuIncorreto() throws Exception {
        for (String token:new String[]{"","Bearer incorreto"}) {
            http.perform(post("/iot/eventos").header("Authorization",token).contentType(MediaType.APPLICATION_JSON).content(corpo(UUID.randomUUID())))
                    .andExpect(status().isUnauthorized());
        }
    }
    @Test void rejeitaPayloadInvalidoEConflito() throws Exception {
        var body=corpo(UUID.randomUUID());
        http.perform(post("/iot/eventos").header("Authorization","Bearer teste-iot-nao-producao").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        http.perform(post("/iot/eventos").header("Authorization","Bearer teste-iot-nao-producao").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        http.perform(post("/iot/eventos").header("Authorization","Bearer teste-iot-nao-producao").contentType(MediaType.APPLICATION_JSON).content(body.replace("PRESENCA","INICIO_ATIVIDADE")))
                .andExpect(status().isConflict());
    }
    @Test void habilitarSemTokenFalhaNaConfiguracao() {
        assertThrows(IllegalArgumentException.class, () -> new IotController(new RegistroEventosIot(2), ""));
    }
}
