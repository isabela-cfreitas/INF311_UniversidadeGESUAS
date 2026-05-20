package com.UniversidadeGESUAS_INF311;

public class UsuarioRanking {
    private String nome;
    private int pontos;
    private String uid;

    public UsuarioRanking() {}

    public UsuarioRanking(String nome, int pontos, String uid) {
        this.nome = nome;
        this.pontos = pontos;
        this.uid = uid;
    }

    public String getNome() { return nome; }
    public int getPontos() { return pontos; }
    public String getUid() { return uid; }
}
