import java.util.*;
import org.example.tdd.gamificacao.application.*;

/** Smoke test executável apenas com javac/java; complementa, não substitui, mvnw test. */
public class LabDomainSmoke {
    public static void main(String[] args) {
        Map<Long,AlunoSnapshot> dados = new HashMap<>();
        AlunoStore fake = new AlunoStore() {
            public Optional<AlunoSnapshot> buscar(Long id) { return Optional.ofNullable(dados.get(id)); }
            public AlunoSnapshot salvar(AlunoSnapshot a) { dados.put(a.id(),a); return a; }
        };
        dados.put(1L, new AlunoSnapshot(1L,8.0,2));
        int atual = new ConcluirCursoUseCase(fake).executar(1L).cursosLiberados();
        if (atual != 5) throw new AssertionError("Esperado 5, recebido " + atual);
        dados.put(2L, new AlunoSnapshot(2L,7.0,2));
        int limite = new ConcluirCursoUseCase(fake).executar(2L).cursosLiberados();
        if (limite != 2) throw new AssertionError("Esperado 2, recebido " + limite);
        System.out.println("PASSOU: caso de uso e fake adapter, sem Spring/JPA (2 cenários).");
    }
}
