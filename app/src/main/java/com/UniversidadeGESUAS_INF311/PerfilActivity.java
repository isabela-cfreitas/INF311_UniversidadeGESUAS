package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText usuario, email;
    private TextView cargo, pontuacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();
        usuario = (EditText) findViewById(R.id.perfil_nome);
        email = (EditText) findViewById(R.id.perfil_email);
        db = FirebaseFirestore.getInstance();
        cargo = findViewById(R.id.perfil_cargo);
        pontuacao = findViewById(R.id.perfil_pontuacao);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        carregarDados();
    }

    protected void carregarDados () {
        String idUser = mAuth.getCurrentUser().getUid();
        db.collection("Usuarios").document(idUser).get().addOnSuccessListener(res -> {
                    if (res.exists()) {
                        String nomeUsuarioBanco = res.getString("nome_usuario");
                        String cargoBanco = res.getString("cargo");
                        Long pontos = res.getLong("pontos");

                        cargo.setText(cargoBanco);
                        usuario.setText(nomeUsuarioBanco);
                        String emailUsuario = mAuth.getCurrentUser().getEmail();
                        email.setText(emailUsuario);
                        if (pontos != null) {
                           pontuacao.setText(pontos + " pontos");
                        } else {
                            pontuacao.setText("0 pontos");
                        }
                    } else {
                        Toast.makeText(this, "Perfil do usuário não encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao buscar perfil", Toast.LENGTH_SHORT).show();
                });
    }

    public void sair(View v) {
        mAuth.signOut();
        Intent it = new Intent(this, LoginActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //tem q limpar pra usuario nao conseguir "relogar" dps só com o botão de voltar
        startActivity(it);
        finish();
    }

    public void voltar(View v) {
        finish();
    }

}