package org.example.tdd.domain;

public class Aluno {

    private double media;
    private int cursosLiberados;

    public Aluno(double media) {
        this.media = media;
        this.cursosLiberados = 0;
    }

    public void concluirCurso() {
        if (media > 7.0) {
            cursosLiberados += 3;
        }
    }

    public int getCursosLiberados() {
        return cursosLiberados;
    }
}