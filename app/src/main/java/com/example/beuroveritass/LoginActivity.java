package com.example.beuroveritass;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beuroveritass.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private String emailPattern = "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*";
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;

    int RC_SIGN_IN = 65;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        listeners();
    }

    private void signInFunc() { // this one's for google sign in
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            }catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d("TAG", "signInWithCredential:success");
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void listeners() {

        binding.btnLogin.setOnClickListener(view -> {
            signIn();
        });

        binding.ivGoogleLogo.setOnClickListener(view -> {
            signInWithGoogle();
        });

        binding.ivFacebookLogo.setOnClickListener(view -> {
            signInWithFacebook();
        });

        binding.tvSignUp.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

    }

    private void loading(boolean isLoading) {
        if(isLoading == true) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setVisibility(View.INVISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnLogin.setVisibility(View.VISIBLE);
        }
    }

    private void signInWithFacebook() {

    }

    private void signInWithGoogle() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("528433836492-c6l50fk25h0rv6inslpi5ca036nrgo9e.apps.googleusercontent.com")
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        signInFunc();
    }

    private void signIn() {

        if(isValidSignInDetails()) {
            loading(true);


            auth.signInWithEmailAndPassword(binding.etEmail.getText().toString().trim(), binding.etPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                loading(false);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                            else {
                                loading(false);
                                Exception e = task.getException();
                                Toast.makeText(LoginActivity.this, "Authentication failed. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }

    private boolean isValidSignInDetails() {

        if(binding.etEmail.getText().toString().trim().isEmpty()) {
            binding.etEmail.setError("Enter your email");
            return false;
        }
        else if(!Pattern.matches(emailPattern, binding.etEmail.getText().toString())) {
            binding.etEmail.setError("Enter valid email");
            return false;
        }
        else if(binding.etPassword.getText().toString().trim().isEmpty()) {
            binding.etPassword.setError("Enter your password");
            return false;
        }
        else if(binding.etPassword.getText().toString().length() < 6) {
            binding.etPassword.setError("Password length too small. Min. 6 required");
        }
        return true;
    }
}