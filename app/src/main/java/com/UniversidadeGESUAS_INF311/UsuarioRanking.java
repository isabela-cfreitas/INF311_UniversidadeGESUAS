package com.UniversidadeGESUAS_INF311;

public class UsuarioRanking {
    private String nome;
    private int pontos;
    private String uid;
    private String cargo;

    public UsuarioRanking() {}

    public UsuarioRanking(String nome, int pontos, String uid, String cargo) {
        this.nome = nome;
        this.pontos = pontos;
        this.uid = uid;
        this.cargo = cargo;
    }

    public String getNome() { return nome; }
    public int getPontos() { return pontos; }
    public String getUid() { return uid; }
    public String getCargo() { return cargo; }

    public void setNome(String nome) {
        this.nome = nome;
    }
    public void setPontos(int pontos) {
        this.pontos = pontos;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
