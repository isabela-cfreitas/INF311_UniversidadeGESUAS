package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final long TEMPO_MINIMO_MS = 2000;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean dadosCarregados = false;
    private boolean tempoMinimoPassou = false;

    // Resultado do carregamento — vai pronto pra InicioActivity, sem ela precisar buscar de novo
    private String nomeOla;
    private String avatarNome;
    private boolean isAdmin;
    private String beeaTexto;
    private int beeaDrawableRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            // Sem usuário logado: não tem nada pra carregar
            irPara(new Intent(this, MainActivity.class));
            return;
        }

        // A partir daqui SÓ executa se tiver usuário logado (tem dados de verdade pra carregar)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tempoMinimoPassou = true;
            tentarNavegar();
        }, TEMPO_MINIMO_MS);

        carregarDadosCompletos();
    }

    private void carregarDadosCompletos() {
        String idUsuario = mAuth.getCurrentUser().getUid();

        // Contador simples pra saber quando os 2 carregamentos terminaram
        final int[] pendentes = {2};

        BeeaRepository.buscarDadosUsuario(db, idUsuario, new BeeaRepository.DadosUsuarioCallback() {
            @Override
            public void onSucesso(String nomeOlaResultado, String avatarNomeResultado, boolean isAdminResultado) {
                nomeOla = nomeOlaResultado;
                avatarNome = avatarNomeResultado;
                isAdmin = isAdminResultado;
                marcarConcluido(pendentes);
            }

            @Override
            public void onFalha() {
                nomeOla = "Olá!";
                marcarConcluido(pendentes);
            }
        });

        BeeaRepository.calcularEAtualizarEstadoBeea(db, idUsuario, this, new BeeaRepository.EstadoBeeaCallback() {
            @Override
            public void onSucesso(String texto, int drawableRes) {
                beeaTexto = texto;
                beeaDrawableRes = drawableRes;
                marcarConcluido(pendentes);
            }

            @Override
            public void onFalha() {
                marcarConcluido(pendentes);
            }
        });
    }

    private void marcarConcluido(int[] pendentes) {
        pendentes[0]--;
        if (pendentes[0] <= 0) {
            dadosCarregados = true;
            tentarNavegar();
        }
    }

    private void tentarNavegar() {
        if (!dadosCarregados || !tempoMinimoPassou) return;

        Intent it = new Intent(this, InicioActivity.class);
        // Entrega os dados já prontos: a InicioActivity não precisa buscar nada de novo na primeira exibição
        it.putExtra("nomeOla", nomeOla);
        it.putExtra("avatarNome", avatarNome);
        it.putExtra("isAdmin", isAdmin);
        it.putExtra("beeaTexto", beeaTexto);
        it.putExtra("beeaDrawableRes", beeaDrawableRes);
        irPara(it);
    }

    private void irPara(Intent it) {
        startActivity(it);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}