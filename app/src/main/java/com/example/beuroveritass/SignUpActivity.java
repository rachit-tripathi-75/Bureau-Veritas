package com.example.beuroveritass;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beuroveritass.databinding.ActivitySignUpBinding;
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

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private final String emailPattern = "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*";
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;
    int RC_SIGN_IN = 65;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
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
                        if (task.isSuccessful()) {
                            Log.d("TAG", "signInWithCredential:success");
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void listeners() {
        binding.btnSignUp.setOnClickListener(view -> {
            try {
                signUp();
            }
            catch (Exception e) {
                Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.tvLogin.setOnClickListener(view -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        binding.ivGoogleLogo.setOnClickListener(view -> {
            signInWithGoogle();
        });

    }


    private void signInWithGoogle() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("528433836492-c6l50fk25h0rv6inslpi5ca036nrgo9e.apps.googleusercontent.com")
                .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        signInFunc();
    }

    private void signInFunc() { // this one's for google sign in
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signUp() throws IOException {

        if (isValidSignUpDetails()) {

            loading(true);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, " {\r\n    \"email\": \"rachit111@gmail.com\",\r\n    \"password\": \"111111\"\r\n}     ");
            Request request = new Request.Builder()
                    .url("https://bureauveritas.deificindia.com/api/register")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            int statusCode = response.code();

            if(statusCode == 200) {
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            }
            else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
//            auth.createUserWithEmailAndPassword(binding.etEmail.getText().toString().trim(), binding.etPassword.getText().toString().trim())
//                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                loading(false);
//                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
//                                finish();
//                            } else {
//                                loading(false);
//                                Exception e = task.getException();
//                                Toast.makeText(SignUpActivity.this, "Authentication failed. " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });

        }
    }

    private void loading(boolean isLoading) {
        if (isLoading == true) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSignUp.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnSignUp.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValidSignUpDetails() {
        if (binding.etEmail.getText().toString().trim().isEmpty()) {
            binding.etEmail.setError("Enter your email");
            return false;
        } else if (!Pattern.matches(emailPattern, binding.etEmail.getText().toString())) {
            binding.etEmail.setError("Enter valid email");
            return false;
        } else if (binding.etPassword.getText().toString().trim().isEmpty()) {
            binding.etPassword.setError("Enter your password");
            return false;
        } else if (binding.etConfirmPassword.getText().toString().trim().isEmpty()) {
            binding.etConfirmPassword.setError("Enter your confirm password");
            return false;
        } else if (binding.etPassword.getText().toString().length() < 6) {
            binding.etPassword.setError("Password length too small. Min. 6 required");
        } else if (binding.etConfirmPassword.getText().toString().length() < 6) {
            binding.etPassword.setError("Password length too small. Min. 6 required");
        } else if (!binding.etPassword.getText().toString().trim().equals(binding.etConfirmPassword.getText().toString().trim())) {
            binding.etPassword.setError("Password doesn't matches");
            binding.etConfirmPassword.setError("Password doesn't matches");
            return false;
        }
        return true;

    }
}