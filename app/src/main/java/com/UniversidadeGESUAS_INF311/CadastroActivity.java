package com.UniversidadeGESUAS_INF311;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText nome,nome_usuario,dia,mes,ano,cpf,email,senha,senha_conf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        mAuth = FirebaseAuth.getInstance();
        nome = (EditText) findViewById(R.id.nome_res);
        nome_usuario = (EditText) findViewById(R.id.nome_usuario_res);
        dia = (EditText) findViewById(R.id.dia_res);
        mes = (EditText) findViewById(R.id.mes_res);
        ano = (EditText) findViewById(R.id.ano_res);
        cpf = (EditText) findViewById(R.id.cpf_res);
        email = (EditText) findViewById(R.id.email_res);
        senha = (EditText) findViewById(R.id.senha_res);
        senha_conf = (EditText) findViewById(R.id.senha_conf_res);

        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    public void click_cadastrar (View v) {
        String email_ = email.getText().toString().trim();
        String senha_ = senha.getText().toString();
        String senha_conf_ = senha_conf.getText().toString();
        String nome_ = nome.getText().toString().trim();
        String nome_usuario_ = nome_usuario.getText().toString().trim();
        String dia_ = dia.getText().toString().trim();
        String mes_ = mes.getText().toString().trim();
        String ano_ = ano.getText().toString().trim();
        String cpf_ = cpf.getText().toString().trim();
        String nasc_ = dia.getText().toString() + "/" + mes.getText().toString() + "/" + ano.getText().toString();
        if (validar_dados(nome_,nome_usuario_, dia_, mes_, ano_, cpf_, email_, senha_, senha_conf_)) {

            db.collection("Usuarios").whereEqualTo("nome_usuario", nome_usuario_).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    Toast.makeText(this, "Este nome de usuário já está em uso.", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email_, senha_).addOnCompleteListener(this, task2 -> {
                        if (task2.isSuccessful()) {
                            salvar_dados(task2.getResult().getUser().getUid(), nome_, nome_usuario_, cpf_, nasc_);
                        } else {
                            String erro = task2.getException().getMessage();
                            Toast.makeText(this, "Erro: " + erro, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }

    private Boolean validar_dados (String nome_,String nome_usuario_,String dia_,String mes_,String ano_,String cpf_,String email_,String senha_,String senha_conf_) {
        boolean ok = false;
        if (email_.isEmpty() || senha_.isEmpty() || senha_conf_.isEmpty() || nome_.isEmpty() || dia_.isEmpty() || mes_.isEmpty() || ano_.isEmpty() || cpf_.isEmpty() || nome_usuario_.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        } else if (!senha_.equals(senha_conf_)){
            Toast.makeText(this, "Senha ou confirmação de senha incorreta", Toast.LENGTH_SHORT).show();
        } else if (cpf_.length()!=11) {
            Toast.makeText(this, "Informe um cpf válido", Toast.LENGTH_SHORT).show();
        } else if (!nome_usuario_.matches("^[a-zA-Z0-9_]+$")) {
            Toast.makeText(this, "O nome de usuário deve conter apenas letras, números e underlines (_)", Toast.LENGTH_SHORT).show();
        } else {
            ok = true;
        }
        return ok;
    }

    private void salvar_dados(String id_, String nome_,String nome_usuario_, String cpf_, String nasc_) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nome", nome_);
        usuario.put("nome_usuario",nome_usuario_);
        usuario.put("cargo", "estudante");
        usuario.put("cpf", cpf_);
        usuario.put("dataNascimento", nasc_);
        usuario.put("pontos", 0);

        db.collection("Usuarios").document(id_)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CadastroActivity.this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void click_back(View v) {
        finish();
    }
}