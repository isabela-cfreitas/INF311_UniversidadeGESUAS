package com.UniversidadeGESUAS_INF311;

public class Curso {
    private String titulo;
    private String data;
    private String hora;

    private String urlCurso;

    public Curso(String titulo, String data, String hora, String urlCurso) {
        this.titulo = titulo;
        this.data   = data;
        this.hora   = hora;
        this.urlCurso = urlCurso;
    }

    public String getTitulo() { return titulo; }
    public String getData()   { return data;   }
    public String getHora()   { return hora;   }
    public String getUrlCurso() { return urlCurso; }
}