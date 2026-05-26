package com.UniversidadeGESUAS_INF311;

import com.google.firebase.Timestamp;

public class Post {
    private String idPost;
    private String nomeUsuario;
    private String cargo;
    private String conteudo;
    private Timestamp timestamp;//ordenar publicacoes da mais recente para mais antiga
    private int numeroCurtidas;
    private int numeroComentarios;

    public Post() {
    }

    public Post(String nomeUsuario, String cargo, String conteudo, Timestamp timestamp) {
        this.nomeUsuario = nomeUsuario;
        this.cargo = cargo;
        this.conteudo = conteudo;
        this.timestamp = timestamp;
        this.numeroCurtidas = 0;
        this.numeroComentarios = 0;
    }

    public String getIdPost() {
        return idPost;
    }
    public void setIdPost(String idPost) {
        this.idPost = idPost;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }
    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getCargo() {
        return cargo;
    }
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getConteudo() {
        return conteudo;
    }
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getNumeroCurtidas() {
        return numeroCurtidas;
    }
    public void setNumeroCurtidas(int numeroCurtidas) {
        this.numeroCurtidas = numeroCurtidas;
    }

    public int getNumeroComentarios() {
        return numeroComentarios;
    }
    public void setNumeroComentarios(int numeroComentarios) {
        this.numeroComentarios = numeroComentarios;
    }
}