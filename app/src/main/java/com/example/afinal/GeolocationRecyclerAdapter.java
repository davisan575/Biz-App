package com.example.afinal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GeolocationRecyclerAdapter
        extends RecyclerView.Adapter<GeolocationRecyclerAdapter.ViewHolder>{
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference allPostsRef = database.getReference("Users");
    ChildEventListener usersRefListener;
    private List<String> keyList;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private List<User> usersList;
    private List<User> users_filtered;
    private Uri profileImageUri=null;
    private HashMap<String,User> key_to_User;
    private Context c;
    private Marker currentMarker =null;
    private  ItemClickListener itemClickListener;
//    private List<User> usersList;
//    private List<User> users_filtered;
    private ArrayList<String> friendsList;
    private RecyclerView r;
    private onListItemClickListener onListItemClickListener=null;
    DatabaseReference friendsNode = allPostsRef.child(currentUser.getUid()).child("friends");


    public GeolocationRecyclerAdapter(HashMap<String,User> kp, List<String> kl, ItemClickListener _itemClickListener, Context c) {
        keyList = kl;
        key_to_User = kp;
        c = this.c;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        itemClickListener = _itemClickListener;
    }

    @NonNull
    @Override
    public GeolocationRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent,false);
        final GeolocationRecyclerAdapter.ViewHolder vh = new GeolocationRecyclerAdapter.ViewHolder(v);
        return vh;
    }

//    static class User{
//        public String postKey_name;
//        public String displayname;
//        public String profilepic;
//        public String card;
//        public String email;
//        public String phone;
//        public Marker marker;
//        public User(String postKey_name, String display_name, String email, String phone, String profilepic, String card) {
//            this.postKey_name=postKey_name;
//            this.displayname=display_name;
//            this.email=email;
//            this.phone=phone;
//            this.profilepic=profilepic;
//            this.card=card;
//            this.marker = null;
//        }
//
//        public String getPostKey_name() {return postKey_name;}
//    }

    @Override
    public void onBindViewHolder(@NonNull final GeolocationRecyclerAdapter.ViewHolder holder, final int position) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        Log.d("onBindViewHolder", "Running on bind view holder");
        String userKey = keyList.get(position);
        final User u = key_to_User.get(keyList.get(position));
        String card_name = u.card;
        holder.name_v.setText(u.displayname);
        final StorageReference cardRef = storage.getReference("Business_Cards").child(card_name + ".jpg");
        cardRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri URI) {
                Picasso.get().load(URI.toString()).into(holder.business_v);
//                    Toast.makeText(mCtx, "SUCCESS", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
//                    Log.d("ERROR: ", exception.getMessage());
                Toast.makeText(c, "FAIL", Toast.LENGTH_SHORT).show();
            }
        });

        ////NOTE I EDITED THIS!
        if (!keyList.contains(userKey)) {
            Log.d("****NOT IN FRIENDS LIST", userKey);
            if (userKey.equals(currentUser.getUid())) {
                holder.add_friend_v.setVisibility(View.INVISIBLE);
            } else {
                holder.add_friend_v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference currUser = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
                        DatabaseReference friendsPath = currUser.child("friends");
                        friendsPath.child(keyList.get(position)).setValue(true);
                        holder.add_friend_v.setVisibility(View.INVISIBLE);
                        Toast.makeText(c, u.displayname + " has been added to your friends", Toast.LENGTH_LONG).show();
                    }
                });
            }

            if (holder.uref != null && holder.urefListener != null) {
                holder.uref.removeEventListener(holder.urefListener);
            }

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            holder.uref = database.getReference("Users");
            holder.uref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
