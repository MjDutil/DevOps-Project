package org.example.tdd.gamificacao;

import org.example.tdd.gamificacao.application.AlunoSnapshot;
import org.example.tdd.gamificacao.application.AlunoStore;

/** Executa integralmente o contrato sem inicializar Spring e sem banco. */
class FakeAlunoStoreContractTest extends AlunoStoreContract {
    private FakeAlunoStore fake;

    @Override protected AlunoStore store() { return fake; }
    @Override protected void limpar() { fake = new FakeAlunoStore(); }
    @Override protected AlunoSnapshot inserir(double media, int cursos) {
        var aluno = new AlunoSnapshot(1L, media, cursos);
        fake.inserir(aluno);
        return aluno;
    }
}
