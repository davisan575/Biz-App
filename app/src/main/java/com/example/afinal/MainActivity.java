package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar myToolBar;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    public static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);
        myToolBar=findViewById(R.id.top_toolbar);

        setSupportActionBar(myToolBar);
        ActionBar ab=getSupportActionBar();
        ab.setTitle("Home");

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, myToolBar,
                R.string.openD, R.string.closeD) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                Toast toast=Toast.makeText(MainActivity.this,"NavigationDrawer Closed",Toast.LENGTH_SHORT);
//                toast.show();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerClosed(drawerView);
//                Toast toast=Toast.makeText(MainActivity.this,"NavigationDrawer Opened",Toast.LENGTH_SHORT);
//                toast.show();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_personal_details, null, false);
        FrameLayout frame = (FrameLayout) findViewById(R.id.main_container);
        frame.removeAllViews();
        frame.addView(contentView);
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
                        intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT,"Hello World!");
                        Intent chooser = Intent.createChooser(intent, "Share via");
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(chooser);
                        }
                        Log.d("bottom bar", "share action");
                        return true;
                    case R.id.add_friend_action:
                        intent = new Intent(MainActivity.this, AddNewFriends.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        view=findViewById(R.id.add_friend_action);
                        optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, view, "add_new_friends_activity_transition");
                        startActivity(intent, optionsCompat.toBundle());
                        Log.d("bottom bar", "add new friends");
                        return true;
                    case R.id.friends_list_action:
                        intent = new Intent(MainActivity.this, FriendsActivity.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        view=findViewById(R.id.friends_list_action);
                        optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, view, "friends_activity_transition");
                        startActivity(intent, optionsCompat.toBundle());
                        Log.d("bottom bar", "friends");
                        return true;
                    case R.id.edit_profile_action:
                        intent = new Intent(MainActivity.this, EditProfileActivity.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        view=findViewById(R.id.edit_profile_action);
                        optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, view, "edit_profile_activity_transition");
                        startActivity(intent, optionsCompat.toBundle());
                        Log.d("bottom bar", "edit profile");
                        return true;
                    default:
                        return true;
                }
            }
        });
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Are you sure you want to log out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "User wants to log out", Toast.LENGTH_SHORT).show();
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
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View contentView = inflater.inflate(R.layout.activity_personal_details, null, false);
                FrameLayout frame = (FrameLayout) findViewById(R.id.main_container);
                frame.removeAllViews();
                frame.addView(contentView);
                ActionBar ab = getSupportActionBar();
                ab.setTitle("Profile");
                Toast toast = Toast.makeText(this, "Item 1 Clicked", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case R.id.item2:
                Toast toast2 = Toast.makeText(this, "Load edit profile pop up", Toast.LENGTH_SHORT);
                toast2.show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void LoadNewProfilePhoto(View v)
    {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView iv = findViewById(R.id.small_rory);
        ImageView circularIv = findViewById(R.id.circular_img);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
                BitmapFactory bitmapFactory = new BitmapFactory();
                Bitmap bm = bitmapFactory.decodeStream(inputStream);
                iv.setImageBitmap(bm);
                circularIv.setImageBitmap(bm);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}