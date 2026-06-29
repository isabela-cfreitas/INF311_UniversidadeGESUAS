package com.UniversidadeGESUAS_INF311;

import android.content.Context;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// Centraliza a lógica de busca de dados do usuário e do estado da Beea
public class BeeaRepository {
    private static final long DIAS_PARA_FICAR_BRAVA = 3;
    private static final long DIAS_SEM_INTERAGIR_CURSO = 5;
    private static final long PONTOS_PARA_FICAR_FELIZ = 500;

    public interface DadosUsuarioCallback {
        void onSucesso(String nomeOla, String avatarNome, boolean isAdmin);
        void onFalha();
    }

    public interface EstadoBeeaCallback {
        void onSucesso(String texto, int drawableRes);
        void onFalha();
    }

    private static class VariacaoBeea {
        String texto;
        int drawable;

        VariacaoBeea(String texto, int drawable) {
            this.texto = texto;
            this.drawable = drawable;
        }
    }

    // ---- NOME / AVATAR / CARGO ----
    public static void buscarDadosUsuario(FirebaseFirestore db, String idUsuario, DadosUsuarioCallback callback) {
        db.collection("Usuarios").document(idUsuario).get()
                .addOnSuccessListener(res -> {
                    if (!res.exists()) {
                        callback.onSucesso("Olá!", null, false);
                        return;
                    }
                    String nome = res.getString("nome_usuario");
                    String nomeOla = (nome != null && !nome.isEmpty()) ? ("Olá, " + nome + "!") : "Olá!";
                    String avatarNome = res.getString("avatar_nome");
                    String cargo = res.getString("cargo");
                    boolean isAdmin = "administrador".equalsIgnoreCase(cargo);
                    callback.onSucesso(nomeOla, avatarNome, isAdmin);
                })
                .addOnFailureListener(e -> callback.onFalha());
    }

