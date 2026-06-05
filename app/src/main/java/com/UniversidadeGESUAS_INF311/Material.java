package com.UniversidadeGESUAS_INF311;

public class Material {
    private String nome;
    private String cor; // hexadecimal

    public Material(String nome, String cor) {
        this.nome = nome;
        this.cor  = cor;
    }

    public String getNome() { return nome; }
    public String getCor()  { return cor;  }
}