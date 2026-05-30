package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class NovoComentarioActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String idPost;
    private EditText texto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_novo_comentario);

        mAuth = FirebaseAuth.getInstance();
        texto = (EditText) findViewById(R.id.texto_coment);
        db = FirebaseFirestore.getInstance();
        Intent it = getIntent();
        idPost = it.getStringExtra("idPost");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void publicar_coment (View v) {
        String texto_ = texto.getText().toString().trim();
        if (texto_.isEmpty()) {
            Toast.makeText(this, "Escreva alguma coisa antes de postar o comentário!", Toast.LENGTH_SHORT).show();
            return;
        }
        String idUsuario= mAuth.getCurrentUser().getUid();
        db.collection("Usuarios").document(idUsuario).get().addOnSuccessListener(res -> {
                    if (res.exists()) {
                        String nomeUsuarioBanco = res.getString("nome_usuario");
                        String cargoBanco = res.getString("cargo");
                        Post novoComent = new Post(nomeUsuarioBanco, cargoBanco, texto_, Timestamp.now());

                        db.collection("Posts").document(idPost).collection("Comentarios").add(novoComent).addOnSuccessListener(documentReference -> {
                            db.collection("Posts").document(idPost).update("numeroComentarios", com.google.firebase.firestore.FieldValue.increment(1)).addOnSuccessListener(aVoid -> {
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                finish();
                            });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(NovoComentarioActivity.this, "Erro ao comentar", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(this, "Perfil do usuário não encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao buscar perfil", Toast.LENGTH_SHORT).show();
                });

    }

    public void voltar (View v) {
        finish();
    }
}