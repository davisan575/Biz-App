package com.example.afinal;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //Attributes for User Profile
    ImageView mImg;
    ImageView mCardImg;
    TextView tv_name;
    TextView tv_email;
    TextView tv_company;
    TextView tv_education;
    TextView tv_employment;
    TextView tv_hobbies;

    LinearLayout ll_education;
    LinearLayout ll_employment;
    LinearLayout ll_hobbies;
    View v;

    private static final int REQUEST_FOR_CAMERA=0011;
    private static final int OPEN_FILE=0012;
    private Uri imageUri=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Bundle args = getArguments();
        //Inflate the layout for this fragment
        v = inflater.inflate(R.layout.activity_personal_details, container, false);

        mImg = (ImageView) v.findViewById(R.id.profilepic);
        mCardImg = (ImageView) v.findViewById(R.id.profile_business_card);
        tv_name = (TextView) v.findViewById(R.id.name);
        tv_email = (TextView) v.findViewById(R.id.email);
        tv_company = (TextView) v.findViewById(R.id.company);
        tv_education = (TextView) v.findViewById(R.id.user_education);
        tv_employment = (TextView) v.findViewById(R.id.user_employment_details);
        tv_hobbies = (TextView) v.findViewById(R.id.user_hobbies);

        ll_education = (LinearLayout) v.findViewById(R.id.education);
        ll_employment = (LinearLayout) v.findViewById(R.id.employment);
        ll_hobbies = (LinearLayout) v.findViewById(R.id.hobbies);

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users/"+currentUser.getUid());

// Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                tv_name.setText(u.displayname);
                Log.d("onDataChange", u.displayname);
                tv_email.setText(u.email);
                tv_company.setText(u.company);
                if(u.education != null && u.education != "")
                {
                    ll_education.setVisibility(View.VISIBLE);
                    tv_education.setText(u.education);
                    Log.d("Education: ",u.education);
                }
                if(u.employment != null && u.employment!= "")
                {
                    ll_employment.setVisibility(View.VISIBLE);
                    tv_employment.setText(u.employment);
                }
                if(u.hobbies != null && u.hobbies != "")
                {
                    ll_hobbies.setVisibility(View.VISIBLE);
                    tv_hobbies.setText(u.hobbies);
                }

                if(tv_education.getText().toString().equals(""))
                {
                    ll_education.setVisibility(View.GONE);
                }
                if(tv_employment.getText().toString().equals(""))
                {
                    ll_employment.setVisibility(View.GONE);
                }
                if(tv_hobbies.getText().toString().equals(""))
                {
                    ll_hobbies.setVisibility(View.GONE);
                }

                Log.d("profile pic: ", u.profilepic);
                Log.d("card pic: ", u.card);

                StorageReference profilePathReference = FirebaseStorage.getInstance().getReference("Profile_Pictures/"+u.profilepic+".jpg");
                //StorageReference profReference = FirebaseStorage.getInstance().getReference()("images/")
                profilePathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(mImg);
                    }
                });
                StorageReference cardPathReference = FirebaseStorage.getInstance().getReference("Business_Cards/"+u.card+".jpg");
                cardPathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(mCardImg);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        return v;
    }

    @Override
    public void onAttach (Context context) {
        Log.d("onAttach", "Running onAttach Fragment");
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        Log.d("OnDetach", "Running on detach");
        super.onDetach();
        v = null;
    }

}
