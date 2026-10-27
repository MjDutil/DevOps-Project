import java.util.ConcurrentModificationException;
import org.example.tdd.domain.Aluno;
import org.example.tdd.gamificacao.FakeAlunoStore;
import org.example.tdd.gamificacao.application.AlunoSnapshot;
import org.example.tdd.gamificacao.application.ConcluirCursoUseCase;

/** Executável com somente JDK: mesma regra e mesmo fake utilizados nos testes. */
public class LabDomainSmoke {
    public static void main(String[] args) {
        Aluno dominio = new Aluno("Aluno");
        dominio.concluirCurso(8.0);
        if (dominio.getCursosLiberados() != 3) throw new AssertionError("Regra de domínio");
        FakeAlunoStore fake = new FakeAlunoStore();
        ConcluirCursoUseCase casoDeUso = new ConcluirCursoUseCase(fake);
        for (double media : new double[]{8.0, 7.0, 6.5}) {
            fake.inserir(new AlunoSnapshot(1L, media, 2));
            int esperado = media > 7 ? 5 : 2;
            if (casoDeUso.executar(1L).cursosLiberados() != esperado) {
                throw new AssertionError("Média " + media);
            }
        }
        AlunoSnapshot anterior = fake.buscar(1L).orElseThrow();
        fake.atualizarCursos(anterior, 3);
        try {
            fake.atualizarCursos(anterior, 99);
            throw new AssertionError("Snapshot obsoleto deveria ser rejeitado");
        } catch (ConcurrentModificationException esperado) {
            if (fake.buscar(1L).orElseThrow().cursosLiberados() != 3) {
                throw new AssertionError("Atualização anterior perdida");
            }
        }
        System.out.println("PASSOU: domínio, 3 cenários com fake e conflito; somente JDK, sem Spring/JPA/banco.");
    }
}
