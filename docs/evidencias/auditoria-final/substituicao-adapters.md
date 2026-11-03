# Substituição dos adapters — evidência da auditoria final

Os mesmos métodos herdados de `AlunoStoreContract` são executados pelos dois adapters.
O caso de uso e `domain.Aluno` são os mesmos. Somente a composição/fixture de persistência muda.

| Cenário comum (nome real no Surefire) | Fake | JPA/H2 |
|---|---|---|
| `atualizarNaoDeveCriarAlunoAusente` | PASSOU | PASSOU |
| `deveAcumularQuatroConclusoes` | PASSOU | PASSOU |
| `deveAtualizarSomenteSaldoPreservandoIdEMedia` | PASSOU | PASSOU |
| `deveBuscarSnapshotExistente` | PASSOU | PASSOU |
| `deveFalharNoCasoDeUsoParaAlunoAusente` | PASSOU | PASSOU |
| `deveRejeitarMediaObsoletaSemAlterarSaldo` | PASSOU | PASSOU |
| `deveRejeitarSaldoObsoletoSemPerderAtualizacaoAnterior` | PASSOU | PASSOU |
| `deveRetornarVazioParaAlunoAusente` | PASSOU | PASSOU |
| `mesmaRegraDeveFuncionarComQualquerAdapter(double, int)[1]` | PASSOU | PASSOU |
| `mesmaRegraDeveFuncionarComQualquerAdapter(double, int)[2]` | PASSOU | PASSOU |
| `mesmaRegraDeveFuncionarComQualquerAdapter(double, int)[3]` | PASSOU | PASSOU |

Os três cenários adicionais do adapter JPA também passaram:
- `adapterDeEscritaDeveExigirTransacao`.
- `fachadaDeveComitarComUmaLeituraEUmaAtualizacao`.
- `rollbackDeveDesfazerConclusao`.

Fonte: relatórios XML de `target/surefire-reports` da execução limpa cujo log acompanha esta pasta.
Os parâmetros do método de média correspondem a `8.0 → 5`, `7.0 → 2`, `6.5 → 2`, com saldo inicial 2.
Os números por adapter não são novos testes além dos 75; são um recorte dessa mesma execução.
