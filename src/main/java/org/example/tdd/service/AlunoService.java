package org.example.tdd.service;

import org.example.tdd.dto.AlunoRequestDto;
import org.example.tdd.dto.AlunoResponseDto;
import org.example.tdd.entity.AlunoEntity;
import org.example.tdd.repository.AlunoRepository;
import org.springframework.stereotype.Service;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public AlunoResponseDto concluirCurso(Long id) {

        AlunoEntity aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        if (aluno.getMedia() > 7.0) {
            aluno.setCursosLiberados(
                    aluno.getCursosLiberados() + 3
            );
        }

        AlunoEntity salvo = alunoRepository.save(aluno);

        return new AlunoResponseDto(
                salvo.getId(),
                salvo.getMedia(),
                salvo.getCursosLiberados()
        );
    }

    public AlunoResponseDto criar(AlunoRequestDto request) {

        AlunoEntity aluno = new AlunoEntity(
                request.media(),
                0
        );

        AlunoEntity salvo = alunoRepository.save(aluno);

        return new AlunoResponseDto(
                salvo.getId(),
                salvo.getMedia(),
                salvo.getCursosLiberados()
        );
    }
}