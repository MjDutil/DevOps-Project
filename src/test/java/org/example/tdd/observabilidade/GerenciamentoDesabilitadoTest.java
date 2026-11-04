package org.example.tdd.observabilidade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class GerenciamentoDesabilitadoTest {
    @Autowired WebApplicationContext context;
    @Test void naoExpoeGerenciamentoNaConfiguracaoPadrao() throws Exception {
        var http=MockMvcBuilders.webAppContextSetup(context).build();
        for(String path:new String[]{"/actuator/health","/actuator/prometheus","/actuator/env"})
            http.perform(get(path)).andExpect(status().isNotFound());
    }
}
