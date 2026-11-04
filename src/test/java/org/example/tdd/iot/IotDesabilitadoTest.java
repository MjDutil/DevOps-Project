package org.example.tdd.iot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class IotDesabilitadoTest {
    @Autowired WebApplicationContext context;
    @Test void padraoNaoExpoeEntradaNemCriaRegistro() throws Exception {
        assertTrue(context.getBeansOfType(IotController.class).isEmpty());
        assertTrue(context.getBeansOfType(RegistroEventosIot.class).isEmpty());
        MockMvcBuilders.webAppContextSetup(context).build().perform(post("/iot/eventos")).andExpect(status().isNotFound());
    }
}
