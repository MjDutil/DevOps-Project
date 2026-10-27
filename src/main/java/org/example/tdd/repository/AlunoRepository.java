package org.example.tdd.repository;

import org.example.tdd.entity.AlunoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<AlunoEntity, Long> {

    Optional<AlunoEntity> findById(Long id);

    /** Compare-and-update: não altera plano, voucher, moedas ou média. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AlunoEntity a set a.cursosLiberados = :novosCursos "
            + "where a.id = :id and a.media = :media and a.cursosLiberados = :cursosAtuais")
    int atualizarCursosSeInalterado(@Param("id") Long id,
                                  @Param("media") Double media,
                                  @Param("cursosAtuais") Integer cursosAtuais,
                                  @Param("novosCursos") int novosCursos);
}
