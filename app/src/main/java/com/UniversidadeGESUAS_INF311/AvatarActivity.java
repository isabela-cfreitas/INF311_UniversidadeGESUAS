package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;

public class AvatarActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean veioDoCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        veioDoCadastro = getIntent().getBooleanExtra("isFromCadastro", false);

        List<String> listaAvatares = Arrays.asList("nino", "ju", "juca", "drone", "ariana", "anitta");
        LinearLayout container = findViewById(R.id.containerAvatares);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (String nomeAvatar : listaAvatares) {
            View itemView = inflater.inflate(R.layout.item_avatar, container, false);
            ImageView imgAvatarItem = itemView.findViewById(R.id.imgAvatarItem);
            int resId = getResources().getIdentifier(nomeAvatar, "drawable", getPackageName());
            if (resId != 0) {
                imgAvatarItem.setImageResource(resId);
            }
            imgAvatarItem.setOnClickListener(v -> salvarAvatarNoFirebase(nomeAvatar));
            container.addView(itemView);
        }
    }

    private void salvarAvatarNoFirebase(String nomeAvatar) {
        String uid = mAuth.getCurrentUser().getUid();
        java.util.HashMap<String, Object> dados = new java.util.HashMap<>();
        dados.put("avatar_nome", nomeAvatar);

        db.collection("Usuarios").document(uid).set(dados, com.google.firebase.firestore.SetOptions.merge()).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Avatar atualizado!", Toast.LENGTH_SHORT).show();
            if (veioDoCadastro) {
                Intent intent = new Intent(AvatarActivity.this, InicioActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erro ao salvar avatar.", Toast.LENGTH_SHORT).show();
        });
    }
}