//                holder.fname_v.setText("First Name: " +dataSnapshot.child("displayname").getValue().toString());
//                holder.email_v.setText("Email:  " + dataSnapshot.child("email").getValue().toString());
//                holder.phone_v.setText("Phone Num:  " + dataSnapshot.child("phone").getValue().toString());
//                holder.date_v.setText("Date Created: "+u.date);
//                if(dataSnapshot.child("profilePicture").exists())
//                {
//                    Picasso.get().load(dataSnapshot.child("profilePicture").getValue().toString()).transform(new CircleTransform()).into(holder.fab);
//                }
//                else
//                {
//                    holder.fab.setImageDrawable( ContextCompat.getDrawable(holder.fab.getContext(), R.drawable.lab_logo));
//
//                }                holder.fname_v.setText("First Name: " +dataSnapshot.child("displayname").getValue().toString());
//                holder.email_v.setText("Email:  " + dataSnapshot.child("email").getValue().toString());
//                holder.phone_v.setText("Phone Num:  " + dataSnapshot.child("phone").getValue().toString());
//                holder.date_v.setText("Date Created: "+u.date);
//                if(dataSnapshot.child("profilePicture").exists())
//                {
//                    Picasso.get().load(dataSnapshot.child("profilePicture").getValue().toString()).transform(new CircleTransform()).into(holder.fab);
//                }
//                else
//                {
//                    holder.fab.setImageDrawable( ContextCompat.getDrawable(holder.fab.getContext(), R.drawable.lab_logo));
//
//                }
//                holder.fab.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        // DatabaseReference dbr = database.getReference("Posts").child(u.postKey).child("uid");
//                        String postOwnerId = u.uid;
//                        String currentUserId = currentUser.getUid();
//                        String email = currentUser.getEmail();
//                        Log.d("onBindViewHolder", "postowner: " + postOwnerId + ", userid: " + currentUserId);
//                        Intent intent = new Intent(holder.fab.getContext(), MessengerActivity.class);
//                        intent.putExtra("CURRENT_USER_ID", currentUserId);
//                        intent.putExtra("POST_USER_ID", postOwnerId);
//                        intent.putExtra("POST_KEY", u.postKey);
//                        String substr = holder.fname_v.getText().toString().substring(12);
//                        intent.putExtra("USER_NAME",email);
//                        //intent.putExtra("URI", currentUser.getPhotoUrl().toString());
//                        holder.fab.getContext().startActivity(intent);
//                        //Toast.makeText(c, postOwnerId + " and " + currentUserId , Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

//        holder.likeCountRef=
//                database.getReference("Posts/"+u.postKey+"/likeCount");
//        Log.d("LIKEC ", u.postKey);
//        holder.likeCountRefListener=holder.likeCountRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                //Log.d("CRASH", dataSnapshot.toString());
//                if(dataSnapshot.getValue()!=null)
//                    holder.likeCount.setText(dataSnapshot.getValue().toString()+" Likes");
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//        holder.likesRef=database.getReference("Posts/"+u.postKey+"/likes/"+currentUser.getUid());
//        holder.likesRefListener=holder.likesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists() && dataSnapshot.getValue().toString().equals("true"))
//                {
//                    holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(holder.likeBtn.getContext(), R.drawable.like_active));
//                }
//                else{
//                    holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(holder.likeBtn.getContext(), R.drawable.like_disabled));
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                database.getReference("Posts/"+u.postKey).runTransaction(new Transaction.Handler() {
//                    @NonNull
//                    @Override
//                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
//                        PhotoPreview.Post p = mutableData.getValue(PhotoPreview.Post.class);
//                        if (p == null) {
//                            return Transaction.success(mutableData);
//                        }
//
//                        if (p.likes.containsKey(currentUser.getUid())) {
//                            // Unstar the post and remove self from stars
//                            p.likeCount = p.likeCount - 1;
//                            p.likes.remove(currentUser.getUid());
//                        } else {
//                            // Star the post and add self to stars
//                            p.likeCount = p.likeCount + 1;
//                            p.likes.put(currentUser.getUid(), true);
//                        }
//
//                        // Set value and report transaction success
//                        mutableData.setValue(p);
//                        return Transaction.success(mutableData);
//                    }

//                    @Override
//                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
//
//                    }
//                });
//            }
//        });
//        holder.imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (currentMarker!=null)
//                    currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_grey));
//
//                u.m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));
//                currentMarker=u.m;
//                if (itemClickListener!=null)
//                    itemClickListener.onItmeClick(currentMarker.getPosition());
//            }
//        });
//        holder.description_v.setText(u.description);
//        StorageReference pathReference = FirebaseStorage.getInstance().getReference("images/"+u.url);
//        //StorageReference profReference = FirebaseStorage.getInstance().getReference()("images/")
//        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Picasso.get().load(uri).into(holder.imageView);
//            }
//        });

    }



    @Override
    public int getItemCount() {
        return keyList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView business_v;
        public ImageView add_friend_v;
        public TextView name_v;
        DatabaseReference uref;
        ValueEventListener urefListener;

        public ViewHolder(View v){
            super(v);
            business_v = v.findViewById(R.id.card_business);
            add_friend_v=v.findViewById(R.id.card_add_friend);
            name_v=v.findViewById(R.id.card_name);
        }
    }
}