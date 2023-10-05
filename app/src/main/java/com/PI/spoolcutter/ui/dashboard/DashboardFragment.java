package com.PI.spoolcutter.ui.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.PI.spoolcutter.R;
import com.PI.spoolcutter.databinding.FragmentDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView firebaseData;
    private EditText editTextProductionNumber;
    private TextView textViewMessage;
    private Handler handler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        final Button button = binding.button2;
        firebaseData = root.findViewById(R.id.firebase_data);
        editTextProductionNumber = root.findViewById(R.id.editTextProductionNumber);
        textViewMessage = root.findViewById(R.id.textViewMessage);

        // Inicialize o Firebase Authentication
        auth = FirebaseAuth.getInstance();
        // Inicialize o Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Crie um handler para manipular a exibição de mensagens
        handler = new Handler(Looper.getMainLooper());

        // Define o clique do botão
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtenha o usuário atualmente autenticado
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    String email = user.getEmail();
                    String numeroProducao = editTextProductionNumber.getText().toString();
                    String empresa = "Orange";
                    int numProd = Integer.parseInt(numeroProducao);

                    // Verifique se o número de produção não está vazio
                    if (numeroProducao.isEmpty()) {
                        displayMessage("Digite o número de produção"); // Exibe uma mensagem de erro
                        return;
                    }

                    // Obtenha a data atual (pode ser uma string no formato que você preferir)
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String dataAtual = dateFormat.format(new Date());

                    String caminhoDocumento = "Teste/"+empresa+"/"+ email+"/"+ dataAtual; // Substitua pelo caminho real do documento
                    String campoParaLer = "numero_producao"; // Substitua pelo nome real do campo que deseja ler

                    final int[] valorC = {0}; // Crie uma variável final (array) para armazenar o valor
                    final String[] numeroProducaoFinal = {numeroProducao}; // Crie uma variável final (array) para armazenar numeroProducao

                    // Crie uma referência para o documento e leia o campo desejado
                    db.document(caminhoDocumento).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // O documento existe, agora você pode ler o campo específico
                                    String valorCampo = documentSnapshot.getString(campoParaLer);

                                    if (valorCampo != null) {
                                        valorC[0] = Integer.parseInt(valorCampo); // Modifique o valor dentro do array
                                        int novoValor = numProd + valorC[0]; // Acesse o valor modificado
                                        numeroProducaoFinal[0] = String.valueOf(novoValor);

                                        // Crie um objeto de mapa com os dados que você deseja enviar
                                        Map<String, Object> dados = new HashMap<>();
                                        dados.put("email", email);
                                        dados.put("data_atual", dataAtual);
                                        dados.put("numero_producao", numeroProducaoFinal[0]);

                                        // Exiba os dados no TextView
                                        displayMessage("Email: " + dados.get("email") + "\nNúmero de Produção: " + dados.get("numero_producao") + "\nData Atual: " + dados.get("data_atual"));

                                        // Adicione os dados ao Firestore
                                        db.collection("Teste/"+empresa+"/"+ email).document(dataAtual).set(dados)
                                                .addOnSuccessListener(documentReference -> {
                                                    // Exiba uma mensagem de sucesso
                                                    displayMessage("Dados enviados com sucesso para o Firestore");
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Exiba uma mensagem de erro
                                                    displayMessage("Erro ao enviar dados para o Firestore: " + e.getMessage());
                                                    Log.e("Firestore", "Erro ao enviar dados para o Firestore", e);
                                                });
                                    } else {
                                        displayMessage("Campo não encontrado");
                                    }
                                } else {
                                    displayMessage("Documento não encontrado");
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Exiba uma mensagem de erro
                                displayMessage("Erro ao ler o documento: " + e.getMessage());
                                Log.e("Firestore", "Erro ao ler o documento", e);
                            });
                } else {
                    displayMessage("Faça login para enviar dados"); // Exibe uma mensagem se o usuário não estiver autenticado
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Método para exibir mensagens no TextView em uma thread de IU
    private void displayMessage(String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                textViewMessage.setText(message);
                textViewMessage.setVisibility(View.VISIBLE); // Torna o TextView visível
            }
        });
    }
}
