package com.PI.spoolcutter.ui.settings;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.PI.spoolcutter.R;
import com.PI.spoolcutter.databinding.FragmentSettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView textViewMessage;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Recupere os elementos de interface do usuário
        EditText editTextRede = binding.editTextRede;
        EditText editTextSenha = binding.editTextSenha;
        ImageView imageViewSend = binding.imageView4;
        textViewMessage = root.findViewById(R.id.textViewMessage); // Substitua 'R.id.textViewMessage' pelo ID correto do seu TextView


        // Define destinos
        String empresa = "Orange";
        String caminhoDocumento = "Teste/" + empresa + "/" + "Dispositivo"; // Substitua pelo caminho real do documento
        String campoParaLer = "WIFI"; // Substitua pelo nome real do campo que deseja ler

        // Inicialize o Firebase Authentication
        auth = FirebaseAuth.getInstance();
        // Inicialize o Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Atualize os campos de edição com dados do Firestore
        atualizaDadosFirestore(caminhoDocumento, campoParaLer);

        // Adicione um listener de clique à imagem "Send"
        imageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    // Recupere os valores dos campos de edição
                    String rede = editTextRede.getText().toString();
                    String senha = editTextSenha.getText().toString();

                    if (TextUtils.isEmpty(rede) || TextUtils.isEmpty(senha)) {
                        // Validação de campos em branco
                        displayMessage(getString(R.string.empty_field_error));
                    } else {
                        // Envie dados para o Firestore
                        sendDadosFirestore(caminhoDocumento, campoParaLer, empresa, rede, senha);
                    }
                } else {
                    // Usuário não autenticado
                    displayMessage(getString(R.string.login_required_error));
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

    // Exibe uma mensagem na interface do usuário
    private void displayMessage(String message) {


        handler.post(new Runnable() {
            @Override
            public void run() {
                textViewMessage.setText(message);
                textViewMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    // Envia dados para o Firestore
    private void sendDadosFirestore(String CaminhoDocumento, String Documento, String empresa, String rede, String senha) {
        // Crie um objeto de mapa com os dados que você deseja enviar
        Map<String, Object> dados = new HashMap<>();
        dados.put("rede", rede);
        dados.put("senha", senha);

        // Adicione os dados ao Firestore
        db.collection(CaminhoDocumento).document(Documento).set(dados)
                .addOnSuccessListener(documentReference -> {
                    // Exibe uma mensagem de sucesso
                    displayMessage(getString(R.string.data_sent_success));
                })
                .addOnFailureListener(e -> {
                    // Exibe uma mensagem de erro
                    displayMessage(getString(R.string.data_send_error) + e.getMessage());
                    Log.e("Firestore", "Erro ao enviar dados para o Firestore", e);
                });
    }

    // Atualiza os campos de edição com dados do Firestore
    private void atualizaDadosFirestore(String caminhoDocumento, String CampoParaLer){
        EditText editTextRede = binding.editTextRede;
        EditText editTextSenha = binding.editTextSenha;

        // Crie uma referência para o documento e leia o campo desejado
        db.document(caminhoDocumento + "/" + CampoParaLer).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // O documento existe, agora você pode ler o campo específico
                        String rede = documentSnapshot.getString("rede");
                        String senha = documentSnapshot.getString("senha");

                        if ((rede != null) || (senha != null)) {
                            editTextRede.setText(rede);
                            editTextSenha.setText(senha);
                        } else {
                            Log.e("Firestore_get", "Campo não encontrado");
                        }
                    } else {
                        Log.e("Firestore_get", "Documento não encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    // Exiba uma mensagem de erro
                    displayMessage(getString(R.string.read_document_error) + e.getMessage());
                    Log.e("Firestore", "Erro ao ler o documento", e);
                });
    }
}
