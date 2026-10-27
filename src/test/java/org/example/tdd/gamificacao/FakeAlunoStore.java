package org.example.tdd.gamificacao;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.tdd.gamificacao.application.AlunoSnapshot;
import org.example.tdd.gamificacao.application.AlunoStore;

/** Adapter de teste em memória: somente JDK, sem contexto Spring ou banco. */
public class FakeAlunoStore implements AlunoStore {
    private final Map<Long, AlunoSnapshot> dados = new HashMap<>();

    public void inserir(AlunoSnapshot aluno) {
        dados.put(aluno.id(), aluno);
    }

    @Override
    public Optional<AlunoSnapshot> buscar(Long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public AlunoSnapshot atualizarCursos(AlunoSnapshot esperado, int cursosLiberados) {
        if (!esperado.equals(dados.get(esperado.id()))) {
            throw new ConcurrentModificationException("Aluno alterado ou removido: " + esperado.id());
        }
        var atualizado = new AlunoSnapshot(esperado.id(), esperado.media(), cursosLiberados);
        dados.put(esperado.id(), atualizado);
        return atualizado;
    }
}
