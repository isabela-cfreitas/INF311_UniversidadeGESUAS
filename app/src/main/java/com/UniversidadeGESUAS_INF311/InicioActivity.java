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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

import java.util.Arrays;
import java.util.List;

public class InicioActivity extends AppCompatActivity {
    private final boolean mockCademi = true;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private androidx.drawerlayout.widget.DrawerLayout menu;
    private ImageView fotoPerfil;


    private static final long DIAS_PARA_FICAR_BRAVA = 3;
    private static final long DIAS_SEM_INTERAGIR_CURSO = 5;
    private static final long PONTOS_PARA_FICAR_FELIZ = 500;

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

        configurarCursos();
        configurarMateriais();
        resgatarNomeUsuario();
        calcularEAtualizarEstadoBeea();
        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String avatarLocal = prefs.getString("avatar_local", null);
        if (avatarLocal != null && !avatarLocal.isEmpty()) {
            int resIdLocal = getResources().getIdentifier(avatarLocal, "drawable", getPackageName());
            if (resIdLocal != 0) {
                com.bumptech.glide.Glide.with(this).load(resIdLocal).into(fotoPerfil);
            }
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
                        String avatarNome = res.getString("avatar_nome");
                        if (avatarNome != null && !avatarNome.isEmpty()) {
                            int resId = getResources().getIdentifier(avatarNome, "drawable", getPackageName());
                            if (resId != 0) {
                                com.bumptech.glide.Glide.with(InicioActivity.this)
                                        .load(resId)
                                        .placeholder(R.drawable.perfil_beea)
                                        .error(R.drawable.perfil_beea)
                                        .into(fotoPerfil);
                                android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                prefs.edit().putString("avatar_local", avatarNome).apply();
                            }
                        }
                        String cargo = res.getString("cargo");
                        if ("administrador".equalsIgnoreCase(cargo)) {
                            findViewById(R.id.btn_menu_cadastrar_admin).setVisibility(View.VISIBLE);
                            findViewById(R.id.se_tiver_outro).setVisibility(View.VISIBLE);
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

    private static class VariacaoBeea {
        String texto;
        int drawable;

        VariacaoBeea(String texto, int drawable) {
            this.texto = texto;
            this.drawable = drawable;
        }
    }

    private VariacaoBeea[] variacoesPara(String estado) {
        switch (estado) {
            case "brava":
                return new VariacaoBeea[]{
                        new VariacaoBeea(getString(R.string.beea_brava_1), R.drawable.beea_brava),
                        new VariacaoBeea(getString(R.string.beea_brava_2), R.drawable.beea_brava2),
                        new VariacaoBeea(getString(R.string.beea_brava_3), R.drawable.beea_brava3),
                };
            case "triste":
                return new VariacaoBeea[]{
                        new VariacaoBeea(getString(R.string.beea_triste_1), R.drawable.beea_triste),
                        new VariacaoBeea(getString(R.string.beea_triste_2), R.drawable.beea_triste2),
                };
            case "preocupada":
                return new VariacaoBeea[]{
                        new VariacaoBeea(getString(R.string.beea_preocupada_1), R.drawable.beea_triste_rank),
                        new VariacaoBeea(getString(R.string.beea_preocupada_2), R.drawable.beea_preocupada),
                };
            case "orgulhosa":
                return new VariacaoBeea[]{
                        new VariacaoBeea(getString(R.string.beea_orgulhosa_1), R.drawable.beea_orgulhosa),
                        new VariacaoBeea(getString(R.string.beea_orgulhosa_2), R.drawable.beea_orgulhosa2),
                };
            case "feliz":
                return new VariacaoBeea[]{
                        new VariacaoBeea(getString(R.string.beea_feliz_1), R.drawable.beea_feliz),
                        new VariacaoBeea(getString(R.string.beea_feliz_2), R.drawable.beea_feliz2),
                        new VariacaoBeea(getString(R.string.beea_feliz_3), R.drawable.bea_init),
                        new VariacaoBeea(getString(R.string.beea_feliz_4), R.drawable.bea_init),
                };
            case "hexa":
                return new VariacaoBeea[]{
                        new VariacaoBeea(getString(R.string.beea_hexa_1), R.drawable.beea_hexa),
                };
            default: // "neutra"
                return new VariacaoBeea[]{
                        new VariacaoBeea(getString(R.string.beea_neutra_1), R.drawable.bea_init),
                        new VariacaoBeea(getString(R.string.beea_neutra_2), R.drawable.bea_init)
                };
        }
    }

    private void calcularEAtualizarEstadoBeea() {
        String idUsuario = mAuth.getCurrentUser().getUid();
        DocumentReference meuDoc = db.collection("Usuarios").document(idUsuario);

        meuDoc.get().addOnSuccessListener(res -> {
            if (!res.exists()) return;

            Long pontos = res.getLong("pontos");
            if (pontos == null) pontos = 0L;
            final long pontosFinal = pontos;

            Long posicaoAnterior = res.getLong("posicao_ranking");
            if (posicaoAnterior == null) posicaoAnterior = 0L;
            final long posicaoAnteriorFinal = posicaoAnterior;

            Long sequenciaAtual = res.getLong("sequencia_dias");
            if (sequenciaAtual == null) sequenciaAtual = 0L;
            final long sequenciaAtualFinal = sequenciaAtual;

            long diasSemAbrir = diasDesde(res.getTimestamp("ultimo_acesso"));
            long diasSemInteragir = diasDesde(res.getTimestamp("ultima_interacao_curso"));

            // ---- CÁLCULO DA SEQUÊNCIA (streak) ----
            long diasCalendario = diasCalendarDesde(res.getTimestamp("ultimo_acesso"));
            long novaSequencia;

            if (diasCalendario < 0) {
                // Nunca acessou antes -> primeiro dia da sequência
                novaSequencia = 1;
            } else if (diasCalendario == 0) {
                // Já acessou hoje antes (abriu o app de novo no mesmo dia) -> mantém
                novaSequencia = sequenciaAtual == 0 ? 1 : sequenciaAtual;
            } else if (diasCalendario == 1) {
                // Acessou ontem, acessa hoje -> continua a sequência
                novaSequencia = sequenciaAtual + 1;
            } else {
                // Ficou 2+ dias sem acessar -> zera e reinicia em 1 (hoje conta como o novo dia 1)
                novaSequencia = 1;
            }

            // Busca o ranking de todo mundo, ordenado por pontos
            db.collection("Usuarios")
                    .orderBy("pontos", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(todosOsUsuarios -> {
                        long novaPosicao = 1;
                        for (int i = 0; i < todosOsUsuarios.size(); i++) {
                            if (todosOsUsuarios.getDocuments().get(i).getId().equals(idUsuario)) {
                                novaPosicao = i + 1;
                                break;
                            }
                        }

                        boolean primeiroAcesso = (res.getTimestamp("ultimo_acesso") == null);

                        String estado;

                        if (primeiroAcesso) {
                            estado = "neutra";
                        } else if (diasSemAbrir < 0 || diasSemAbrir >= DIAS_PARA_FICAR_BRAVA) {
                            estado = "brava";
                        } else if (diasSemInteragir < 0 || diasSemInteragir >= DIAS_SEM_INTERAGIR_CURSO) {
                            estado = "triste";
                        } else if (novaSequencia == 5) {
                            estado = "hexa";
                        } else if (posicaoAnteriorFinal > 0 && novaPosicao > posicaoAnteriorFinal) {
                            estado = "preocupada";
                        } else if (posicaoAnteriorFinal > 0 && novaPosicao < posicaoAnteriorFinal) {
                            estado = "orgulhosa";
                        } else if (pontosFinal >= PONTOS_PARA_FICAR_FELIZ) {
                            estado = "feliz";
                        } else {
                            estado = "neutra";
                        }

                        // Escolhe UMA variação completa (texto + arte juntos) pro estado decidido
                        VariacaoBeea[] opcoes = variacoesPara(estado);
                        VariacaoBeea escolhida = opcoes[new java.util.Random().nextInt(opcoes.length)];

                        // Atualiza a tela
                        ImageView imgBeea = findViewById(R.id.beaaState);
                        TextView txtTitulo = findViewById(R.id.txtTitulo);
                        txtTitulo.setText(escolhida.texto);
                        imgBeea.setImageResource(escolhida.drawable);

                        // Grava tudo de volta no próprio documento
                        Map<String, Object> dados = new HashMap<>();
                        dados.put("posicao_ranking_anterior", posicaoAnteriorFinal);
                        dados.put("posicao_ranking", novaPosicao);
                        dados.put("beea_state", estado);
                        dados.put("beea_texto_card", escolhida.texto);
                        dados.put("sequencia_dias", novaSequencia);
                        dados.put("ultimo_acesso", FieldValue.serverTimestamp());
                        meuDoc.set(dados, SetOptions.merge());
                    });
        });
    }

    private long diasCalendarDesde(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return -1; // nunca acessou antes

        java.util.Calendar hoje = java.util.Calendar.getInstance();
        zerarHora(hoje);

        java.util.Calendar ultimoAcesso = java.util.Calendar.getInstance();
        ultimoAcesso.setTime(timestamp.toDate());
        zerarHora(ultimoAcesso);

        long diffMillis = hoje.getTimeInMillis() - ultimoAcesso.getTimeInMillis();
        return diffMillis / (1000 * 60 * 60 * 24);
    }

    private void zerarHora(java.util.Calendar cal) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
    }

    private long diasDesde(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return -1; // nunca aconteceu
        long diffMillis = System.currentTimeMillis() - timestamp.toDate().getTime();
        return diffMillis / (1000 * 60 * 60 * 24);
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