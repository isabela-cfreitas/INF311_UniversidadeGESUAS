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
    private EditText nome,dia,mes,ano,cpf,email,senha,senha_conf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        mAuth = FirebaseAuth.getInstance();
        nome = (EditText) findViewById(R.id.nome_res);
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
        String email_ = email.getText().toString();
        String senha_ = senha.getText().toString();
        String senha_conf_ = senha_conf.getText().toString();
        String nome_ = nome.getText().toString();
        String dia_ = dia.getText().toString();
        String mes_ = mes.getText().toString();
        String ano_ = ano.getText().toString();
        String cpf_ = cpf.getText().toString();
        String nasc_ = dia.getText().toString() + "/" + mes.getText().toString() + "/" + ano.getText().toString();
        if (validar_dados(nome_, dia_, mes_, ano_, cpf_, email_, senha_, senha_conf_)) {
            mAuth.createUserWithEmailAndPassword(email_, senha_).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    salvar_dados(task.getResult().getUser().getUid(), nome_, cpf_, nasc_);
                } else {
                    String erro = task.getException().getMessage();
                    Toast.makeText(this, "Erro: " + erro, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private Boolean validar_dados (String nome_,String dia_,String mes_,String ano_,String cpf_,String email_,String senha_,String senha_conf_) {
        boolean ok = false;
        if (email_.isEmpty() || senha_.isEmpty() || senha_conf_.isEmpty() || nome_.isEmpty() || dia_.isEmpty() || mes_.isEmpty() || ano_.isEmpty() || cpf_.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        } else if (!senha_.equals(senha_conf_)){
            Toast.makeText(this, "Senha ou confirmação de senha incorreta", Toast.LENGTH_SHORT).show();
        } else if (cpf_.length()!=11) {
            Toast.makeText(this, "Informe um cpf válido", Toast.LENGTH_SHORT).show();
        } else {
            ok = true;
        }
        return ok;
    }

    private void salvar_dados(String id_, String nome_, String cpf_, String nasc_) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nome", nome_);
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