package org.example.tdd.gamificacao;

import java.util.function.Supplier;
import jakarta.persistence.EntityManagerFactory;
import org.example.tdd.entity.AlunoEntity;
import org.example.tdd.gamificacao.api.ConclusaoCurso;
import org.example.tdd.gamificacao.application.AlunoSnapshot;
import org.example.tdd.gamificacao.application.AlunoStore;
import org.example.tdd.repository.AlunoRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

/** Mesmo contrato do fake, com adapter real e commits JPA/H2 entre as operações. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:desafio2;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.stat=OFF",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF"
})
class JpaAlunoStoreContractTest extends AlunoStoreContract {
    @Autowired AlunoStore adapter;
    @Autowired AlunoRepository repository;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired ConclusaoCurso fachada;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManagerFactory entityManagerFactory;

    @Override protected AlunoStore store() { return adapter; }
    @Override protected void limpar() { jdbc.update("delete from alunos"); }
    @Override protected AlunoSnapshot inserir(double media, int cursos) {
        var aluno = repository.saveAndFlush(new AlunoEntity(media, cursos));
        return new AlunoSnapshot(aluno.getId(), media, cursos);
    }
    @Override protected <T> T executar(Supplier<T> operacao) {
        return new TransactionTemplate(transactionManager).execute(status -> operacao.get());
    }

    @Test
    void fachadaDeveComitarComUmaLeituraEUmaAtualizacao() {
        var aluno = inserir(8.0, 2);
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        assertEquals(5, fachada.concluir(aluno.id()).cursosLiberados());
        assertEquals(2, statistics.getPrepareStatementCount(), "Um SELECT e um UPDATE");
        assertEquals(1, statistics.getEntityLoadCount(), "Aluno carregado uma única vez");
        assertEquals(5, jdbc.queryForObject(
                "select cursos_liberados from alunos where id=?", Integer.class, aluno.id()));
        System.out.println("D2 JPA: conclusão com 1 entidade carregada e 2 statements (SELECT + UPDATE).");
    }

    @Test
    void rollbackDeveDesfazerConclusao() {
        var aluno = inserir(8.0, 2);
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertEquals(5, fachada.concluir(aluno.id()).cursosLiberados());
            status.setRollbackOnly();
        });
        assertEquals(2, jdbc.queryForObject(
                "select cursos_liberados from alunos where id=?", Integer.class, aluno.id()));
    }

    @Test
    void adapterDeEscritaDeveExigirTransacao() {
        var aluno = inserir(8.0, 2);
        assertThrows(IllegalTransactionStateException.class,
                () -> adapter.atualizarCursos(aluno, 5));
        assertEquals(2, jdbc.queryForObject(
                "select cursos_liberados from alunos where id=?", Integer.class, aluno.id()));
    }
}
