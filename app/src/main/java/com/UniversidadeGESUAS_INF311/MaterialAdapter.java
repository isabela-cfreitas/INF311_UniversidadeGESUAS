package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {
    private final List<Material> materiais;

    public MaterialAdapter(List<Material> materiais) {
        this.materiais = materiais;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_materiais, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Material material = materiais.get(position);
        holder.txtNome.setText(material.getNome());
        holder.cardRoot.setBackgroundColor(Color.parseColor(material.getCor()));

        // Abre o site ao clicar no card
        holder.itemView.setOnClickListener(v -> {

            // Registra a interação com o material no Firestore
            String idUsuario = FirebaseAuth.getInstance().getUid();
            if (idUsuario != null) {
                FirebaseFirestore.getInstance()
                        .collection("Usuarios")
                        .document(idUsuario)
                        .update("ultima_interacao_curso", FieldValue.serverTimestamp());
            }

            String url = "https://membros.universidadegesuas.com.br/auth/login";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return materiais.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome;
        RelativeLayout cardRoot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome  = itemView.findViewById(R.id.txtNomeMaterial);
            cardRoot = itemView.findViewById(R.id.cardRoot);
        }
    }
}