package org.example.tdd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.example.tdd.gamificacao.api.ConclusaoCurso;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
class TddApplicationTests {

    @Autowired ConfigurableApplicationContext context;

    @Test
    void contextLoads() {
        assertTrue(context.isActive());
        assertNotNull(context.getBean(ConclusaoCurso.class));
    }

}
