package com.example.afinal;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

public class RegisterUser extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{
    private Button mRegButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    EditText et_email;
    EditText et_fn;
    EditText et_ln;
    EditText et_password;
    EditText et_repeat_password;
    EditText et_company;
    EditText et_phonenumber;
    FloatingActionButton fab_save;

    private static final int REQUEST_FOR_CAMERA=0011;
    private static final int OPEN_FILE=0012;
    private Uri imageUri=null;
    ImageView aImg;
    boolean imageUploaded;
    boolean wait;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOR_CAMERA && resultCode == RESULT_OK) {
            if(imageUri==null)
            {
                Toast.makeText(this, "Error taking photo.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(imageUri);
                BitmapFactory bitmapFactory = new BitmapFactory();
                Bitmap bm = bitmapFactory.decodeStream(inputStream);
                aImg.setImageBitmap(bm);
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
            //uploadImage();
            return;
        }
        if(requestCode==OPEN_FILE && resultCode==RESULT_OK) {
            imageUri = data.getData();
            File fileLocation = new File(String.valueOf(imageUri)); //file path, which can be String, or Uri
            //Picasso.get().load(fileLocation).into(aImg);
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(imageUri);
                BitmapFactory bitmapFactory = new BitmapFactory();
                Bitmap bm = bitmapFactory.decodeStream(inputStream);
                aImg.setImageBitmap(bm);
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }

            //uploadImage();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        et_email = (EditText) findViewById(R.id.add_email);
        et_password = (EditText) findViewById(R.id.add_password);
        et_repeat_password = (EditText) findViewById(R.id.repeat_password);
        et_fn = (EditText) findViewById(R.id.add_first_name);
        et_ln = (EditText) findViewById(R.id.add_last_name);
        et_company = (EditText)findViewById(R.id.add_company);
        et_phonenumber = (EditText) findViewById(R.id.add_phone);
        aImg = (ImageView) findViewById(R.id.choose_photo);
        fab_save = (FloatingActionButton) findViewById(R.id.fab);

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

                ValidateChange(et_email.getText().toString(),
                        et_password.getText().toString());
            }
        };


        et_password.addTextChangedListener(afterTextChangedListener);

    }

//    private void updateUI(){
//        finish();
//    }

    private void saveUserDataToDB() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("Users");
        String fullname = et_fn.getText().toString() + " "+ et_ln.getText().toString();
        final User upload_user = new User();
        upload_user.email = et_email.getText().toString();
        upload_user.firstname = et_fn.getText().toString();
        upload_user.lastname = et_ln.getText().toString();
        upload_user.displayname = fullname;
        upload_user.company = et_company.getText().toString();
        upload_user.phone = et_phonenumber.getText().toString();
        if(imageUploaded == false) {
            Log.d("imageUri", "Equals null");
            upload_user.profilepic = "default_pic.jpg";
            usersRef.child(currentUser.getUid()).setValue(new User(et_email.getText().toString(),
                    et_fn.getText().toString(),
                    et_ln.getText().toString(),
                    fullname,
                    et_company.getText().toString(),
                    et_phonenumber.getText().toString(),
                    "default_pic",
                    "default_card"));
        }
        else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final String fileNameInStorage = UUID.randomUUID().toString();
            String path = "Profile_Pictures/" + fileNameInStorage + ".jpg";
            final StorageReference imageRef = storage.getReference(path);
            Log.d("path", path);
            Log.d("imageeUri", imageUri.toString());
            UploadTask uploadTask = imageRef.putFile(imageUri);
            upload_user.profilepic = fileNameInStorage + ".jpg";
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        imageUploaded = true;
                        Uri downloadUri = task.getResult();
                        upload_user.profilepic = fileNameInStorage + ".jpg";
                        Log.d("path url version: ", fileNameInStorage + ".jpg");
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference mRootReference = database.getReference("Users");
                        String fullname = et_fn.getText().toString() + " "+ et_ln.getText().toString();
                        mRootReference.child(currentUser.getUid()).setValue(new User(et_email.getText().toString(),
                                et_fn.getText().toString(),
                                et_ln.getText().toString(),
                                fullname,
                                et_company.getText().toString(),
                                et_phonenumber.getText().toString(),
                                fileNameInStorage,
                                "default_card"));
                        finish();


                    } else {
                        Toast.makeText(RegisterUser.this, "issue", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        } //end else

    }

    public void Signup(View view) {

        if (et_email.getText().toString().equals("") || et_password.getText().toString().equals("")
                || et_fn.getText().toString().equals("") || et_ln.getText().toString().equals("")) {
            Toast.makeText(this, "Please provide all required information", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString())
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
            fab_save.setClickable(true);
            return true;
        }
        else
        {
            if(!isUserNameValid(aUsername))
            {
                et_email.setError("Must be valid email address");
            }
            if(!isPasswordValid(aPassword))
            {
                et_password.setError("Password must be longer than 5 characters");
            }
            return false;
        }
    }

    public void sendEmailVerification(View view) {
        if(mAuth.getCurrentUser()==null){
            Toast.makeText(this, "Please login first to resend verification email.", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUser.sendEmailVerification()
                .addOnSuccessListener(RegisterUser.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterUser.this, "Verification email Sent!", Toast.LENGTH_SHORT).show();
                        //updateUI();
                    }
                }).addOnFailureListener(RegisterUser.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterUser.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isPasswordValid(String password) {
        Log.d("isPasswordValid", "verify: " + et_repeat_password.getText().toString());
        return password != null && password.trim().length() > 5;
    }

    private boolean isUserNameValid(String username) {
            return username != null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED && requestCode==REQUEST_FOR_CAMERA )
        {
            if(ContextCompat.checkSelfPermission(getBaseContext(),
                    android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
            {
                takePhoto();
            }
        }
        else{
            Toast.makeText(this, "We need to access your camera and photos to upload.", Toast.LENGTH_LONG).show();

        }

    }

    public void ChoosePhoto(View view) {
        imageUploaded = true;
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    private void checkPermissions(){

        if (ContextCompat.checkSelfPermission(getBaseContext(),
                android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "We need permission to access your camera and photo.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_FOR_CAMERA);
        }
        else
        {
            takePhoto();
        }
    }

    private void takePhoto(){
        imageUploaded = true;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        Intent chooser=Intent.createChooser(intent,"Select a Camera App.");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, REQUEST_FOR_CAMERA);}
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.takephoto:
                checkPermissions();
                return true;
            case R.id.upload:
                Intent intent = new Intent().setType("*/*") //when un commented the argument here shall be "start/star"
                        .setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select a file"), OPEN_FILE);
                return true;
            default:
                return false;
        }
    }
}