package org.example.tdd;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.example.tdd.repository.AlunoRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP via DispatcherServlet + serviços reais + JPA/H2, sem mocks ou transação de teste. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:desafio1;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class AlunoHttpJpaIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired AlunoRepository repository;
    private MockMvc http;

    @BeforeEach
    void preparar() {
        http = MockMvcBuilders.webAppContextSetup(context).build();
        jdbc.update("delete from alunos");
    }

    @ParameterizedTest
    @CsvSource({"8.0,3", "7.0,0", "6.5,0"})
    void devePreservarConclusaoEGravarNoBanco(double media, int cursos) throws Exception {
        long id = criar(media);
        http.perform(post("/alunos/{id}/concluir-curso", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.media").value(media))
                .andExpect(jsonPath("$.cursosLiberados").value(cursos));
        assertEquals(cursos, jdbc.queryForObject(
                "select cursos_liberados from alunos where id=?", Integer.class, id));
    }

    @ParameterizedTest
    @CsvSource({"10,8,3,1", "8,10,3,0", "10,8,0,0"})
    void devePreservarForumESomarComConclusao(int topicos, int outros, int ajuda, int bonus)
            throws Exception {
        long id = criar(8.0);
        http.perform(post("/alunos/{id}/recompensar-forum", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topicosCriados\":" + topicos
                                + ",\"maiorQuantidadeTopicosDosOutros\":" + outros
                                + ",\"comentariosDeAjuda\":" + ajuda + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cursosLiberados").value(bonus));
        concluir(id);
        assertEquals(3 + bonus, jdbc.queryForObject(
                "select cursos_liberados from alunos where id=?", Integer.class, id));
    }

    @Test
    void mediaAlteradaEPersistidaDeveSerUsadaNaConclusaoSeguinte() throws Exception {
        long id = criar(6.5);
        var aluno = repository.findById(id).orElseThrow();
        aluno.setMedia(8.0);
        repository.saveAndFlush(aluno);
        assertEquals(8.0, jdbc.queryForObject(
                "select media from alunos where id=?", Double.class, id));
        http.perform(post("/alunos/{id}/concluir-curso", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.media").value(8.0))
                .andExpect(jsonPath("$.cursosLiberados").value(3));
        assertEquals(3, jdbc.queryForObject(
                "select cursos_liberados from alunos where id=?", Integer.class, id));
    }

    @Test
    void deveManterBasicoAbaixoDoLimitePremium() throws Exception {
        long id = criar(8.0);
        for (int i = 0; i < 3; i++) concluir(id);
        http.perform(post("/alunos/{id}/verificar-premium", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cursosLiberados").value(9))
                .andExpect(jsonPath("$.plano").value("Basico"))
                .andExpect(jsonPath("$.voucherProjetosReais").value(false))
                .andExpect(jsonPath("$.moedas").value(0));
    }

    @Test
    void devePreservarPremiumVoucherEMoedasAposNovaConclusao() throws Exception {
        long id = criar(8.0);
        for (int i = 0; i < 4; i++) concluir(id);
        for (int i = 0; i < 2; i++) {
            http.perform(post("/alunos/{id}/verificar-premium", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cursosLiberados").value(12))
                    .andExpect(jsonPath("$.plano").value("Premium"))
                    .andExpect(jsonPath("$.voucherProjetosReais").value(true))
                    .andExpect(jsonPath("$.moedas").value(3));
        }
        concluir(id);
        assertEquals(15, jdbc.queryForObject(
                "select cursos_liberados from alunos where id=?", Integer.class, id));
        assertEquals("Premium", jdbc.queryForObject(
                "select plano from alunos where id=?", String.class, id));
        assertEquals(true, jdbc.queryForObject(
                "select voucher_projetos_reais from alunos where id=?", Boolean.class, id));
        assertEquals(3, jdbc.queryForObject(
                "select moedas from alunos where id=?", Integer.class, id));
    }

    private long criar(double media) throws Exception {
        String body = http.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"media\":" + media + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.media").value(media))
                .andExpect(jsonPath("$.cursosLiberados").value(0))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(body, "$.id").longValue();
    }

    private void concluir(long id) throws Exception {
        http.perform(post("/alunos/{id}/concluir-curso", id)).andExpect(status().isOk());
    }
}
