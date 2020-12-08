package com.example.afinal;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

public class FriendsDetailActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_detail_frag);
        Intent intent = getIntent();
        String card = intent.getStringExtra("card");
        final String displayname = intent.getStringExtra("displayname");
        final String email = intent.getStringExtra("email");
        final String phone = intent.getStringExtra("phone");
        String profilepic = intent.getStringExtra("profilepic");
        String hobbies = intent.getStringExtra("hobbies");
        String education = intent.getStringExtra("education");
        final String employment = intent.getStringExtra("employment");
        String company = intent.getStringExtra("company");

        ImageView cardView=findViewById(R.id.friend_card);
        Picasso.get().load(card).into(cardView);

        TextView titleText=findViewById(R.id.friend_name);
        titleText.setText(displayname);

        TextView yearText=findViewById(R.id.friend_email);
        yearText.setText(email);

        TextView descText=findViewById(R.id.friend_phone);
        descText.setText(phone);

        TextView companyText=findViewById(R.id.friend_company);
        companyText.setText(company);

        TextView eduText=findViewById(R.id.friend_edu);
        eduText.setText(education);

        TextView employText=findViewById(R.id.friend_employment);
        employText.setText(employment);

        TextView hobbyText=findViewById(R.id.friend_hobby);
        hobbyText.setText(hobbies);

        ImageView proPicView=findViewById(R.id.friend_pic);
        Picasso.get().load(profilepic).into(proPicView);

        ImageView share = findViewById(R.id.friend_share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                sb.append("Hello! Check out this awesome connection.");
                sb.append('\n');
                sb.append('\n');
                sb.append("You can find their contact information below:");
                sb.append('\n');
                sb.append("Name : ");
                sb.append(displayname);
                sb.append('\n');
                sb.append("Email : ");
                sb.append(email);
                sb.append('\n');
                sb.append("Phone : ");
                sb.append(phone);
                sb.append('\n');
                sb.append("Current Employment : ");
                sb.append(employment);
                sb.append('\n');
                sb.append('\n');
                sb.append("Download Biz-App to find more connections!");

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                intent.setType("text/plain");
                Intent chooser = Intent.createChooser(intent, "Share via");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
            }
        });
    }
}
