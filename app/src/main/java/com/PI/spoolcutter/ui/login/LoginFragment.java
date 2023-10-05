package com.PI.spoolcutter.ui.login;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.PI.spoolcutter.databinding.FragmentLoginBinding;
import com.PI.spoolcutter.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

public class LoginFragment extends Fragment {

   // private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001; // Código de solicitação para o login do Google
    private ProgressDialog progressDialog;
    private Handler handler = new Handler();
    private static final String TAG = "LoginFragment"; // Defina a TAG

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Configurar as opções de login do Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

       // loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
               // .get(LoginViewModel.class);


        final ProgressBar loadingProgressBar = binding.loading;
        final Button btnGoogle = binding.BtnGoogle;

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Logging in with Google...");
        progressDialog.setCancelable(false);

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar o processo de login do Google
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // Resto do seu código permanece o mesmo
    }

    // Método onActivityResult para lidar com o resultado do login do Google
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verificar se a resposta é do processo de login do Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Autenticar com o Firebase usando a conta do Google
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Lidar com erros de autenticação
                Log.w(TAG, "Google sign in failed", e);
                progressDialog.dismiss(); // Certifique-se de fechar o pop-up em caso de falha
                // Exibir uma mensagem de erro para o usuário
                Toast.makeText(requireContext(), "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para autenticar com o Firebase usando as credenciais do Google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss(); // Fechar o pop-up de progresso

                        if (task.isSuccessful()) {
                            // Login bem-sucedido
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Exibir a mensagem de boas-vindas
                            String welcomeMessage = "Bem-vindo, " + user.getDisplayName() + "!";
                            TextView welcomeTextView = binding.welcomeMessage;
                            welcomeTextView.setText(welcomeMessage);

                            // Faça qualquer ação adicional aqui, como atualizar a UI
                        } else {
                            // Login falhou
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            // Exibir uma mensagem de erro para o usuário
                            Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Método onDestroyView para limpar o Handler
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        handler.removeCallbacksAndMessages(null); // Limpar todas as ações pendentes do Handler
    }
}
