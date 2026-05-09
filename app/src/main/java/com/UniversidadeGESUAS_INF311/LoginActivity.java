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

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;
    private EditText email,senha;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.email_res);
        senha = (EditText) findViewById(R.id.senha_res);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private Boolean validar_dados (String email_,String senha_) {
        boolean ok = false;
        if (email_.isEmpty() || senha_.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        } else {
            ok = true;
        }
        return ok;
    }

    public void click_entrar (View v) {
        String email_ = email.getText().toString();
        String senha_ = senha.getText().toString();
        if (validar_dados(email_,senha_)) {
            mAuth.signInWithEmailAndPassword(email_,senha_).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(this, InicioActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String erro = task.getException().getMessage();
                    Toast.makeText(this, "Erro ao entrar: " + erro, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void click_back(View v) {
        finish();
    }
}