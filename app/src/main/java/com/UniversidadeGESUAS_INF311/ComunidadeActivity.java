package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComunidadeActivity extends AppCompatActivity {

    private LinearLayout containerPosts;
    private FirebaseFirestore db;

    private EditText inputBusca;
    private ImageView fotoPerfilTopo;
    private List<QueryDocumentSnapshot> listaPosts = new ArrayList<>();

    private String abaAtual = "geral";

    private androidx.drawerlayout.widget.DrawerLayout menu;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comunidade);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db= FirebaseFirestore.getInstance();
        containerPosts = findViewById(R.id.posts);
        fotoPerfilTopo = findViewById(R.id.foto_perfil);
        mAuth = FirebaseAuth.getInstance();
        menu = findViewById(R.id.drawer_layout);

        inputBusca = findViewById(R.id.busca);
        inputBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPosts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        String idUsuario = mAuth.getCurrentUser().getUid();

        db.collection("Usuarios").document(idUsuario).get().addOnSuccessListener(res -> {
            if (res.exists()) {
                String cargo = res.getString("cargo");
                if ("administrador".equalsIgnoreCase(cargo)) {
                    findViewById(R.id.btn_menu_cadastrar_admin).setVisibility(View.VISIBLE);
                    findViewById(R.id.se_tiver_outro).setVisibility(View.VISIBLE);
                }
            }
            String avatarLogado = res.getString("avatar_nome");
            if (avatarLogado != null && !avatarLogado.isEmpty()) {
                int resId = getResources().getIdentifier(avatarLogado, "drawable", getPackageName());
                if (resId != 0) {
                    com.bumptech.glide.Glide.with(ComunidadeActivity.this)
                            .load(resId)
                            .placeholder(R.drawable.perfil_beea)
                            .error(R.drawable.perfil_beea)
                            .into(fotoPerfilTopo);
                }
                else fotoPerfilTopo.setImageResource(R.drawable.perfil_beea);
            } else {
                fotoPerfilTopo.setImageResource(R.drawable.perfil_beea);
            }
        });

        carregarPost();

        android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String avatarLocal = prefs.getString("avatar_local", null);
        if (avatarLocal != null && !avatarLocal.isEmpty()) {
            int resIdLocal = getResources().getIdentifier(avatarLocal, "drawable", getPackageName());
            if (resIdLocal != 0) {
                com.bumptech.glide.Glide.with(this).load(resIdLocal).into(fotoPerfilTopo);
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
                com.bumptech.glide.Glide.with(this).load(resIdLocal).into(fotoPerfilTopo);
            }
        }
        String idUsuario = mAuth.getCurrentUser().getUid();
        db.collection("Usuarios").document(idUsuario).get().addOnSuccessListener(res -> {
            if (res.exists()) {
                String avatarLogado = res.getString("avatar_nome");
                if (avatarLogado != null && !avatarLogado.isEmpty()) {
                    int resId = getResources().getIdentifier(avatarLogado, "drawable", getPackageName());
                    if (resId != 0) {
                        com.bumptech.glide.Glide.with(ComunidadeActivity.this)
                                .load(resId)
                                .placeholder(R.drawable.perfil_beea)
                                .error(R.drawable.perfil_beea)
                                .into(fotoPerfilTopo);
                    }
                    prefs.edit().putString("avatar_local", avatarLogado).apply();
                }
            }
        });
    }

    public void carregarPost() {
        db.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING).limit(20).addSnapshotListener((value,error)->{
            if (error != null) {
                Toast.makeText(this, "Erro ao carregar o feed", Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null) {
                listaPosts.clear();//vai atualizar lista de posts
                for (QueryDocumentSnapshot doc : value) {
                    listaPosts.add(doc);
                }
                filtrarPosts(inputBusca.getText().toString());
            }
        });
    }

    public void novo_post (View v) {
        Intent it = new Intent(getBaseContext(), NovoPostActivity.class);
        startActivity(it);
    }

    public void curtir (View v) {
        String idPost = (String) v.getTag();
        if (idPost != null) {
            db.collection("Posts").document(idPost).update("numeroCurtidas", FieldValue.increment(1)).addOnFailureListener(e -> {
                Toast.makeText(this, "Erro ao curtir o post", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void comentar (View v) {
        String idPost = (String) v.getTag();
        if (idPost != null) {
            Intent it = new Intent(this, ComentariosActivity.class);
            it.putExtra("idPost",idPost);
            startActivity(it);
        }
    }

    public void navegar(View v) {
        int id = v.getId();
        if (id == R.id.home) {
            Intent it = new Intent(this, InicioActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Igual ao slide pág. 36
            startActivity(it);
        } else if (id == R.id.ranking) {
            Intent it = new Intent(this, RankingActivity.class);
            it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(it);
        } else if (id == R.id.comunidade) {

        }

        else if (id == R.id.geral) {
            abaAtual = "geral";
            atualizarVisualAbas();
            filtrarPosts(inputBusca.getText().toString());
        } else if (id == R.id.profissionais) {
            abaAtual = "profissionais";
            atualizarVisualAbas();
            filtrarPosts(inputBusca.getText().toString());
        }
    }

    private void filtrarPosts(String textoBusca) {
        containerPosts.removeAllViews();
        String query = textoBusca.toLowerCase().trim();

        for (QueryDocumentSnapshot publi : listaPosts) {
            String nome = publi.getString("nomeUsuario"); //preciso jogar pra string pq a funçao contains recebe string
            String cargo = publi.getString("cargo");
            String conteudo = publi.getString("conteudo");

            if (abaAtual.equals("profissionais")) {
                if (cargo == null || !cargo.equalsIgnoreCase("administrador")) {
                    continue; // Pula este post e vai para o próximo
                }
            }

            if (!query.isEmpty()) {
                boolean contemConteudo = conteudo != null && conteudo.toLowerCase().contains(query);
                boolean contemNome = nome != null && nome.toLowerCase().contains(query);

                if (!contemConteudo && !contemNome) {
                    continue;
                }
            }

            Long curtidas = publi.getLong("numeroCurtidas");
            Long comentarios = publi.getLong("numeroComentarios");
            Timestamp time = publi.getTimestamp("timestamp");

            //esse método de getLayoutInflater deveria pegar o xml que eu passei e fingir que ele é um view
            // normal, se estiver dando crash depois isso tem potencial de ser o causador, pq nao entendi mt bem como usa :)
            View postView = getLayoutInflater().inflate(R.layout.item_post_comunidade, containerPosts, false);

            TextView Nome = postView.findViewById(R.id.nome_usuario);
            TextView Cargo = postView.findViewById(R.id.cargo_usuario);
            TextView Conteudo = postView.findViewById(R.id.conteudo_post);
            TextView Curtidas = postView.findViewById(R.id.contador_curtidas);
            TextView Comentarios = postView.findViewById(R.id.contador_comentarios);
            TextView Data = postView.findViewById(R.id.data_post);

            LinearLayout botaoCurtir = postView.findViewById(R.id.layout_curtir);
            botaoCurtir.setTag(publi.getId());
            LinearLayout botaoComentar = postView.findViewById(R.id.layout_comentar);
            botaoComentar.setTag(publi.getId());

            Nome.setText(nome);
            Cargo.setText(cargo);
            Conteudo.setText(conteudo);
            Curtidas.setText(String.valueOf(curtidas != null ? curtidas : 0));
            Comentarios.setText(String.valueOf(comentarios != null ? comentarios : 0));

            ImageView fotoAutorPost = postView.findViewById(R.id.perfil);
            fotoAutorPost.setImageResource(R.drawable.perfil_beea);

            if (nome != null && !nome.isEmpty()) {
                fotoAutorPost.setTag(nome);

                db.collection("Usuarios").whereEqualTo("nome_usuario", nome).get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                com.google.firebase.firestore.DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);

                                String avatarAutor = userDoc.getString("avatar_nome");
                                if (avatarAutor != null && !avatarAutor.isEmpty()) {
                                    int resId = getResources().getIdentifier(avatarAutor, "drawable", getPackageName());
                                    if (resId != 0) {
                                        if (nome.equals(fotoAutorPost.getTag())) {
                                            com.bumptech.glide.Glide.with(ComunidadeActivity.this)
                                                    .load(resId)
                                                    .placeholder(R.drawable.perfil_beea)
                                                    .error(R.drawable.perfil_beea)
                                                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade())
                                                    .into(fotoAutorPost);
                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (nome.equals(fotoAutorPost.getTag())) {
                                fotoAutorPost.setImageResource(R.drawable.perfil_beea);
                            }
                        });
            }

            if (time != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                Data.setText(sdf.format(time.toDate()));
            } else {
                Data.setText("Agora mesmo");
            }
            containerPosts.addView(postView);

        }
    }

    private void atualizarVisualAbas() {
        com.google.android.material.button.MaterialButton btnGeral = findViewById(R.id.geral);
        com.google.android.material.button.MaterialButton btnProfissionais = findViewById(R.id.profissionais);
        int corVerdeAtivo = androidx.core.content.ContextCompat.getColor(this, R.color.verde2);
        int corTextoInativo = android.graphics.Color.parseColor("#556B2F");
        int corBranco = android.graphics.Color.parseColor("#FFFFFF");
        int corTransparente = android.graphics.Color.TRANSPARENT;

        if (abaAtual.equals("geral")) {
            btnGeral.setBackgroundColor(corVerdeAtivo);
            btnGeral.setTextColor(corBranco);
            btnProfissionais.setBackgroundColor(corTransparente);
            btnProfissionais.setTextColor(corTextoInativo);
        } else {
            btnProfissionais.setBackgroundColor(corVerdeAtivo);
            btnProfissionais.setTextColor(corBranco);
            btnGeral.setBackgroundColor(corTransparente);
            btnGeral.setTextColor(corTextoInativo);
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