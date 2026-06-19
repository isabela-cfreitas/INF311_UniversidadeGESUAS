package com.UniversidadeGESUAS_INF311;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CursoAdapter extends RecyclerView.Adapter<CursoAdapter.ViewHolder> {

    private final List<Curso> cursos;

    public CursoAdapter(List<Curso> cursos) {
        this.cursos = cursos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_cursos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Curso curso = cursos.get(position);
        holder.txtTitulo.setText(curso.getTitulo());
        holder.txtData.setText(curso.getData());
        holder.txtHora.setText(curso.getHora());

        // Abre o site ao clicar no card
        holder.itemView.setOnClickListener(v -> {
            String url = curso.getUrlCurso();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cursos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtData, txtHora;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloCurso);
            txtData   = itemView.findViewById(R.id.txtDataCurso);
            txtHora   = itemView.findViewById(R.id.txtHoraCurso);
        }
    }
}