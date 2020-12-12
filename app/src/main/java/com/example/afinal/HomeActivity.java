package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
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

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar myToolBar;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    ImageView circImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);

        myToolBar=findViewById(R.id.top_toolbar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        setSupportActionBar(myToolBar);
        ActionBar ab=getSupportActionBar();
        ab.setTitle("Home");

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, myToolBar,
                R.string.openD, R.string.closeD) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        FrameLayout frame = (FrameLayout) findViewById(R.id.main_container);
        Bundle args=new Bundle();
        Fragment detailFragment=new ProfileFragment();
        detailFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container,detailFragment).addToBackStack(null).commit();

        LoadUserData();
//        ab.setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomToolbar= findViewById(R.id.bottom_toolbar);
        bottomToolbar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                View view;
                ActivityOptionsCompat optionsCompat;
                switch (item.getItemId()) {
                    case R.id.share_action:
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference ref = database.getReference("Users/"+currentUser.getUid());
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User u = snapshot.getValue(User.class);
//                                StringBuilder sb = new StringBuilder();
//                                sb.append("Hello! It was lovely connecting with you today.");
//                                sb.append('\n');
//                                sb.append('\n');
//                                sb.append("You can find my contact information below:");
//                                sb.append('\n');
//                                sb.append("Name : ");
//                                sb.append(u.displayname);
//                                sb.append('\n');
//                                sb.append("Email : ");
//                                sb.append(u.email);
//                                sb.append('\n');
//                                sb.append("Phone : ");
//                                sb.append(u.phone);
//                                sb.append('\n');
//                                sb.append("Current Employment : ");
//                                sb.append(u.employment);
//                                sb.append('\n');
//                                sb.append('\n');
//                                sb.append("Download Biz-App to find more connections!");
//
//                                Intent intent = new Intent(Intent.ACTION_SEND);
//                                intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
//                                intent.setType("text/plain");
//                                Intent chooser = Intent.createChooser(intent, "Share via");
//                                if (intent.resolveActivity(getPackageManager()) != null) {
//                                    startActivity(chooser);
//                                }
                                String cardName = u.card;
                                Log.d("card name", u.card);
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference cardRef = storage.getReference("Business_Cards").child(cardName + ".jpg");
                                cardRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri picURI) {
                                        Intent intent = new Intent(Intent.ACTION_SEND);
//                                        intent.putExtra(Intent.EXTRA_TEXT, "Hello! It was nice to connect with you. " +
//                                                "Feel free to contact me via my information below.");
//                                        intent.setType("text/plain");
                                        intent.putExtra(Intent.EXTRA_STREAM, picURI);
                                        intent.setType("image/jpeg");
//                                        intent.putExtra(Intent.EXTRA_TEXT, "Download Biz-App to find more connections!");
//                                        intent.setType("text/plain");
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                        startActivity(intent);
                                        Intent chooser = Intent.createChooser(intent, "Share via");
                                        if (intent.resolveActivity(getPackageManager()) != null) {
                                            startActivity(chooser);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        Log.d("bottom bar", "share action");
                        return true;
                    case R.id.add_friend_action:
                        intent = new Intent(HomeActivity.this, AddNewFriends.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        view=findViewById(R.id.add_friend_action);
                        optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeActivity.this, view, "add_new_friends_activity_transition");
                        startActivity(intent, optionsCompat.toBundle());
                        Log.d("bottom bar", "add new friends");
                        return true;
                    case R.id.friends_list_action:
                        intent = new Intent(HomeActivity.this, FriendsActivity.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        view=findViewById(R.id.friends_list_action);
                        optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeActivity.this, view, "friends_activity_transition");
                        startActivity(intent, optionsCompat.toBundle());
                        Log.d("bottom bar", "friends");
                        return true;
                    case R.id.edit_profile_action:
                        intent = new Intent(HomeActivity.this, EditProfileActivity.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        view=findViewById(R.id.edit_profile_action);
                        optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeActivity.this, view, "edit_profile_activity_transition");
                        startActivity(intent, optionsCompat.toBundle());
                        Log.d("bottom bar", "edit profile");
                        return true;
                    default:
                        return true;
                }
            }
        });

//        final View header = (View) findViewById(R.id.header);
//        final ImageView imageViewUser =  (ImageView)header.findViewById(R.id.circular_img);
        View headerView = navigationView.getHeaderView(0);
        final ImageView circularImg =  (ImageView)headerView.findViewById(R.id.circular_img);
        final TextView headerName = (TextView)headerView.findViewById(R.id.name_header);
        final TextView headerEmail = (TextView)headerView.findViewById(R.id.email_header);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users/"+currentUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User u = snapshot.getValue(User.class);
                headerName.setText(u.displayname);
                headerEmail.setText(u.email);
                StorageReference profilePathReference = FirebaseStorage.getInstance().getReference("Profile_Pictures/" + u.profilepic + ".jpg");
                //StorageReference profReference = FirebaseStorage.getInstance().getReference()("images/")
                profilePathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(circularImg);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void LoadUserData() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //set_bottom_bar();
        getMenuInflater().inflate(R.menu.top_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_action:
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Are you sure you want to log out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        finish();
                        Toast.makeText(HomeActivity.this, "Logging out, goodbye!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();                            }
                });
                builder.create().show();
                Log.d("bottom bar", "log out");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.item1:
                FrameLayout frame = (FrameLayout) findViewById(R.id.main_container);
                Bundle args=new Bundle();
                Fragment detailFragment=new ProfileFragment();
                detailFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container,detailFragment).addToBackStack(null).commit();
                ActionBar ab = getSupportActionBar();
                ab.setTitle("Home");
                Toast toast = Toast.makeText(this, "Loading User Profile.", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case R.id.item2:
                Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                View view=findViewById(R.id.edit_profile_action);
                startActivity(intent);
                Log.d("bottom bar", "edit profile");
                break;
            case R.id.geolocation_finder:

                final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

                if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    buildAlertMessageNoGps();
                }

                Bundle geo_args=new Bundle();
                Fragment geoFragment=new GeolocationFragment();
                geoFragment.setArguments(geo_args);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container,geoFragment).addToBackStack(null).commit();
                ActionBar ab_geo = getSupportActionBar();
                ab_geo.setTitle("Geolocation Search");
                Toast.makeText(this, "Loading Geolocation Finder.", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}