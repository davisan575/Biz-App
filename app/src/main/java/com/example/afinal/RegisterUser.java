package com.example.afinal;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity {
    private EditText mNameEditText;
    private EditText mLastNameEditText;
    private EditText email, password, passwordVerify, displayname, phonenumber;
    private Button mRegButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_user);
        email = (EditText) findViewById(R.id.usernameCreate);
        password = (EditText) findViewById(R.id.my_password_first);
        passwordVerify = (EditText) findViewById(R.id.my_password_second);
        displayname = (EditText) findViewById(R.id.create_display_name);
        phonenumber = (EditText) findViewById(R.id.editcontactPhone);
        mNameEditText = (EditText) findViewById(R.id.editcontactName);
        mLastNameEditText = (EditText) findViewById(R.id.editcontactLastname);
        mRegButton = (Button) findViewById(R.id.registertButton);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

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

                ValidateChange(email.getText().toString(),
                        password.getText().toString());
            }
        };

        password.addTextChangedListener(afterTextChangedListener);

    }

    private void saveUserDataToDB() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("Users");
        usersRef.child(currentUser.getUid()).setValue(new User(displayname.getText().toString(),
                email.getText().toString(), phonenumber.getText().toString()));

    }

    public void Signup(View view) {

        if (email.getText().toString().equals("") || password.getText().toString().equals("")
                || phonenumber.getText().toString().equals("") || displayname.getText().toString().equals("")) {
            Toast.makeText(this, "Please provide all information", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        currentUser = authResult.getUser();
                        currentUser.sendEmailVerification().addOnSuccessListener(RegisterUser.this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(RegisterUser.this, "Signup successful. Verification email Sent!", Toast.LENGTH_SHORT).show();
                                saveUserDataToDB();
                                finish();
                            }
                        }).addOnFailureListener(RegisterUser.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterUser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterUser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean ValidateChange(String aUsername, String aPassword) {
        if( isPasswordValid(aPassword))
        {
            mRegButton.setEnabled(true);
            return true;
        }
        else
        {
            if(!isUserNameValid(aUsername))
            {
                email.setError("Must be valid email address");
            }
            if(!isPasswordValid(aPassword))
            {
                password.setError("Password must be longer than 5 characters");
            }
            return false;
        }
    }

//Allegedly not needed?
//    public void sendEmailVerification (View view){
//        if (mAuth.getCurrentUser() == null) {
//            Toast.makeText(this, "Please login first to resend verification email.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        currentUser.sendEmailVerification()
//                .addOnSuccessListener(SignupLogin.this, new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(SignupLogin.this, "Verification email Setn!", Toast.LENGTH_SHORT).show();
//                        updateUI();
//                    }
//                }).addOnFailureListener(SignupLogin.this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(SignupLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private boolean isPasswordValid(String password) {
        Log.d("isPasswordValid", "verify: " + passwordVerify.getText().toString());
        return password != null && password.trim().length() > 5;
    }

    private boolean isUserNameValid(String username) {
            return username != null;
    }
}