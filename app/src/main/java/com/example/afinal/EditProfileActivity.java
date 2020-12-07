package com.example.afinal;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    EditText edit_fn;
    EditText edit_ln;
    EditText edit_company;
    EditText edit_phonenumber;
    EditText edit_education;
    EditText edit_employment;
    EditText edit_hobbies;

    ImageView editProfilePic;
    ImageView editCard;

    public Object saveTimestamp;
    public String saveEmail;
    public String saveProfilepic;
    public String saveCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity_layout);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        edit_fn = (EditText) findViewById(R.id.edit_first_name);
        edit_ln = (EditText) findViewById(R.id.edit_last_name);
        edit_company = (EditText) findViewById(R.id.edit_company);
        edit_phonenumber = (EditText) findViewById(R.id.edit_phone_number);
        edit_education = (EditText) findViewById(R.id.edit_education);
        edit_employment = (EditText) findViewById(R.id.edit_employment);
        edit_hobbies = (EditText) findViewById(R.id.edit_hobbies);

        editCard = (ImageView) findViewById(R.id.edit_business_card);
        editProfilePic = (ImageView) findViewById(R.id.edit_profilepic);

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users/"+currentUser.getUid());

        User read_u = new User();

// Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                edit_fn.setText(u.firstname);
                Log.d("onDataChange", u.displayname);
                edit_ln.setText(u.lastname);
                edit_phonenumber.setText(u.phone);
                edit_company.setText(u.company);
                saveTimestamp = u.timestamp;
                saveEmail = u.email;
                saveCard = u.card;
                saveProfilepic = u.profilepic;
                if(u.education != null)
                {
                    //ll_education.setVisibility(View.VISIBLE);
                    edit_education.setText(u.education);
                }
                if(u.employment != null)
                {
                    //ll_employment.setVisibility(View.VISIBLE);
                    edit_employment.setText(u.employment);
                }
                if(u.hobbies != null)
                {
                    //ll_hobbies.setVisibility(View.VISIBLE);
                    edit_hobbies.setText(u.hobbies);
                }

                StorageReference profilePathReference = FirebaseStorage.getInstance().getReference("Profile_Pictures/"+u.profilepic+".jpg");
                StorageReference cardPathReference = FirebaseStorage.getInstance().getReference("Business_Cards/"+u.card+".jpg");

                profilePathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(editProfilePic);
                    }
                });

                cardPathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(editCard);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    public void SaveChanges(View view) {

        if (edit_fn.getText().toString().equals("") || edit_ln.getText().toString().equals("")
                || edit_phonenumber.getText().toString().equals("") || edit_company.getText().toString().equals("")) {
            Toast.makeText(this, "Please provide all required information", Toast.LENGTH_SHORT).show();
            return;
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users/"+currentUser.getUid());

        User write_user = new User();
        write_user.firstname = edit_fn.getText().toString();
        write_user.lastname = edit_ln.getText().toString();
        write_user.displayname = write_user.firstname + " " + write_user.lastname;
        write_user.company = edit_company.getText().toString();
        write_user.phone = edit_phonenumber.getText().toString();
        write_user.timestamp = saveTimestamp;
        write_user.email = saveEmail;
        write_user.profilepic = saveProfilepic;
        write_user.card = saveCard;
        if(edit_hobbies.getText().toString() != "")
        {
            write_user.hobbies = edit_hobbies.getText().toString();
        }
        if(edit_employment.getText().toString() != "")
        {
            write_user.employment = edit_employment.getText().toString();
        }
        if(edit_education.getText().toString() != "")
        {
            write_user.education = edit_education.getText().toString();
        }
        ref.setValue(write_user);
        finish();

    }

    public void UpdateCard(View view) {

    }
}
