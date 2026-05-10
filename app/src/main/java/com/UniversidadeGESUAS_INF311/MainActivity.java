package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //@Override
    //protected void onStart() {
    //    super.onStart();
    //    if (mAuth.getCurrentUser() != null) {
    //        startActivity(new Intent(this, InicioActivity.class));
    //        finish();
    //    }
    //}

    public void click_create(View v) {
        Intent it = new Intent(getBaseContext(), CadastroActivity.class);

        startActivity(it);
    }

    public void click_login(View v) {
        Intent it = new Intent(getBaseContext(), LoginActivity.class);

        startActivity(it);
    }
}