    // ---- ESTADO DA BEEA (calcula, grava no Firestore e retorna texto + drawable) ----
    public static void calcularEAtualizarEstadoBeea(FirebaseFirestore db, String idUsuario, Context context, EstadoBeeaCallback callback) {
        DocumentReference meuDoc = db.collection("Usuarios").document(idUsuario);

        meuDoc.get().addOnSuccessListener(res -> {
            if (!res.exists()) {
                callback.onFalha();
                return;
            }

            Long pontos = res.getLong("pontos");
            if (pontos == null) pontos = 0L;
            final long pontosFinal = pontos;

            Long posicaoAnterior = res.getLong("posicao_ranking");
            if (posicaoAnterior == null) posicaoAnterior = 0L;
            final long posicaoAnteriorFinal = posicaoAnterior;

            Long sequenciaAtual = res.getLong("sequencia_dias");
            if (sequenciaAtual == null) sequenciaAtual = 0L;

            long diasSemAbrir = diasDesde(res.getTimestamp("ultimo_acesso"));
            long diasSemInteragir = diasDesde(res.getTimestamp("ultima_interacao_curso"));

            // ---- CÁLCULO DA SEQUÊNCIA ----
            long diasCalendario = diasCalendarDesde(res.getTimestamp("ultimo_acesso"));
            long novaSequencia;

            if (diasCalendario < 0) {
                // Nunca acessou antes: primeiro dia da sequência
                novaSequencia = 1;
            } else if (diasCalendario == 0) {
                // Já acessou hoje antes (abriu o app de novo no mesmo dia): mantém
                novaSequencia = sequenciaAtual == 0 ? 1 : sequenciaAtual;
            } else if (diasCalendario == 1) {
                // Acessou ontem, acessa hoje: continua a sequência
                novaSequencia = sequenciaAtual + 1;
            } else {
                // Ficou 2+ dias sem acessar: zera e reinicia em 1 (hoje conta como o novo dia 1)
                novaSequencia = 1;
            }

            // Busca o ranking de todo mundo, ordenado por pontos
            db.collection("Usuarios")
                    .orderBy("pontos", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(todosOsUsuarios -> {
                        long novaPosicao = 1;
                        for (int i = 0; i < todosOsUsuarios.size(); i++) {
                            if (todosOsUsuarios.getDocuments().get(i).getId().equals(idUsuario)) {
                                novaPosicao = i + 1;
                                break;
                            }
                        }

                        boolean primeiroAcesso = (res.getTimestamp("ultimo_acesso") == null);

                        String estado;

                        if (primeiroAcesso) {
                            estado = "neutra";
                        } else if (diasSemAbrir < 0 || diasSemAbrir >= DIAS_PARA_FICAR_BRAVA) {
                            estado = "brava";
                        } else if (diasSemInteragir < 0 || diasSemInteragir >= DIAS_SEM_INTERAGIR_CURSO) {
                            estado = "triste";
                        } else if (novaSequencia == 5) {
                            estado = "hexa";
                        } else if (posicaoAnteriorFinal > 0 && novaPosicao > posicaoAnteriorFinal) {
                            estado = "preocupada";
                        } else if (posicaoAnteriorFinal > 0 && novaPosicao < posicaoAnteriorFinal) {
                            estado = "orgulhosa";
                        } else if (pontosFinal >= PONTOS_PARA_FICAR_FELIZ) {
                            estado = "feliz";
                        } else {
                            estado = "neutra";
                        }

                        // Escolhe UMA variação completa (texto + arte juntos) pro estado decidido
                        VariacaoBeea[] opcoes = variacoesPara(context, estado);
                        VariacaoBeea escolhida = opcoes[new Random().nextInt(opcoes.length)];

                        // Grava tudo de volta no próprio documento
                        Map<String, Object> dados = new HashMap<>();
                        dados.put("posicao_ranking_anterior", posicaoAnteriorFinal);
                        dados.put("posicao_ranking", novaPosicao);
                        dados.put("beea_state", estado);
                        dados.put("beea_texto_card", escolhida.texto);
                        dados.put("sequencia_dias", novaSequencia);
                        dados.put("ultimo_acesso", FieldValue.serverTimestamp());
                        meuDoc.set(dados, SetOptions.merge());

                        callback.onSucesso(escolhida.texto, escolhida.drawable);
                    })
                    .addOnFailureListener(e -> callback.onFalha());
        }).addOnFailureListener(e -> callback.onFalha());
    }

    private static VariacaoBeea[] variacoesPara(Context context, String estado) {
        switch (estado) {
            case "brava":
                return new VariacaoBeea[]{
                        new VariacaoBeea(context.getString(R.string.beea_brava_1), R.drawable.beea_brava),
                        new VariacaoBeea(context.getString(R.string.beea_brava_2), R.drawable.beea_brava2),
                        new VariacaoBeea(context.getString(R.string.beea_brava_3), R.drawable.beea_brava3),
                };
            case "triste":
                return new VariacaoBeea[]{
                        new VariacaoBeea(context.getString(R.string.beea_triste_1), R.drawable.beea_triste),
                        new VariacaoBeea(context.getString(R.string.beea_triste_2), R.drawable.beea_triste2),
                };
            case "preocupada":
                return new VariacaoBeea[]{
                        new VariacaoBeea(context.getString(R.string.beea_preocupada_1), R.drawable.beea_triste_rank),
                        new VariacaoBeea(context.getString(R.string.beea_preocupada_2), R.drawable.beea_preocupada),
                };
            case "orgulhosa":
                return new VariacaoBeea[]{
                        new VariacaoBeea(context.getString(R.string.beea_orgulhosa_1), R.drawable.beea_orgulhosa),
                        new VariacaoBeea(context.getString(R.string.beea_orgulhosa_2), R.drawable.beea_orgulhosa2),
                };
            case "feliz":
                return new VariacaoBeea[]{
                        new VariacaoBeea(context.getString(R.string.beea_feliz_1), R.drawable.beea_feliz),
                        new VariacaoBeea(context.getString(R.string.beea_feliz_2), R.drawable.beea_feliz2),
                        new VariacaoBeea(context.getString(R.string.beea_feliz_3), R.drawable.bea_init),
                        new VariacaoBeea(context.getString(R.string.beea_feliz_4), R.drawable.bea_init),
                };
            case "hexa":
                return new VariacaoBeea[]{
                        new VariacaoBeea(context.getString(R.string.beea_hexa_1), R.drawable.beea_hexa),
                };
            default: // "neutra"
                return new VariacaoBeea[]{
                        new VariacaoBeea(context.getString(R.string.beea_neutra_1), R.drawable.bea_init),
                        new VariacaoBeea(context.getString(R.string.beea_neutra_2), R.drawable.bea_init)
                };
        }
    }

    private static long diasCalendarDesde(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return -1; // nunca acessou antes

        Calendar hoje = Calendar.getInstance();
        zerarHora(hoje);

        Calendar ultimoAcesso = Calendar.getInstance();
        ultimoAcesso.setTime(timestamp.toDate());
        zerarHora(ultimoAcesso);

        long diffMillis = hoje.getTimeInMillis() - ultimoAcesso.getTimeInMillis();
        return diffMillis / (1000 * 60 * 60 * 24);
    }

    private static void zerarHora(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private static long diasDesde(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return -1; // nunca aconteceu
        long diffMillis = System.currentTimeMillis() - timestamp.toDate().getTime();
        return diffMillis / (1000 * 60 * 60 * 24);
    }
}