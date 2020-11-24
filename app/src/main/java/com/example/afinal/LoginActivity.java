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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends AppCompatActivity {

    private Button b;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressBar loadingProgressBar;
    private Toolbar loginToolbar;
    private TextView register;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        usernameEditText = findViewById(R.id.username);
        passwordEditText =findViewById(R.id.password);
        b = findViewById(R.id.login);
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

                ValidateChange(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        //usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    PerformLoginCheck(usernameEditText, passwordEditText);
                }
                return false;
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                PerformLoginCheck(usernameEditText, passwordEditText);
            }
        });
    }

    private boolean ValidateChange(String aUsername, String aPassword) {
        if(isUserNameValid(aUsername) && isPasswordValid(aPassword))
        {
            b.setEnabled(true);
            return true;
        }
        else
        {
            if(!isUserNameValid(aUsername))
            {
                usernameEditText.setError("Must be valid email address");
            }
            if(!isPasswordValid(aPassword))
            {
                 passwordEditText.setError("Password must be longer than 5 characters");
            }
            return false;
        }
    }

    public void Register(View view) {
        //implement register function here
        Toast.makeText(getApplicationContext(), "Registration clicked", Toast.LENGTH_SHORT).show();
    }

    public void PerformLoginCheck(EditText usernameEdit, EditText passwordEdit)
    {
        if (isUserNameValid(usernameEdit.getText().toString()) && isPasswordValid(passwordEdit.getText().toString()))
        {
            Toast.makeText(getApplicationContext(), "Welcome, " + usernameEdit.getText().toString(), Toast.LENGTH_SHORT).show();
            // Otherwise may need to load different activity or different parcelable data...
            Intent aboutMeIntent=new Intent(this,MainActivity.class);
            startActivity(aboutMeIntent);
        }
        else
        {
            if(!isUserNameValid(usernameEdit.getText().toString()))
            {
                Toast.makeText(getApplicationContext(), "Username is incorrect.", Toast.LENGTH_LONG).show();
            }
            if(!isPasswordValid(passwordEdit.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Username or password is incorrect.", Toast.LENGTH_LONG).show();
            }
        }

    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }


    public void ForgotPassword(View view) {
        Toast.makeText(getApplicationContext(), "Forgot Password Clicked!", Toast.LENGTH_SHORT).show();
    }
}