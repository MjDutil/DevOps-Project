# Comparação JaCoCo — antes/depois

Percentuais calculados diretamente dos XMLs, sem arredondamento para inteiro.

| Métrica global | Antes | Depois |
|---|---|---|
| INSTRUCTION | 83.71% (560/669) | 100.00% (669/669) |
| BRANCH | 75.00% (12/16) | 100.00% (16/16) |
| LINE | 88.12% (141/160) | 100.00% (160/160) |
| METHOD | 83.87% (52/62) | 100.00% (62/62) |
| CLASS | 100.00% (17/17) | 100.00% (17/17) |

| Classe | Instruções antes | Instruções depois | Branches antes → depois | Linhas antes → depois |
|---|---|---|---|---|
| `org.example.tdd.TddApplication` | 37.50% (3/8) | 100.00% (8/8) | — → — | 33.33% (1/3) → 100.00% (3/3) |
| `org.example.tdd.controller.AlunoController` | 100.00% (44/44) | 100.00% (44/44) | — → — | 100.00% (13/13) → 100.00% (13/13) |
| `org.example.tdd.domain.Aluno` | 100.00% (84/84) | 100.00% (84/84) | 100.00% (10/10) → 100.00% (10/10) | 100.00% (25/25) → 100.00% (25/25) |
| `org.example.tdd.dto.AlunoRequestDto` | 100.00% (6/6) | 100.00% (6/6) | — → — | 100.00% (1/1) → 100.00% (1/1) |
| `org.example.tdd.dto.AlunoResponseDto` | 100.00% (12/12) | 100.00% (12/12) | — → — | 100.00% (1/1) → 100.00% (1/1) |
| `org.example.tdd.dto.ParticipacaoForumRequestDto` | 100.00% (12/12) | 100.00% (12/12) | — → — | 100.00% (1/1) → 100.00% (1/1) |
| `org.example.tdd.dto.PlanoPremiumResponseDto` | 100.00% (18/18) | 100.00% (18/18) | — → — | 100.00% (1/1) → 100.00% (1/1) |
| `org.example.tdd.entity.AlunoEntity` | 94.44% (68/72) | 100.00% (72/72) | — → — | 90.91% (20/22) → 100.00% (22/22) |
| `org.example.tdd.gamificacao.api.ConclusaoCurso$Resultado` | 100.00% (12/12) | 100.00% (12/12) | — → — | 100.00% (1/1) → 100.00% (1/1) |
| `org.example.tdd.gamificacao.application.AlunoSnapshot` | 100.00% (12/12) | 100.00% (12/12) | — → — | 100.00% (1/1) → 100.00% (1/1) |
| `org.example.tdd.gamificacao.application.ConcluirCursoUseCase` | 100.00% (43/43) | 100.00% (43/43) | — → — | 100.00% (8/8) → 100.00% (8/8) |
| `org.example.tdd.gamificacao.infrastructure.ConclusaoCursoFacade` | 100.00% (21/21) | 100.00% (21/21) | — → — | 100.00% (5/5) → 100.00% (5/5) |
| `org.example.tdd.gamificacao.infrastructure.GamificacaoConfiguration` | 100.00% (8/8) | 100.00% (8/8) | — → — | 100.00% (2/2) → 100.00% (2/2) |
| `org.example.tdd.gamificacao.infrastructure.JpaAlunoStore` | 100.00% (55/55) | 100.00% (55/55) | 100.00% (2/2) → 100.00% (2/2) | 100.00% (10/10) → 100.00% (10/10) |
| `org.example.tdd.ia.IaClient` | 20.79% (21/101) | 100.00% (101/101) | 0.00% (0/4) → 100.00% (4/4) | 40.91% (9/22) → 100.00% (22/22) |
| `org.example.tdd.ia.IaController` | 37.50% (6/16) | 100.00% (16/16) | — → — | 33.33% (1/3) → 100.00% (3/3) |
| `org.example.tdd.service.AlunoService` | 93.10% (135/145) | 100.00% (145/145) | — → — | 100.00% (41/41) → 100.00% (41/41) |

## Trechos inicialmente descobertos

- `org/example/tdd/service/AlunoService.java:43`: 5 instruções e 0 branches descobertos.
- `org/example/tdd/service/AlunoService.java:70`: 5 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:29`: 10 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:30`: 1 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:31`: 5 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:32`: 8 instruções e 2 branches descobertos.
- `org/example/tdd/ia/IaClient.java:33`: 1 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:34`: 4 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:41`: 5 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:42`: 16 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:43`: 5 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:44`: 10 instruções e 2 branches descobertos.
- `org/example/tdd/ia/IaClient.java:45`: 1 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:46`: 6 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaClient.java:51`: 8 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaController.java:20`: 5 instruções e 0 branches descobertos.
- `org/example/tdd/ia/IaController.java:25`: 5 instruções e 0 branches descobertos.
- `org/example/tdd/entity/AlunoEntity.java:41`: 3 instruções e 0 branches descobertos.
- `org/example/tdd/entity/AlunoEntity.java:42`: 1 instruções e 0 branches descobertos.
- `org/example/tdd/TddApplication.java:10`: 4 instruções e 0 branches descobertos.
- `org/example/tdd/TddApplication.java:11`: 1 instruções e 0 branches descobertos.

## Trechos ainda descobertos

Nenhuma instrução, linha ou branch descoberto no relatório final.

“—” indica ausência de branches/código executável, não um percentual presumido.
Cobertura não mede a completude funcional, a validade de ML/RAG, rede real, carga ou PostgreSQL.
