package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.MenuItemCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_activity_layout);

        Toolbar myToolBar=findViewById(R.id.friends_search);
        setSupportActionBar(myToolBar);
        ActionBar ab=getSupportActionBar();
        ab.setTitle("");
//        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //set_bottom_bar();
        getMenuInflater().inflate(R.menu.friends_search_bar_menu, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.search_action);
        SearchView searchView=(SearchView)myActionMenuItem.getActionView();
        searchView.setQueryHint("Search friends");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast toast=Toast.makeText(getApplicationContext(),"Query Text="+query,Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                Toast toast=Toast.makeText(getApplicationContext(),newText,Toast.LENGTH_SHORT);
                toast.show();
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        MenuItem shareItem = menu.findItem(R.id.share_action);
//        ShareActionProvider myShareActionProvider =(ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
//        myShareActionProvider.setShareIntent(chooser);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.settings_action:
//                /*Intent intent=new Intent(this,ViewPagerActivity.class);
//                startActivity(intent);*/
//                Toast.makeText(this, "SEttings", Toast.LENGTH_SHORT).show();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}