package com.PI.spoolcutter.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.PI.spoolcutter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Handler handler = new Handler();
    private Handler updateHandler = new Handler();

    private Runnable updateRunnable;
    private String CaminhoDocumento;
    private String CaminhoDados;
    private String CampoParaLer;
    private String Status;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        // Recuperar elementos de interface do usuário
        TextView textViewMessage = root.findViewById(R.id.textViewMessage);
        TextView viewTextUltimoComprimento = root.findViewById(R.id.textViewProducao);
        TextView viewTextProducaoAtual = root.findViewById(R.id.textViewCompriento);
        Button buttonIniciar = root.findViewById(R.id.buttonIniciar);
        EditText editTextComprimento = root.findViewById(R.id.editTextNumberComprimento);
        ProgressBar progressBar = root.findViewById(R.id.progressBar);

        // Definir destinos
        String empresa = "Orange";
        CaminhoDocumento = "Teste/" + empresa + "/Dispositivo";
        CampoParaLer = "Processo";
        CaminhoDados = CaminhoDocumento + "/" + CampoParaLer + "/Dados";

        // Inicializar Firebase Authentication
        auth = FirebaseAuth.getInstance();
        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Verificar e criar campos no Firestore, se necessário
        verificarECriarCamposFirestore();
        atualizaDadosFirestore();
        startPeriodicUpdate();
        
        // Definir ouvinte de clique para o botão "Iniciar"
        buttonIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                atualizaDadosFirestore();
                // Iniciar a atualização periódica
                startPeriodicUpdate();
                
                // Obter texto do EditText "editTextComprimento"
                String comprimento = editTextComprimento.getText().toString();
                String producaoAtual = viewTextProducaoAtual.getText().toString();
                String ultimoComprimento = viewTextUltimoComprimento.getText().toString();



                // Verificar se o campo "Comprimento" não está vazio
                if (!comprimento.isEmpty()) {
                    // Chamar o método sendDadosFirestore com o texto do EditText como argumento
                    sendDadosFirestore(comprimento, "1");

                } else {
                    // Exibir uma mensagem de erro
                    displayMessage("Favor insira um valor válido para o campo 'Comprimento'");
                }
            }
        });

        return root;
    }

    private void displayMessage(String message) {
        TextView textViewMessage = getView().findViewById(R.id.textViewMessage);
        textViewMessage.setText(message);
        textViewMessage.setVisibility(View.VISIBLE);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textViewMessage.setVisibility(View.INVISIBLE);
            }
        }, 5000);
    }


    private void sendDadosFirestore(String comprimento, String iniciar) {
        

        TextView textViewProducao = root.findViewById(R.id.textViewProducao);
        TextView textViewComprimento = root.findViewById(R.id.textViewCompriento);
       
        Map<String, Object> dados = new HashMap<>();

        // Primeiro, envie "Produção Atual"
        dados.put("Producao Atual", textViewProducao.getText());
        // Em seguida, envie "Ultimo Comprimento"
        dados.put("Ultimo Comprimento", textViewComprimento.getText());
        dados.put("Comprimento desejado", comprimento);
        dados.put("Iniciar", iniciar);
        dados.put("Status", "0");

        db.collection(CaminhoDocumento).document(CampoParaLer).set(dados)
                .addOnSuccessListener(documentReference -> {
                    displayMessage(getString(R.string.data_sent_success));

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", getString(R.string.data_send_error), e);
                });
    }
    private void SendDataFirestore(){

        TextView textViewProducao = root.findViewById(R.id.textViewProducao);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String dataAtual = dateFormat.format(new Date());
        Map<String, Object> prod = new HashMap<>();
        prod.put("Producao", textViewProducao.getText());
        prod.put("Data", dataAtual );


        db.collection(CaminhoDados).document(dataAtual).set(prod)
                .addOnSuccessListener(documentReference1 -> {
                    Log.w("Firestore", getString(R.string.data_sent_success));
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", getString(R.string.data_send_error), e);
                });
    }
    private void atualizaDadosFirestore() {

        TextView textViewProducao = root.findViewById(R.id.textViewProducao);
        TextView textViewComprimento = root.findViewById(R.id.textViewCompriento);

        db.document(CaminhoDocumento + "/" + CampoParaLer).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Status = documentSnapshot.getString("Status");
                        String producaoAtual = documentSnapshot.getString("Producao Atual");
                        String lastComprimento = documentSnapshot.getString("Ultimo Comprimento");

                        if (producaoAtual != null && lastComprimento != null) {
                            textViewProducao.setText(producaoAtual);
                            textViewComprimento.setText(lastComprimento);
                        }
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", getString(R.string.read_document_error), e);
                });
    }


    private void startPeriodicUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                atualizaDadosFirestore();
                checkStatusAndScheduleUpdate();
            }
        };

        // Agende a execução do Runnable com um atraso inicial de 0 (ou o valor que você preferir)
        updateHandler.postDelayed(updateRunnable, 1000); //delay de 1 segundos
    }

    private void checkStatusAndScheduleUpdate() {
        Button buttonIniciar = getView().findViewById(R.id.buttonIniciar);
        ProgressBar progressBar = getView().findViewById(R.id.progressBar);

        if (!"0".equals(Status) ) {
            progressBar.setVisibility(View.INVISIBLE);
            buttonIniciar.setVisibility(View.VISIBLE);
            SendDataFirestore();
            updateHandler.postDelayed(updateRunnable, 60000); //delay entre processos 1 minuto

        }else{
            progressBar.setVisibility(View.VISIBLE);
            buttonIniciar.setVisibility(View.INVISIBLE);

            updateHandler.postDelayed(updateRunnable, 1000); //delay entre processos

        }
    }

    private void stopPeriodicUpdate() {
        updateHandler.removeCallbacks(updateRunnable);
    }

    private void verificarECriarCamposFirestore() {
        db.collection(CaminhoDocumento).document(CampoParaLer).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Map<String, Object> dadosIniciais = new HashMap<>();
                        dadosIniciais.put("Comprimento desejado", "0");
                        dadosIniciais.put("Iniciar", "0");
                        dadosIniciais.put("Producao Atual", "0");
                        dadosIniciais.put("Ultimo Comprimento", "0");
                        dadosIniciais.put("Status", "1");

                        db.collection(CaminhoDocumento).document(CampoParaLer).set(dadosIniciais)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Documento criado com campos iniciais");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Erro ao criar o documento: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Erro ao verificar a existência do documento: " + e.getMessage());
                });

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String dataAtual = dateFormat.format(new Date());
        Map<String, Object> prod = new HashMap<>();
        prod.put("Producao", "0");

        db.collection(CaminhoDados).document(dataAtual).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        db.collection(CaminhoDados).document(dataAtual).set(prod)
                                .addOnSuccessListener(documentReference1 -> {
                                    Log.w("Firestore", getString(R.string.data_sent_success));
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", getString(R.string.data_send_error), e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Erro ao verificar a existência do documento: " + e.getMessage());
                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPeriodicUpdate();
    }
}
