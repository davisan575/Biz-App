package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private Button b;
    private EditText email;
    private EditText password;
    private ProgressBar loadingProgressBar;
    private Toolbar loginToolbar;
    private TextView register;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        email = findViewById(R.id.username);
        password = findViewById(R.id.password);
        b = findViewById(R.id.login);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        loadingProgressBar = findViewById(R.id.loading);
        register = findViewById(R.id.register);
        loginToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(loginToolbar);
        ActionBar lt = getSupportActionBar();
        lt.setTitle("Login");

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {

                ContentCheck();
            }
        };

        password.addTextChangedListener(afterTextChangedListener);
        email.addTextChangedListener(afterTextChangedListener);

    }


        public void Login (View view){
            if (email.getText().toString().equals("") || password.getText().toString().equals("")) {
                Toast.makeText(this, "Please provide all information", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            currentUser = authResult.getUser();
                            if (currentUser.isEmailVerified()) {
                                Toast.makeText(LoginActivity.this, "Login Succesful.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify your email and login again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }

        public void Register (View view) {
            Toast.makeText(LoginActivity.this, "Register clicked.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, RegisterUser.class));
        }

        public boolean ContentCheck(){
            if(email.getText() != null && password.getText() != null)
            {
                b.setEnabled(true);
                return true;
            }
            else
            {
                return false;
            }
        }
}