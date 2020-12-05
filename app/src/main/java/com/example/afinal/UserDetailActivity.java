package com.example.afinal;

import android.content.Intent;
import android.graphics.BitmapFactory;
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

public class UserDetailActivity extends AppCompatActivity {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.user_detail_frag);
            Intent intent = getIntent();
            String card = intent.getStringExtra("card");
            String displayname = intent.getStringExtra("displayname");
            String email = intent.getStringExtra("email");
            String phone = intent.getStringExtra("phone");
            String profilepic = intent.getStringExtra("profilepic");

            ImageView cardView=findViewById(R.id.large_business_card);
            Picasso.get().load(card).into(cardView);

            TextView titleText=findViewById(R.id.large_name);
            titleText.setText(displayname);

            TextView yearText=findViewById(R.id.large_email);
            yearText.setText(email);

            TextView descText=findViewById(R.id.large_phone);
            descText.setText(phone);

            ImageView proPicView=findViewById(R.id.large_prof_pic);
            Picasso.get().load(profilepic).into(proPicView);
        }

    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.mainFragContainer));
        finish();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
