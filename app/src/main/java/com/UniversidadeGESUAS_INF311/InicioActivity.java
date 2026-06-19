package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class InicioActivity extends AppCompatActivity {
    private final boolean mockCademi = true;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private androidx.drawerlayout.widget.DrawerLayout menu;

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

        configurarCursos();
        configurarMateriais();
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
        // Pega o ID único do usuário logado no Firebase Auth
        String idUsuario = mAuth.getCurrentUser().getUid();

        // Busca o documento do usuário na coleção "Usuarios" do Firestore
        db.collection("Usuarios").document(idUsuario).get()
                .addOnSuccessListener(res -> {
                    TextView txtOla = findViewById(R.id.ola);
                    if (res.exists()) {
                        // Lê o campo "nome_usuario" do documento encontrado
                        String nome = res.getString("nome_usuario");
                        if (nome != null && !nome.isEmpty()) {
                            // Nome encontrado: exibe personalizado
                            txtOla.setText("Olá, " + nome + "!");
                        } else {
                            // Documento existe mas o campo nome_usuario está vazio
                            txtOla.setText("Olá!");
                        }
                    } else {
                        // Documento do usuário não existe no Firestore
                        txtOla.setText("Olá!");
                    }
                })
                .addOnFailureListener(e -> {
                    // Falha na conexão ou erro ao buscar: exibe fallback
                    TextView txtOla = findViewById(R.id.ola);
                    txtOla.setText("Olá!");
                });
    }

    // Função para abrir o site do GESUAS para visualizar os conteúdos
    public void abrirSiteGesuas(View v) {
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
        menu.closeDrawer(androidx.core.view.GravityCompat.END);
        startActivity(new Intent(this, PerfilActivity.class));
    }

    public void VerMetas(View v) {
        menu.closeDrawer(androidx.core.view.GravityCompat.END);
        startActivity(new Intent(this, MetasActivity.class));
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