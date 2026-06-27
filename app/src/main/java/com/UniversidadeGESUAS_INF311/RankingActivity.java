package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class RankingActivity extends AppCompatActivity {
    private final boolean mockCademi = true;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout leaderboard;

    private androidx.drawerlayout.widget.DrawerLayout menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ranking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        leaderboard = findViewById(R.id.leaderboard);
        menu = findViewById(R.id.drawer_layout);

        getFromDB();
    }

    final int top_size = 10;
    private UsuarioRanking user;
    private UsuarioRanking[] top;

    private void getFromDB() {
        db.collection("Usuarios")
                .orderBy("pontos", Query.Direction.DESCENDING)
                .limit(top_size)
                .get()
                .addOnCompleteListener(res-> {
                    if (res.isSuccessful()) {
                        top = new UsuarioRanking[res.getResult().size()];
                        int i = 0;
                        for (QueryDocumentSnapshot document : res.getResult()) {
                            String nome = document.getString("nome");
                            int pontos = document.getLong("pontos").intValue();
                            String uid = document.getId();
                            String cargo = document.getString("cargo");

                            top[i] = new UsuarioRanking(nome, pontos, uid, cargo);
                            i++;
                        }
                        desenharRanking();
                    } else {
                        Log.w("Ranking", "Erro ao buscar o ranking.", res.getException());
                    }
        });
        user = new UsuarioRanking();
        user.setUid(mAuth.getCurrentUser().getUid());
        db.collection("Usuarios")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {

                            //user.setPontos(document.getLong("pontos").intValue());
                            //user.setNome(document.getString("nome"));
                            //user.setCargo(document.getString("cargo"));
                            //desenharRanking();
                            if (mockCademi) {
                                //pro mock to usando os valores do bd do firebase
                                user.setPontos(document.getLong("pontos").intValue());
                                user.setNome(document.getString("nome"));
                                user.setCargo(document.getString("cargo"));

                                String cargo = document.getString("cargo");
                                if ("administrador".equalsIgnoreCase(cargo)) {
                                    findViewById(R.id.btn_menu_cadastrar_admin).setVisibility(View.VISIBLE);
                                    findViewById(R.id.se_tiver_outro).setVisibility(View.VISIBLE);
                                }

                                desenharRanking();

                            } else {
                                //quando eles passarem os dados da api a a gente vai ativar isso aqui, pra
                                //poder atualizar o firestore com os dados do cademi
                                //int pontosDaAPI =(puxar do do retrofit)
                                //user.setPontos(pontosDaAPI);
                                //db.collection("Usuarios").document(user.getUid()).update("pontos", pontosDaAPI);
                                //desenharRanking();
                            }

                        } else {
                            Log.d("FirestoreApp", "Esse ID de usuário não existe no banco.");
                        }
                    } else {
                        Log.w("FirestoreApp", "Erro ao buscar usuário.", task.getException());
                    }
                });
    }

    private void desenharRanking(){
        if (top != null && user.getNome() != null) {
            db.collection("Usuarios")
                    .whereGreaterThan("pontos", user.getPontos())
                    .count()
                    .get(AggregateSource.SERVER)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            leaderboard.removeAllViews();
                            AggregateQuerySnapshot snapshot = task.getResult();

                            // Quantidade de pessoas na frente dele
                            long usuariosNaFrente = snapshot.getCount();

                            // A posição dele será o número de pessoas na frente + 1
                            int posicaoAtual = ((int) usuariosNaFrente) + 1;
                            int pontos_agr = top[0].getPontos();
                            int pos_agr = 1;
                            addToLeaderBoard(1, top[0]);

                            for (int i = 1; i < top.length; i++) {
                                UsuarioRanking u = top[i];
                                int pos = i+1;
                                if (u.getPontos() == pontos_agr) pos = pos_agr; // Tratar empate como a mesma posição
                                addToLeaderBoard(pos, u);
                                pontos_agr = u.getPontos();
                                pos_agr = pos;
                            }

                            if (posicaoAtual > top_size) {
                                addToLeaderBoard(posicaoAtual, user);

                            } else {
                            }


                        } else {
                            Log.w("Ranking", "Erro ao calcular a posição.", task.getException());
                        }
                    });


        }
    }
    void addToLeaderBoard(int position, UsuarioRanking u) {
        View positionView = getLayoutInflater().inflate(R.layout.item_ranking, leaderboard, false);

        TextView Position = positionView.findViewById(R.id.position_number);
        TextView Nome = positionView.findViewById(R.id.nome_usuario);
        TextView Pontos = positionView.findViewById(R.id.pontos);
        TextView Cargos = positionView.findViewById(R.id.cargo_usuario);

        Position.setText(position + "º");
        Nome.setText(u.getNome());
        Pontos.setText(u.getPontos() + "");
        Cargos.setText(u.getCargo());

        if (position == 1) {
            Position.setTextColor(0xFFD3AF37);
        }
        if (position == 2) {
            Position.setTextColor(0xFFC4C4C4);
        }
        if (position == 3) {
            Position.setTextColor(0xFFCE8946);
        }
        if (Objects.equals(u.getUid(), user.getUid())) Nome.setTextColor(0xFF4040FF);

        leaderboard.addView(positionView);
    }

    //funções que nao sao da logica de ranking:
    public void navegar(View v) {
        int id = v.getId();
        if (id == R.id.home) {
            Intent it = new Intent(this, InicioActivity.class);
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

    public void CadastroAdmin(View v) {
        if (menu.isDrawerOpen(androidx.core.view.GravityCompat.END)) {
            menu.closeDrawer(androidx.core.view.GravityCompat.END);
        }
        startActivity(new Intent(this, CadastroAdminActivity.class));
    }
}