package com.UniversidadeGESUAS_INF311;

public class AlunoCademi {
    //@SerializedName("total_points")
    //tem que fazer tipo isso dps se os nomes das variáveis deles forem diferentes dos q tao aqui
    private int pontos;
    private int cursosEmAndamento;

    public AlunoCademi(int pontos, int cursosEmAndamento) {
        this.pontos = pontos;
        this.cursosEmAndamento = cursosEmAndamento;
    }

    public int getPontos() { return pontos; }
    public int getCursosEmAndamento() { return cursosEmAndamento; }
}