package org.example.tdd.controller;

import org.example.tdd.dto.AlunoRequestDto;
import org.example.tdd.dto.AlunoResponseDto;
import org.example.tdd.service.AlunoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    public ResponseEntity<AlunoResponseDto> criar(
            @RequestBody AlunoRequestDto request
    ) {
        return ResponseEntity.ok(
                alunoService.criar(request)
        );
    }

    @PostMapping("/{id}/concluir-curso")
    public ResponseEntity<AlunoResponseDto> concluirCurso(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                alunoService.concluirCurso(id)
        );
    }
}