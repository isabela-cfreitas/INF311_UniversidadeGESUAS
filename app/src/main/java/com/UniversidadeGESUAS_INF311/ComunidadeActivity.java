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

public class ComunidadeActivity extends AppCompatActivity {

    private LinearLayout containerPosts;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comunidade);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db= FirebaseFirestore.getInstance();
        containerPosts = findViewById(R.id.posts);

        carregarPost();
    }

    public void carregarPost() {
        db.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING).addSnapshotListener((value,error)->{
            if (error != null) {
                Toast.makeText(this, "Erro ao carregar o feed", Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null) {
                containerPosts.removeAllViews();//do jeito que eu to fazendo ele vai recontar todos os posts
                // toda vez, é meio ineficiente mas nao sei fazer de outro jeito, por isso to limpando as view antes

                for (QueryDocumentSnapshot publi : value) {
                    String nome = publi.getString("nomeUsuario");
                    String cargo = publi.getString("cargo");
                    String conteudo = publi.getString("conteudo");
                    Long curtidas = publi.getLong("numeroCurtidas");
                    Long comentarios = publi.getLong("numeroComentarios");
                    Timestamp time = publi.getTimestamp("timestamp");

                    //esse método de getLayoutInflater deveria pegar o xml que eu passei e fingir que ele é um view
                    // normal, se estiver dando crash depois isso tem potencial de ser o causador, pq nao entendi mt bem como usa :)
                    View postView = getLayoutInflater().inflate(R.layout.item_post_comunidade, containerPosts, false);

                    TextView Nome = postView.findViewById(R.id.nome_usuario);
                    TextView Cargo = postView.findViewById(R.id.cargo_usuario);
                    TextView Conteudo = postView.findViewById(R.id.conteudo_post);
                    TextView Curtidas = postView.findViewById(R.id.contador_curtidas);
                    TextView Comentarios = postView.findViewById(R.id.contador_comentarios);
                    TextView Data = postView.findViewById(R.id.data_post);

                    Nome.setText(nome);
                    Cargo.setText(cargo);
                    Conteudo.setText(conteudo);
                    Curtidas.setText(String.valueOf(curtidas != null ? curtidas : 0));
                    Comentarios.setText(String.valueOf(comentarios != null ? comentarios : 0));
                    if (time != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                        Data.setText(sdf.format(time.toDate()));
                    } else {
                        Data.setText("Agora mesmo");
                    }
                    containerPosts.addView(postView);
                }
            }
        });
    }

    public void novo_post (View v) {
        Intent it = new Intent(getBaseContext(), NovoPostActivity.class);
        startActivity(it);
    }

    public void navegar(View v) {
        int id = v.getId();
        if (id == R.id.home) {
            startActivity(new Intent(this, InicioActivity.class));
        } else if (id == R.id.ranking) {
            startActivity(new Intent(this, RankingActivity.class));
        } else if (id == R.id.comunidade) {

        }
    }
}