package org.example.tdd.gamificacao.infrastructure;

import java.util.Optional;
import org.example.tdd.entity.AlunoEntity;
import org.example.tdd.gamificacao.application.AlunoSnapshot;
import org.example.tdd.gamificacao.application.AlunoStore;
import org.example.tdd.repository.AlunoRepository;
import org.springframework.stereotype.Component;

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
    public AlunoSnapshot salvar(AlunoSnapshot aluno) {
        AlunoEntity entity = repository.findById(aluno.id())
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado: " + aluno.id()));
        entity.setCursosLiberados(aluno.cursosLiberados());
        return mapear(repository.save(entity));
    }

    private AlunoSnapshot mapear(AlunoEntity entity) {
        return new AlunoSnapshot(entity.getId(), entity.getMedia(), entity.getCursosLiberados());
    }
}
