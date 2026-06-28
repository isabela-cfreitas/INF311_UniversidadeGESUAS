package com.UniversidadeGESUAS_INF311;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NovoPostActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText texto;
    private ImageView fotoPerfilCriador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_novo_post);

        mAuth = FirebaseAuth.getInstance();
        texto = (EditText) findViewById(R.id.texto_post);
        fotoPerfilCriador = findViewById(R.id.foto_perfil);
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        carregarAvatarUsuarioLogado();
    }

    private void carregarAvatarUsuarioLogado() {
        String idUsuario = mAuth.getCurrentUser().getUid();
        db.collection("Usuarios").document(idUsuario).get().addOnSuccessListener(res -> {
            if (res.exists()) {
                String avatarLogado = res.getString("avatar_nome");
                if (avatarLogado != null && !avatarLogado.isEmpty()) {
                    int resId = getResources().getIdentifier(avatarLogado, "drawable", getPackageName());
                    if (resId != 0) {
                        fotoPerfilCriador.setImageResource(resId);
                    }
                }
            }
        });
    }

    public void publicar_post (View v) {
        String texto_ = texto.getText().toString().trim();
        if (texto_.isEmpty()) {
            Toast.makeText(this, "Escreva alguma coisa antes de postar!", Toast.LENGTH_SHORT).show();
            return;
        }
        String idUsuario= mAuth.getCurrentUser().getUid();
        db.collection("Usuarios").document(idUsuario).get().addOnSuccessListener(res -> {
            if (res.exists()) {
                String nomeUsuarioBanco = res.getString("nome_usuario");
                String cargoBanco = res.getString("cargo");
                Post novoPost = new Post(nomeUsuarioBanco, cargoBanco, texto_, Timestamp.now());

                db.collection("Posts").add(novoPost).addOnSuccessListener(documentReference -> {
                    finish();
                }).addOnFailureListener(e -> {
                        Toast.makeText(NovoPostActivity.this, "Erro ao publicar", Toast.LENGTH_SHORT).show();
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