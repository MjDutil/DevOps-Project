package org.example.tdd.gamificacao.infrastructure;

import java.util.Optional;
import java.util.ConcurrentModificationException;
import org.example.tdd.entity.AlunoEntity;
import org.example.tdd.gamificacao.application.AlunoSnapshot;
import org.example.tdd.gamificacao.application.AlunoStore;
import org.example.tdd.repository.AlunoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Adapter de infraestrutura: JPA implementa o contrato do caso de uso. */
@Component
public class JpaAlunoStore implements AlunoStore {
    private final AlunoRepository repository;

    public JpaAlunoStore(AlunoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AlunoSnapshot> buscar(Long id) {
        return repository.findById(id).map(this::mapear);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public AlunoSnapshot atualizarCursos(AlunoSnapshot esperado, int cursosLiberados) {
        int alterados = repository.atualizarCursosSeInalterado(
                esperado.id(), esperado.media(), esperado.cursosLiberados(), cursosLiberados);
        if (alterados != 1) {
            throw new ConcurrentModificationException("Aluno alterado ou removido: " + esperado.id());
        }
        return new AlunoSnapshot(esperado.id(), esperado.media(), cursosLiberados);
    }

    private AlunoSnapshot mapear(AlunoEntity entity) {
        return new AlunoSnapshot(entity.getId(), entity.getMedia(), entity.getCursosLiberados());
    }
}
