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
    static private List<String> friends;
    private Uri profileImageUri=null;
    private HashMap<String,User> key_to_User;
    private Context c;
    private Marker currentMarker =null;
    private  ItemClickListener itemClickListener;
//    private List<User> usersList;
//    private List<User> users_filtered;
    private RecyclerView r;
    private onListItemClickListener onListItemClickListener=null;
    DatabaseReference friendsNode = allPostsRef.child(currentUser.getUid()).child("friends");


    public GeolocationRecyclerAdapter(HashMap<String,User> kp, List<String> kl, ItemClickListener _itemClickListener, Context ctx) {
        keyList = kl;
        key_to_User = kp;
        friends = new ArrayList<>();
        c = ctx;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        itemClickListener = _itemClickListener;

        FirebaseDatabase.getInstance().getReference().child("Users/"+currentUser.getUid()+"/friends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Log.d("DEBUG", "In loop for key");
                            String friendChild = snapshot.getKey();
                            friends.add(snapshot.getKey());
                            Log.d("friendkey: ", friendChild);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //Toast.makeText(this.getActivity(), "FAIL", Toast.LENGTH_SHORT).show();
                    }
                });
        for( String friend : friends)
        {
            Log.d("Friend Key: ", friend);
        }
        Log.d("size: ", Integer.toString(friends.size())) ;
    }

    public void setOnListItemClickListener(onListItemClickListener listener) {
        onListItemClickListener=listener;
    }

    @NonNull
    @Override
    public GeolocationRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent,false);
        final GeolocationRecyclerAdapter.ViewHolder vh = new GeolocationRecyclerAdapter.ViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onListItemClickListener != null) {
                    onListItemClickListener.onItemClick(v, vh.getAdapterPosition());
                }
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final GeolocationRecyclerAdapter.ViewHolder holder, final int position) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        Log.d("onBindViewHolder", "Running on bind view holder");
        String userKey = keyList.get(position);
        final User u = key_to_User.get(keyList.get(position));
        String card_name = u.card;
        holder.name_v.setText(u.displayname);
        final StorageReference cardRef = storage.getReference("Business_Cards").child(card_name + ".jpg");
        DatabaseReference currUserPath = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
        DatabaseReference friendsPath = currUserPath.child("friends");


        if(!friends.contains(keyList.get(position)))
        {
            holder.add_friend_v.setClickable(true);
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
        if(friends.contains(keyList.get(position)))
        {
            holder.add_friend_v.setClickable(false);
            holder.add_friend_v.setVisibility(View.INVISIBLE);
        }


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