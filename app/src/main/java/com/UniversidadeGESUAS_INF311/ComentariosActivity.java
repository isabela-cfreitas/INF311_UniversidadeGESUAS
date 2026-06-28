package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ComentariosActivity extends AppCompatActivity {
    String idPost;
    FirebaseFirestore db;
    private LinearLayout containerComents;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comentarios);

        db = FirebaseFirestore.getInstance();
        containerComents = findViewById(R.id.coments);
        Intent it = getIntent();
        idPost = it.getStringExtra("idPost");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarComentarios();
    }

    public void carregarComentarios() {
        db.collection("Posts").document(idPost).collection("Comentarios").orderBy("timestamp", Query.Direction.DESCENDING).limit(20).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Erro ao carregar comentários", Toast.LENGTH_SHORT).show();
                return;
            }
            if (task.getResult() != null) {
                containerComents.removeAllViews();

                if (task.getResult().isEmpty()) {
                    TextView txtVazio = new TextView(this);
                    txtVazio.setText("Ninguém comentou nesta publicação ainda");
                    txtVazio.setGravity(android.view.Gravity.CENTER);
                    txtVazio.setPadding(0, 60, 0, 0);
                    txtVazio.setTextColor(android.graphics.Color.parseColor("#64748B"));
                    containerComents.addView(txtVazio);
                } else {
                    for (QueryDocumentSnapshot publi : task.getResult()) {
                        String nome = publi.getString("nomeUsuario");
                        String cargo = publi.getString("cargo");
                        String conteudo = publi.getString("conteudo");
                        Timestamp time = publi.getTimestamp("timestamp");

                        View comentView = getLayoutInflater().inflate(R.layout.item_post_comunidade, containerComents, false);

                        TextView Nome = comentView.findViewById(R.id.nome_usuario);
                        TextView Cargo = comentView.findViewById(R.id.cargo_usuario);
                        TextView Conteudo = comentView.findViewById(R.id.conteudo_post);
                        TextView Data = comentView.findViewById(R.id.data_post);

                        View barraBotoes = comentView.findViewById(R.id.barra_botoes);
                        if (barraBotoes != null) {
                            barraBotoes.setVisibility(View.GONE);
                        }

                        Nome.setText(nome);
                        Cargo.setText(cargo);
                        Conteudo.setText(conteudo);

                        android.widget.ImageView fotoAutorComent = comentView.findViewById(R.id.perfil);
                        fotoAutorComent.setImageResource(R.drawable.perfil_beea);

                        if (nome != null && !nome.isEmpty()) {
                            fotoAutorComent.setTag(nome);

                            db.collection("Usuarios").whereEqualTo("nome_usuario", nome).get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            com.google.firebase.firestore.DocumentSnapshot userDoc = querySnapshot.getDocuments().get(0);

                                            String avatarAutor = userDoc.getString("avatar_nome");
                                            if (avatarAutor != null && !avatarAutor.isEmpty()) {
                                                int resId = getResources().getIdentifier(avatarAutor, "drawable", getPackageName());
                                                if (resId != 0) {
                                                    if (nome.equals(fotoAutorComent.getTag())) {
                                                        fotoAutorComent.setImageResource(resId);
                                                    }
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (nome.equals(fotoAutorComent.getTag())) {
                                            fotoAutorComent.setImageResource(R.drawable.perfil_beea);
                                        }
                                    });
                        }

                        if (time != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                            Data.setText(sdf.format(time.toDate()));
                        } else {
                            Data.setText("Agora mesmo");
                        }

                        containerComents.addView(comentView);
                    }
                }
            }
        });
    }

    public void novo_coment (View v) {
        Intent it = new Intent(getBaseContext(), NovoComentarioActivity.class);
        it.putExtra("idPost",idPost);
        startActivity(it);
    }

    public void voltar (View v) {
        finish();
    }
}