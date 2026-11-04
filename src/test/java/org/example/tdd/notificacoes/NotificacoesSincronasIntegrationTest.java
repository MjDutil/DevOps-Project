package org.example.tdd.notificacoes;

import org.example.tdd.gamificacao.api.ConclusaoCurso;
import org.example.tdd.entity.AlunoEntity;
import org.example.tdd.repository.AlunoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"notificacoes.habilitadas=true", "notificacoes.modo=sincrono",
        "spring.datasource.url=jdbc:h2:mem:d4sync;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"})
@Import(NotificacoesIntegrationTest.Config.class)
class NotificacoesSincronasIntegrationTest {
    @Autowired ConclusaoCurso conclusao;
    @Autowired AlunoRepository repository;
    @Autowired JdbcTemplate jdbc;
    @Autowired NotificacoesIntegrationTest.Controlado consumidor;

    @Test void sucessoSincronoDeveTerminarNaThreadChamadora() {
        consumidor.falhar = false; consumidor.finalizados.clear();
        Long id = repository.saveAndFlush(new AlunoEntity(8.0, 0)).getId();
        assertEquals(3, conclusao.concluir(id).cursosLiberados());
        assertFalse(consumidor.finalizados.isEmpty());
        assertEquals(Thread.currentThread().getName(), consumidor.thread);
        assertEquals(3, jdbc.queryForObject("select cursos_liberados from alunos where id=?", Integer.class, id));
    }
    @Test void falhaSincronaNaoPodeRetornarErroAposCommit() {
        consumidor.falhar = true;
        Long id = repository.saveAndFlush(new AlunoEntity(8.0, 0)).getId();
        assertEquals(3, assertDoesNotThrow(() -> conclusao.concluir(id)).cursosLiberados());
        assertEquals(3, jdbc.queryForObject("select cursos_liberados from alunos where id=?", Integer.class, id));
    }
}
