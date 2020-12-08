package com.example.afinal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Friends_RecyclerAdapter my_Friends_RecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_activity_layout);

        Toolbar myToolBar = findViewById(R.id.friends_search);
        setSupportActionBar(myToolBar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("");
        //        ab.setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        RecyclerView recyclerView=findViewById(R.id.friends_recylcer_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mLayoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(mLayoutManager);

        my_Friends_RecyclerAdapter = new Friends_RecyclerAdapter(recyclerView, getApplicationContext());
        recyclerView.setAdapter(my_Friends_RecyclerAdapter);

        my_Friends_RecyclerAdapter.setOnListItemClickListener(new onListItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                String currUser = my_Friends_RecyclerAdapter.getItem(position).getPostKey_name();
                final DatabaseReference userRef= db.getReference("Users").child(currUser);

                final FirebaseStorage storage = FirebaseStorage.getInstance();
                final Friends_RecyclerAdapter.User u =my_Friends_RecyclerAdapter.getItem(position);

                String busCard=u.card;
                final StorageReference cardRef = storage.getReference("Business_Cards").child(busCard + ".jpg");

                String proPicRef=u.profilepic;
                final StorageReference picRef = storage.getReference("Profile_Pictures").child(proPicRef + ".jpg");


                cardRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri cardURI) {

                        picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri picURI) {
                                Intent intent = new Intent(getApplicationContext(), FriendsDetailActivity.class);
                                intent.putExtra("card", cardURI.toString());
                                intent.putExtra("profilepic", picURI.toString());
                                intent.putExtra("phone", u.phone);
                                intent.putExtra("displayname", u.displayname);
                                intent.putExtra("education", u.education);
                                intent.putExtra("email", u.email);
                                intent.putExtra("hobbies", u.hobbies);
                                intent.putExtra("education", u.education);
                                intent.putExtra("employment", u.employment);
                                intent.putExtra("company", u.company);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage() + " (profile_pic)", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage() + " (business card)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //set_bottom_bar();
        getMenuInflater().inflate(R.menu.friends_search_bar_menu, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.search_action);
        SearchView searchView=(SearchView)myActionMenuItem.getActionView();
        searchView.setQueryHint("Search new connections");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                Toast toast=Toast.makeText(getApplicationContext(),"Query Text="+query,Toast.LENGTH_SHORT);
//                toast.show();
                my_Friends_RecyclerAdapter.getFilter().filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
//                Toast toast=Toast.makeText(getApplicationContext(),newText,Toast.LENGTH_SHORT);
//                toast.show();
                my_Friends_RecyclerAdapter.getFilter().filter(newText);
                return true;
            }
        });

        MenuItem item = menu.findItem(R.id.search_spinner);
        Spinner spinner = (Spinner) item.getActionView();
//        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(this, R.array.filter_options,
//                android.R.layout.simple_spinner_dropdown_item);

        List<String> list = new ArrayList<String>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_layout, list);

        list.add("Name");
        list.add("Phone");
        list.add("Email");

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(FriendsActivity.this, "Search filter selected: "
                        + parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                my_Friends_RecyclerAdapter.getFilterType(parent.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        MenuItem shareItem = menu.findItem(R.id.share_action);
//        ShareActionProvider myShareActionProvider =(ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
//        myShareActionProvider.setShareIntent(chooser);
        return super.onCreateOptionsMenu(menu);
    }
}
