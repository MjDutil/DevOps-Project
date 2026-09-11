package org.example.tdd.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "alunos")
public class AlunoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double media;

    private Integer cursosLiberados;

    public AlunoEntity() {
    }

    public AlunoEntity(Double media, Integer cursosLiberados) {
        this.media = media;
        this.cursosLiberados = cursosLiberados;
    }

    public Long getId() {
        return id;
    }

    public Double getMedia() {
        return media;
    }

    public Integer getCursosLiberados() {
        return cursosLiberados;
    }

    public void setMedia(Double media) {
        this.media = media;
    }

    public void setCursosLiberados(Integer cursosLiberados) {
        this.cursosLiberados = cursosLiberados;
    }
}