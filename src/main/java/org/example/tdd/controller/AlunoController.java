package org.example.tdd.controller;

import org.example.tdd.dto.AlunoRequestDto;
import org.example.tdd.dto.AlunoResponseDto;
import org.example.tdd.service.AlunoService;
import org.example.tdd.gamificacao.api.ConclusaoCurso;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.tdd.dto.ParticipacaoForumRequestDto;
import org.example.tdd.dto.PlanoPremiumResponseDto;

@RestController
@RequestMapping("/alunos")
@CrossOrigin(origins = "*")
public class AlunoController {

    private final AlunoService alunoService;
    private final ConclusaoCurso conclusaoCurso;

    public AlunoController(AlunoService alunoService, ConclusaoCurso conclusaoCurso) {
        this.alunoService = alunoService;
        this.conclusaoCurso = conclusaoCurso;
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
        var aluno = conclusaoCurso.concluir(id);
        return ResponseEntity.ok(new AlunoResponseDto(
                aluno.id(), aluno.media(), aluno.cursosLiberados()));
    }

    @PostMapping("/{id}/recompensar-forum")
    public ResponseEntity<AlunoResponseDto> recompensarParticipacaoForum(
            @PathVariable Long id,
            @RequestBody ParticipacaoForumRequestDto request
    ) {
        return ResponseEntity.ok(
                alunoService.recompensarParticipacaoForum(id, request)
        );
    }

    @PostMapping("/{id}/verificar-premium")
    public ResponseEntity<PlanoPremiumResponseDto> verificarPlanoPremium(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                alunoService.verificarPlanoPremium(id)
        );
    }
}
