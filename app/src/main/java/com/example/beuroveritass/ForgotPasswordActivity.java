package com.example.beuroveritass;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beuroveritass.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private String emailPattern = "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listeners();
    }

    private void listeners() {
        if (!Pattern.matches(emailPattern, binding.etForgetEmail.getText().toString()) && !binding.etForgetEmail.getText().toString().isEmpty()) {
            binding.etForgetEmail.setError("Enter valid email");
        } else {
            binding.btnSendOTP.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {
                    String email = binding.etForgetEmail.getText().toString().trim();

                    if (TextUtils.isEmpty(email)) {
                        binding.etForgetEmail.setError("Email is required.");
                        return;
                    }

                    loading(true);
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                                finish();
                                loading(false);
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                loading(false);
                            }
                        }
                    });
                }
            });
        }

    }


    private void loading(boolean isLoading) {
        if (isLoading == true) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSendOTP.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnSendOTP.setVisibility(View.VISIBLE);
        }
    }
}