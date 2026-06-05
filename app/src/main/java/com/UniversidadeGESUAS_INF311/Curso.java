package com.UniversidadeGESUAS_INF311;

public class Curso {
    private String titulo;
    private String data;
    private String hora;

    public Curso(String titulo, String data, String hora) {
        this.titulo = titulo;
        this.data   = data;
        this.hora   = hora;
    }

    public String getTitulo() { return titulo; }
    public String getData()   { return data;   }
    public String getHora()   { return hora;   }
}