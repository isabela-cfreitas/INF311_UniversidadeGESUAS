package com.UniversidadeGESUAS_INF311;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface CademiApiService {
    //rota de mock
    @GET("aluno/perfil")
    Call<AlunoCademi> obterDadosAluno(@Header("Authorization") String token);
}