package org.example.tdd.notificacoes;

import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NotificacoesDesabilitadasTest {
    @Autowired ApplicationContext context;
    @Test void padraoNaoDeveCriarClienteListenerOuExecutor() {
        assertTrue(context.getBeansOfType(NotificadorConclusao.class).isEmpty());
        assertTrue(context.getBeansOfType(NotificacaoConclusaoListener.class).isEmpty());
        assertTrue(context.getBeansOfType(ThreadPoolExecutor.class).isEmpty());
    }
}
