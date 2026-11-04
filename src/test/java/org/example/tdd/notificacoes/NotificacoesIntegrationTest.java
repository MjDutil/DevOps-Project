package org.example.tdd.notificacoes;

import java.util.concurrent.*;
import org.example.tdd.gamificacao.api.*;
import org.example.tdd.entity.AlunoEntity;
import org.example.tdd.repository.AlunoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"notificacoes.habilitadas=true", "notificacoes.modo=assincrono",
        "notificacoes.fila-capacidade=1", "spring.datasource.url=jdbc:h2:mem:d4async;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"})
@Import(NotificacoesIntegrationTest.Config.class)
@ExtendWith(OutputCaptureExtension.class)
class NotificacoesIntegrationTest {
    @TestConfiguration
    static class Config {
        @Bean @Primary Controlado controlado() { return new Controlado(); }
    }
    static class Controlado implements NotificadorConclusao {
        final BlockingQueue<ConclusaoCursoProcessada> iniciados = new LinkedBlockingQueue<>();
        final BlockingQueue<ConclusaoCursoProcessada> finalizados = new LinkedBlockingQueue<>();
        volatile CountDownLatch liberar = new CountDownLatch(0);
        volatile boolean falhar;
        volatile String thread;
        @Override public void enviar(ConclusaoCursoProcessada evento) {
            thread = Thread.currentThread().getName();
            iniciados.add(evento);
            try {
                if (!liberar.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Teste não liberou consumidor");
                if (falhar) throw new IllegalStateException("Consumidor indisponível");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            } finally { finalizados.add(evento); }
        }
    }
    @Autowired ConclusaoCurso conclusao;
    @Autowired AlunoRepository repository;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager tx;
    @Autowired ThreadPoolExecutor notificacoesExecutor;
    @Autowired Controlado consumidor;

    @BeforeEach void preparar() throws Exception {
        notificacoesExecutor.submit(() -> {}).get(5, TimeUnit.SECONDS);
        consumidor.iniciados.clear(); consumidor.finalizados.clear();
        consumidor.liberar = new CountDownLatch(0); consumidor.falhar = false;
        jdbc.update("delete from alunos");
    }
    @AfterEach void liberar() { consumidor.liberar.countDown(); }
    private Long aluno(double media) { return repository.saveAndFlush(new AlunoEntity(media, 0)).getId(); }
    private int saldo(Long id) { return jdbc.queryForObject("select cursos_liberados from alunos where id=?", Integer.class, id); }
    private void esvaziar() throws Exception { notificacoesExecutor.submit(() -> {}).get(5, TimeUnit.SECONDS); }

    @Test void commitDeveEnviarRetratoNaThreadDedicada() throws Exception {
        Long id = aluno(8);
        assertEquals(3, conclusao.concluir(id).cursosLiberados());
        var evento = consumidor.finalizados.poll(5, TimeUnit.SECONDS);
        assertNotNull(evento); assertEquals(id, evento.alunoId());
        assertEquals(3, evento.cursosLiberados()); assertEquals(3, saldo(id));
        assertNotNull(evento.eventoId()); assertNotNull(evento.ocorridoEm());
        assertEquals("notificacoes-d4", consumidor.thread);
    }
    @Test void rollbackNaoDeveEnviar() throws Exception {
        Long id = aluno(8);
        new TransactionTemplate(tx).executeWithoutResult(status -> {
            conclusao.concluir(id);
            assertTrue(consumidor.iniciados.isEmpty());
            status.setRollbackOnly();
        });
        esvaziar(); assertTrue(consumidor.iniciados.isEmpty()); assertEquals(0, saldo(id));
    }
    @Test void envioSoPodeComecarDepoisDoCommitExterno() throws Exception {
        Long id = aluno(8);
        new TransactionTemplate(tx).executeWithoutResult(status -> {
            conclusao.concluir(id);
            assertTrue(consumidor.iniciados.isEmpty());
        });
        assertNotNull(consumidor.finalizados.poll(5, TimeUnit.SECONDS)); assertEquals(3, saldo(id));
    }
    @Test void alunoAusenteNaoDeveEnviar() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> conclusao.concluir(Long.MAX_VALUE));
        esvaziar(); assertTrue(consumidor.iniciados.isEmpty());
    }
    @Test void mediaSeteMantemRegraEEventoRepresentaOperacaoSemBonus() throws Exception {
        Long id = aluno(7); assertEquals(0, conclusao.concluir(id).cursosLiberados());
        var evento = consumidor.finalizados.poll(5, TimeUnit.SECONDS);
        assertNotNull(evento); assertEquals(0, evento.cursosLiberados()); assertEquals(0, saldo(id));
    }
    @Test void falhaAssincronaNaoDeveDesfazerConclusao(CapturedOutput output) throws Exception {
        consumidor.falhar = true; Long id = aluno(8);
        assertEquals(3, conclusao.concluir(id).cursosLiberados());
        assertNotNull(consumidor.finalizados.poll(5, TimeUnit.SECONDS)); esvaziar();
        assertEquals(3, saldo(id)); assertTrue(output.getAll().contains("D4 FALHA"));
    }
    @Test void filaCheiaDescartaSemExecutarNaThreadChamadora(CapturedOutput output) throws Exception {
        consumidor.liberar = new CountDownLatch(1); Long id = aluno(8);
        conclusao.concluir(id);
        assertNotNull(consumidor.iniciados.poll(5, TimeUnit.SECONDS));
        conclusao.concluir(id); // segundo evento fica na fila de capacidade 1
        assertEquals(1, notificacoesExecutor.getQueue().size());
        assertEquals(9, conclusao.concluir(id).cursosLiberados()); // terceiro é descartado
        assertEquals(9, saldo(id)); assertTrue(output.getAll().contains("D4 DESCARTADA"));
        assertTrue(consumidor.iniciados.isEmpty());
        consumidor.liberar.countDown();
        assertNotNull(consumidor.finalizados.poll(5, TimeUnit.SECONDS));
        assertNotNull(consumidor.finalizados.poll(5, TimeUnit.SECONDS));
        esvaziar(); assertEquals(1, consumidor.iniciados.size());
        assertEquals("notificacoes-d4", consumidor.thread);
    }
}
