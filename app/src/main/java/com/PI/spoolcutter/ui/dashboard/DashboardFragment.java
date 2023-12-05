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
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.PI.spoolcutter.R;
    import com.PI.spoolcutter.databinding.FragmentDashboardBinding;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FieldPath;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;

    import java.text.SimpleDateFormat;
    import java.util.Collections;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Locale;
    import java.util.Map;
    import java.util.ArrayList;
    import java.util.List;

    public class DashboardFragment extends Fragment {

        private FragmentDashboardBinding binding;
        private FirebaseAuth auth;
        private FirebaseFirestore db;
        private TextView firebaseData;
        private EditText editTextProductionNumber;
        private TextView textViewMessage;
        private Handler handler = new Handler();
        private Handler updateHandler = new Handler();

        private Runnable updateRunnable;

        private String CaminhoDocumento;
        private String CaminhoDados;
        private String CampoParaLer;

        private RecyclerView recyclerView;
        private DashboardAdapter adapter;

        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

            // Definir destinos
            String empresa = "Orange";
            CaminhoDocumento = "Teste/" + empresa + "/Dispositivo";
            CampoParaLer = "Processo";
            CaminhoDados = CaminhoDocumento + "/" + CampoParaLer + "/Dados";

            recyclerView = root.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new DashboardAdapter(generateSampleData());
            recyclerView.setAdapter(adapter);


            // Inicializar Firebase Authentication
            auth = FirebaseAuth.getInstance();
            // Inicializar Firebase Firestore
            db = FirebaseFirestore.getInstance();

            getDadosFirestore();
            // Verificar e criar campos no Firestore, se necessário
            startPeriodicUpdate();


            return root;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }


        private List<DashboardItem> generateSampleData() {
            List<DashboardItem> data = new ArrayList<>();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            //data.add(new DashboardItem(currentDate, "0"));

            return data;
        }

        private void startPeriodicUpdate() {
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    getDadosFirestore();
                    checkStatusAndScheduleUpdate();
                }
            };

            // Agende a execução do Runnable com um atraso inicial de 0 (ou o valor que você preferir)
            updateHandler.postDelayed(updateRunnable, 1000); //delay de 1 segundos
        }

        private void checkStatusAndScheduleUpdate() {

            updateHandler.postDelayed(updateRunnable, 1000); //delay entre processos 1 segundo

        }

        private void stopPeriodicUpdate() {
            updateHandler.removeCallbacks(updateRunnable);
        }


        private void getDadosFirestore() {
            db.collection(CaminhoDados) // Acesse a coleção no caminho /Teste/Orange/Dispositivo/Processo/Dados
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DashboardItem> data = new ArrayList<>(); // Crie uma nova lista para armazenar os dados

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Para cada documento dentro da coleção, obtenha os dados de data e produção
                            String dataProduzida = document.getId(); // O ID do documento é a data produzida
                            String producaoDoDia = document.getString("Producao"); // Substitua "producao" pelo nome do campo no Firestore

                            // Log dos dados
                            Log.d("Firestore", "Data Produzida: " + dataProduzida);
                            Log.d("Firestore", "Produção do Dia: " + producaoDoDia);

                            if(Integer.parseInt(producaoDoDia)>0){
                                // Crie um objeto DashboardItem com os dados e adicione-o à lista
                                DashboardItem item = new DashboardItem(dataProduzida, producaoDoDia);
                                data.add(item);

                            }

                        }

                        // Inverta a ordem dos itens
                        Collections.reverse(data);
                        // Atualize o adaptador com os dados obtidos do Firestore
                        adapter.setData(data);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", getString(R.string.read_document_error), e);
                    });
        }
    }
