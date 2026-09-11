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

    private String plano = "Basico";
    private Boolean voucherProjetosReais = false;
    private Integer moedas = 0;

    protected AlunoEntity() {
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


    public String getPlano() { return plano; }

    public void setPlano(String plano) { this.plano = plano; }

    public Boolean getVoucherProjetosReais() { return voucherProjetosReais; }

    public void setVoucherProjetosReais(Boolean voucherProjetosReais) { this.voucherProjetosReais = voucherProjetosReais; }

    public Integer getMoedas() { return moedas; }

    public void setMoedas(Integer moedas) { this.moedas = moedas; }

}