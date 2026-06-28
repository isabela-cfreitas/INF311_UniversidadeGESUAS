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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CadastroAdminActivity extends AppCompatActivity {

    private EditText edtNome, edtNomeUsuario, edtDia, edtMes, edtAno, edtCpf, edtEmail, edtSenha, edtSenhaConf;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtNome = findViewById(R.id.nome_res);
        edtNomeUsuario = findViewById(R.id.nome_usuario_res);
        edtDia = findViewById(R.id.dia_res);
        edtMes = findViewById(R.id.mes_res);
        edtAno = findViewById(R.id.ano_res);
        edtCpf = findViewById(R.id.cpf_res);
        edtEmail = findViewById(R.id.email_res);
        edtSenha = findViewById(R.id.senha_res);
        edtSenhaConf = findViewById(R.id.senha_conf_res);
    }

    public void click_back(View v) {
        finish();
    }

    public void click_cadastrar_admin(View v) {
        String nome = edtNome.getText().toString().trim();
        String nomeUsuario = edtNomeUsuario.getText().toString().trim();
        String dia = edtDia.getText().toString().trim();
        String mes = edtMes.getText().toString().trim();
        String ano = edtAno.getText().toString().trim();
        String cpf = edtCpf.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();
        String senhaConf = edtSenhaConf.getText().toString().trim();

        if (nome.isEmpty() || nomeUsuario.isEmpty() || dia.isEmpty() || mes.isEmpty() ||
                ano.isEmpty() || cpf.isEmpty() || email.isEmpty() || senha.isEmpty() || senhaConf.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cpf.length()!=11 || !validar_cpf(cpf)) {
            Toast.makeText(this, "CPF inválido! Digite os 11 dígitos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!senha.equals(senhaConf)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.criar_conta_admin).setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        String dataNascimento = dia + "/" + mes + "/" + ano;
                        Map<String, Object> adminMap = new HashMap<>();
                        adminMap.put("nome", nome);
                        adminMap.put("nome_usuario", nomeUsuario);
                        adminMap.put("data_nascimento", dataNascimento);
                        adminMap.put("cpf", cpf);
                        adminMap.put("email", email);
                        adminMap.put("pontos", 0);
                        adminMap.put("cargo", "administrador");

                        db.collection("Usuarios").document(uid)
                                .set(adminMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Administrador criado com sucesso!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, AvatarActivity.class);
                                    intent.putExtra("isFromCadastro", true);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erro ao salvar no banco: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    findViewById(R.id.criar_conta_admin).setEnabled(true);
                                });
                    } else {
                        Toast.makeText(this, "Erro ao autenticar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        findViewById(R.id.criar_conta_admin).setEnabled(true);
                    }
                });
    }

    private static Boolean validar_cpf(String cpf_) {
        // CPFs com todos os números repetidos são considerados inválidos mesmo passando na conta
        boolean repetido = true;
        for (int i = 1; i <10; i++){
            if (cpf_.charAt(i) != cpf_.charAt(0)){
                repetido = false;
            }
        }
        if (repetido) return false;

        int soma = 0, d;
        for (int i = 0; i < 9; i++){
            soma += (cpf_.charAt(i) - '0') * (10-i);
        }
        soma = (soma * 10) % 11;
        if (soma == 10) soma = 0;
        if (soma != (cpf_.charAt(9) - '0')) return false;
        soma = 0;
        for (int i = 0; i < 10; i++){
            soma += (cpf_.charAt(i) - '0') * (11-i);
        }
        soma = (soma * 10) % 11;
        if (soma == 10) soma = 0;
        return soma == (cpf_.charAt(10) - '0');
    }
}