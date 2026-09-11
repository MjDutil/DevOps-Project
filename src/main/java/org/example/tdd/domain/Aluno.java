package org.example.tdd.domain;

public class Aluno {

    private static final double MEDIA_MINIMA = 7.0;
    private static final int CURSOS_LIBERADOS_POR_DESEMPENHO = 3;

    private final String nome;
    private int cursosLiberados;

    public Aluno(String nome) {
        this.nome = nome;
    }

    public void concluirCurso(double media) {
        if (media > MEDIA_MINIMA) {
            cursosLiberados += CURSOS_LIBERADOS_POR_DESEMPENHO;
        }
    }

    public int getCursosLiberados() {
        return cursosLiberados;
    }

    public String getNome() {
        return nome;
    }

    public void recompensarParticipacaoForum(
            int topicosCriados,
            int maiorQuantidadeTopicosDosOutros,
            int comentariosDeAjuda
    ) {
        //RED - ainda não realizada
    }
}