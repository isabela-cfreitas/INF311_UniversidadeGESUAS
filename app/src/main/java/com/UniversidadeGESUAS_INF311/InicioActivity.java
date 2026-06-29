package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class InicioActivity extends AppCompatActivity {
    private final boolean mockCademi = true;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private androidx.drawerlayout.widget.DrawerLayout menu;
    private ImageView fotoPerfil;

    private View loadingOverlay;
    private int carregamentosPendentes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        menu = findViewById(R.id.drawer_layout);
        fotoPerfil = findViewById(R.id.foto_perfil);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        configurarCursos();
        configurarMateriais();

        String nomeOlaExtra = getIntent().getStringExtra("nomeOla");
        if (nomeOlaExtra != null) {
            // Se os dados já vieram prontos da SplashActivity ele aplica direto, sem nenhuma espera na tela
            aplicarDadosUsuario(
                    nomeOlaExtra,
                    getIntent().getStringExtra("avatarNome"),
                    getIntent().getBooleanExtra("isAdmin", false)
            );
            aplicarEstadoBeea(
                    getIntent().getStringExtra("beeaTexto"),
                    getIntent().getIntExtra("beeaDrawableRes", R.drawable.bea_init)
            );
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
            }
        } else {
            // Se a tela foi aberta sem passar pela Splash ele carrega aqui mesmo
            carregamentosPendentes = 2;
            resgatarNomeUsuario();
            calcularEAtualizarEstadoBeea();
        }

        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String avatarLocal = prefs.getString("avatar_local", null);
        if (avatarLocal != null && !avatarLocal.isEmpty()) {
            int resIdLocal = getResources().getIdentifier(avatarLocal, "drawable", getPackageName());
            if (resIdLocal != 0) {
                com.bumptech.glide.Glide.with(this).load(resIdLocal).into(fotoPerfil);
            }
        }
    }

    // Aplica nome/avatar/cargo na tela, vindos ou da Splash ou de um carregamento próprio
    private void aplicarDadosUsuario(String nomeOla, String avatarNome, boolean isAdmin) {
        TextView txtOla = findViewById(R.id.ola);
        txtOla.setText(nomeOla != null ? nomeOla : "Olá!");

        if (avatarNome != null && !avatarNome.isEmpty()) {
            int resId = getResources().getIdentifier(avatarNome, "drawable", getPackageName());
            if (resId != 0) {
                com.bumptech.glide.Glide.with(this)
                        .load(resId)
                        .placeholder(R.drawable.perfil_beea)
                        .error(R.drawable.perfil_beea)
                        .into(fotoPerfil);
                getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putString("avatar_local", avatarNome).apply();
            }
        }

        if (isAdmin) {
            findViewById(R.id.btn_menu_cadastrar_admin).setVisibility(View.VISIBLE);
            findViewById(R.id.se_tiver_outro).setVisibility(View.VISIBLE);
        }
    }

    // Aplica texto/imagem da Beea na tela, vindos ou da Splash ou de um carregamento próprio
    private void aplicarEstadoBeea(String texto, int drawableRes) {
        if (texto == null) return;
        ImageView imgBeea = findViewById(R.id.beaaState);
        TextView txtTitulo = findViewById(R.id.txtTitulo);
        txtTitulo.setText(texto);
        imgBeea.setImageResource(drawableRes);
    }

    // Chamado no final de QUALQUER caminho do carregamento sem Splash
    private void marcarCarregamentoConcluido() {
        carregamentosPendentes--;
        if (carregamentosPendentes <= 0 && loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String avatarLocal = prefs.getString("avatar_local", null);
        if (avatarLocal != null && !avatarLocal.isEmpty()) {
            int resIdLocal = getResources().getIdentifier(avatarLocal, "drawable", getPackageName());
            if (resIdLocal != 0) {
                com.bumptech.glide.Glide.with(this).load(resIdLocal).into(fotoPerfil);
            }
        }
        resgatarNomeUsuario();
    }

    // CURSOS EM ANDAMENTO — dados mock
    private void configurarCursos() {
        /*List<Curso> cursos = Arrays.asList(
                new Curso("Ética e sigilo no cotidiano de gestores e trabalhadores do SUAS", "09/04", "09h00"),
                new Curso("Proteção Social Básica no SUAS",                                  "15/04", "14h00"),
                new Curso("Vigilância Socioassistencial",                                    "22/04", "10h00"),
                new Curso("Gestão do Trabalho no SUAS",                                      "29/04", "09h00")
        );

        RecyclerView recyclerCursos = findViewById(R.id.recyclerCursos);
        recyclerCursos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerCursos.setAdapter(new CursoAdapter(cursos));*/

        RecyclerView recyclerCursos = findViewById(R.id.recyclerCursos);
        recyclerCursos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        if (mockCademi) {
            String urlPadrao = "https://membros.universidadegesuas.com.br/auth/login"; //url do mock
            List<Curso> cursosMock = Arrays.asList(
                    new Curso("Ética e sigilo no cotidiano de gestores e trabalhadores do SUAS", "09/04", "09h00", urlPadrao),
                    new Curso("Proteção Social Básica no SUAS",                                  "15/04", "14h00", urlPadrao),
                    new Curso("Vigilância Socioassistencial",                                    "22/04", "10h00", urlPadrao),
                    new Curso("Gestão do Trabalho no SUAS",                                      "29/04", "09h00", urlPadrao)
            );
            recyclerCursos.setAdapter(new CursoAdapter(cursosMock));

        } else {
            //arrumar aqui quando tiver api
            /*
            apiService.obterCursosEmAndamento("TOKEN").enqueue(new retrofit2.Callback<List<Curso>>() {
                @Override
                public void onResponse(Call<List<Curso>> call, Response<List<Curso>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Curso> cursosDaAPI = response.body();
                        //se nao tiver o link de cada curso a gente pode ou jogar tudo pro login ou fazer eles serem nao clicaveis
                        //OBS: se for nao clicavel tem q tirar o camp ourl que botei na classe custos!!!!
                        recyclerCursos.setAdapter(new CursoAdapter(cursosDaAPI));
                    }
                }
                @Override
                public void onFailure(Call<List<Curso>> call, Throwable t) {
                    Log.e("API", "Erro nos cursos", t);
                }
            });
            */
        }
    }

    // MATERIAIS COMPLEMENTARES — dados mock
    private void configurarMateriais() {
        List<Material> materiais = Arrays.asList(
                new Material("MATERIAL I",   "#004e63"),
                new Material("MATERIAL II",  "#26a18e"),
                new Material("MATERIAL III", "#79e581"),
                new Material("MATERIAL IV", "#4e54a1")
        );

        RecyclerView recyclerMateriais = findViewById(R.id.recyclerMateriais);
        recyclerMateriais.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerMateriais.setAdapter(new MaterialAdapter(materiais));
    }

    // Resgatar o nome de usuário para exibir mensagem de boas vindas
    private void resgatarNomeUsuario() {
        String idUsuario = mAuth.getCurrentUser().getUid();

        BeeaRepository.buscarDadosUsuario(db, idUsuario, new BeeaRepository.DadosUsuarioCallback() {
            @Override
            public void onSucesso(String nomeOla, String avatarNome, boolean isAdmin) {
                aplicarDadosUsuario(nomeOla, avatarNome, isAdmin);
                marcarCarregamentoConcluido();
            }

            @Override
            public void onFalha() {
                TextView txtOla = findViewById(R.id.ola);
                txtOla.setText("Olá!");
                marcarCarregamentoConcluido();
            }
        });
    }

    // Calcula e atualiza o estado da Beea
    private void calcularEAtualizarEstadoBeea() {
        String idUsuario = mAuth.getCurrentUser().getUid();

        BeeaRepository.calcularEAtualizarEstadoBeea(db, idUsuario, this, new BeeaRepository.EstadoBeeaCallback() {
            @Override
            public void onSucesso(String texto, int drawableRes) {
                aplicarEstadoBeea(texto, drawableRes);
                marcarCarregamentoConcluido();
            }

            @Override
            public void onFalha() {
                marcarCarregamentoConcluido();
            }
        });
    }

    // Função para abrir o site do GESUAS para visualizar os conteúdos
    public void abrirSiteGesuas(View v) {
        String idUsuario = mAuth.getCurrentUser().getUid();
        db.collection("Usuarios").document(idUsuario)
                .update("ultima_interacao_curso", FieldValue.serverTimestamp());

        String url = "https://membros.universidadegesuas.com.br/auth/login";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // NAVEGAÇÃO
    public void navegar(View v) {
        int id = v.getId();
        if (id == R.id.ranking) {
            Intent it = new Intent(this, RankingActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(it);
        } else if (id == R.id.comunidade) {
            Intent it = new Intent(this, ComunidadeActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(it);
        }
    }

    public void abrirMenu(View v) {
        if (!menu.isDrawerOpen(androidx.core.view.GravityCompat.END)) {
            menu.openDrawer(androidx.core.view.GravityCompat.END);
        }
    }

    public void VerPerfil(View v) {
        if (menu.isDrawerOpen(androidx.core.view.GravityCompat.END)) {
            menu.closeDrawer(androidx.core.view.GravityCompat.END);
        }
        startActivity(new Intent(this, PerfilActivity.class));
    }

    public void VerMetas(View v) {
        if (menu.isDrawerOpen(androidx.core.view.GravityCompat.END)) {
            menu.closeDrawer(androidx.core.view.GravityCompat.END);
        }
        startActivity(new Intent(this, MetasActivity.class));
    }

    public void CadastroAdmin(View v) {
        if (menu.isDrawerOpen(androidx.core.view.GravityCompat.END)) {
            menu.closeDrawer(androidx.core.view.GravityCompat.END);
        }
        startActivity(new Intent(this, CadastroAdminActivity.class));
    }

    public void sairConta (View v) {
        if (menu.isDrawerOpen(androidx.core.view.GravityCompat.END)) {
            menu.closeDrawer(androidx.core.view.GravityCompat.END);
        }
        mAuth.signOut();
        Intent it = new Intent(this, MainActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //tem q limpar pra usuario nao conseguir "relogar" dps só com o botão de voltar
        startActivity(it);
        finish();
    }
}