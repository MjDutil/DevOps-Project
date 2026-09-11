package org.example.tdd.domain;

public class Aluno {

    private static final double MEDIA_MINIMA = 7.0;
    private static final int CURSOS_LIBERADOS_POR_DESEMPENHO = 3;
    private static final int CURSOS_LIBERADOS_POR_FORUM = 1;

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
        if (participacaoForumElegivel(
                topicosCriados,
                maiorQuantidadeTopicosDosOutros,
                comentariosDeAjuda)) {

            cursosLiberados += CURSOS_LIBERADOS_POR_FORUM;
        }
    }

    private boolean participacaoForumElegivel(
            int topicosCriados,
            int maiorQuantidadeTopicosDosOutros,
            int comentariosDeAjuda
    ) {
        return topicosCriados > maiorQuantidadeTopicosDosOutros
                && comentariosDeAjuda > 0;
    }

    private String plano = "Basico";
    private boolean voucherProjetosReais = false;
    private int moedas = 0;

    public void adicionarCursos(int quantidade) {
        cursosLiberados += quantidade;
    }

    public void verificarPlanoPremium() {
        if (cursosLiberados >= 12) {
            plano = "Premium";
            voucherProjetosReais = true;
            moedas = 3;
        }
    }

    public String getPlano() {
        return plano;
    }

    public boolean isVoucherProjetosReais() {
        return voucherProjetosReais;
    }

    public int getMoedas() {
        return moedas;
    }
}