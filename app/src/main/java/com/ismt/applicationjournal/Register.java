package com.ismt.applicationjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {
    Button register_btn;
    EditText username_input, email_input, password_input, confirmPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        TextView register_label = findViewById(R.id.register_label);
//        register_label.setOnClickListener( v -> {
//            startActivity(new Intent(this, Register.class));
//            this.finish();
//        });

        mAuth = FirebaseAuth.getInstance();

        TextView login_label = findViewById(R.id.login_label) ;
        login_label.setOnClickListener( v -> {
            startActivity(new Intent(this, login.class));
        });
        username_input = findViewById(R.id.username_input);
        email_input = findViewById(R.id.email_input);
        password_input = findViewById(R.id.password_input);
        confirmPassword = findViewById(R.id.confirmPassword);

        register_btn = findViewById(R.id.register_btn);
        register_btn.setOnClickListener( v -> {
            registerUser();
        });
    }

    private void registerUser() {
        String email = email_input.getText().toString().trim();
        String full_name = username_input.getText().toString().trim();
        String password = password_input.getText().toString().trim();


        if (full_name.isEmpty()) {
            username_input.setError("User name is required");
            username_input.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            email_input.setError("Email is required");
            email_input.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_input.setError("Provide valid email");
            email_input.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            password_input.setError("Password is required");
            password_input.requestFocus();
            return;
        }
        if (password.length() < 6) {
            password_input.setError("Password must be at least 6 char");
            password_input.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            confirmPassword.setError("Re-type your password");}

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Register.this, "User has been registered successfully", Toast.LENGTH_SHORT).show();
                            User user = new User(full_name, email);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser()
                                            .getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, "User has been registered successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Register.this, "Failed try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(Register.this, "User is already registered !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}