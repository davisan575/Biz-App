package com.example.afinal;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import java.net.URI;

public class AddFriends_RecyclerAdapter extends RecyclerView.Adapter<AddFriends_RecyclerAdapter.ViewHolder>
        implements Filterable
{
    public void getFilterType(String type) {
        filterType = type;
        Log.d("*************TYPE: ", filterType);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if(filterType.equals("Email")) {
                    if (charString.isEmpty()) {
                        users_filtered = usersList;
                    }
                    else {
                        List<User> filteredList = new ArrayList<>();
                        for (User u : users_filtered) {
                            if (u.email.contains(charString)) {
                                filteredList.add((u));
                            }
                        }
                        users_filtered = filteredList;
                    }
                }
                else if (filterType.equals("Phone")) {
                    if (charString.isEmpty()) {
                        users_filtered = usersList;
                    }
                    else {
                        List<User> filteredList = new ArrayList<>();
                        for (User u : users_filtered) {
                            if (u.phone.contains(charString)) {
                                filteredList.add((u));
                            }
                        }
                        users_filtered = filteredList;
                    }
                }
                else {
                    if (charString.isEmpty()) {
                        users_filtered = usersList;
                    }
                    else {
                        List<User> filteredList = new ArrayList<>();
                        for (User u : users_filtered) {
                            if (u.displayname.contains(charString)) {
                                filteredList.add((u));
                            }
                        }
                        users_filtered = filteredList;
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = users_filtered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//                List<User> filteredList = new ArrayList<>();
//
//                if(filterType.equals("Email")) {
//                    for (User u : users_filtered) {
//                        if (u.email.contains(charSequence.toString())) {
//                            filteredList.add((u));
//                        }
//                    }
//                }
//                else if (filterType.equals("Phone")) {
//                    for (User u : users_filtered) {
//                        if (u.phone.contains(charSequence.toString())) {
//                            filteredList.add((u));
//                        }
//                    }
//                }
//                else {
//                    for (User u : users_filtered) {
//                        if (u.displayname.contains(charSequence.toString())) {
//                            filteredList.add((u));
//                        }
//                    }
//                }
//                users_filtered = filteredList;
                users_filtered = (List<User>) filterResults.values;
                notifyDataSetChanged();;
            }
        };
    }

    static class User{
        public String postKey_name;
        public String displayname;
        public String profilepic;
        public String card;
        public String email;
        public String phone;
        public User(String postKey_name, String display_name, String email, String phone, String profilepic, String card) {
            this.postKey_name=postKey_name;
            this.displayname=display_name;
            this.email=email;
            this.phone=phone;
            this.profilepic=profilepic;
            this.card=card;
        }

        public String getPostKey_name() {return postKey_name;}
    }

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference allPostsRef = database.getReference("Users");
    ChildEventListener usersRefListener;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private List<User> usersList;
    private List<User> users_filtered;
    private Context mCtx;
    private RecyclerView r;
    private onListItemClickListener onListItemClickListener=null;
    private String filterType;

    public AddFriends_RecyclerAdapter(RecyclerView recyclerView, Context context){
        usersList =new ArrayList<>();
        users_filtered =new ArrayList<>();
        r=recyclerView;
        mCtx= context;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        allPostsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                User newUser=new User(dataSnapshot.getKey(),
                        dataSnapshot.child("displayname").getValue().toString(),
                        dataSnapshot.child("email").getValue().toString(),
                        dataSnapshot.child("phone").getValue().toString(),
                        dataSnapshot.child("profilepic").getValue().toString(),
                        dataSnapshot.child("card").getValue().toString());
                Log.d("********SNAPSHOT NAME ", dataSnapshot.child("displayname").getValue().toString());
                Log.d("*******POSTKEY NAME ", dataSnapshot.getKey());
                usersList.add(newUser);
                users_filtered.add(newUser);
//                MyRecyclerAdapter.this.notifyItemInserted(moviesList.size()-1);
                AddFriends_RecyclerAdapter.this.notifyItemInserted(users_filtered.size()-1);
                r.scrollToPosition(users_filtered.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for(int i=0; i < getFullItemCount(); i++) {
                    if (usersList.get(i).getPostKey_name().equals(snapshot.getKey())) {
                        usersList.remove(i);
                        AddFriends_RecyclerAdapter.this.notifyItemRemoved(i);
                        AddFriends_RecyclerAdapter.this.notifyItemRangeChanged(i, getFullItemCount());
                        AddFriends_RecyclerAdapter.this.notifyDataSetChanged();
                    }
                }
                for(int i=0; i < getItemCount(); i++) {
                    if (users_filtered.get(i).getPostKey_name().equals(snapshot.getKey())) {
                        users_filtered.remove(i);
                        AddFriends_RecyclerAdapter.this.notifyItemRemoved(i);
                        AddFriends_RecyclerAdapter.this.notifyItemRangeChanged(i, getItemCount());
                        AddFriends_RecyclerAdapter.this.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setOnListItemClickListener(onListItemClickListener listener) {
        onListItemClickListener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent,false);
        final ViewHolder vh = new ViewHolder(v);
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final User u = users_filtered.get(position);

        if (holder.uref != null && holder.urefListener != null) {
            holder.uref.removeEventListener(holder.urefListener);
        }

        if (u.postKey_name.equals(currentUser.getUid())) {
            holder.add_friend_v.setVisibility(View.INVISIBLE);
        }
        else {
            holder.add_friend_v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference currUser = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
                    DatabaseReference friendsPath = currUser.child("friends");
                    friendsPath.child(users_filtered.get(position).postKey_name).setValue(true);
                    holder.add_friend_v.setVisibility(View.INVISIBLE);
                    Toast.makeText(mCtx, u.displayname + " has been added to your friends", Toast.LENGTH_LONG).show();
                }
            });
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String postKey_name = u.postKey_name;
        holder.uref = database.getReference("Users").child(postKey_name);
        holder.uref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.exists()) {
                    String card_name = u.card;
                        final StorageReference cardRef = storage.getReference("Busniess_Cards").child(card_name + ".jpg");
                        cardRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri URI) {
                                Picasso.get().load(URI.toString()).into(holder.business_v);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d("ERROR: ", exception.getMessage());
                            }
                        });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void removeListener(){
        if(allPostsRef !=null && usersRefListener!=null)
            allPostsRef.removeEventListener(usersRefListener);
    }

    @Override
    public int getItemCount() {
        return users_filtered.size();
    }

    public int getFullItemCount() {
        return usersList.size();
    }

    public User getItem(int i) {
        return users_filtered.get(i);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView business_v;
        public ImageView add_friend_v;
        DatabaseReference uref;
        ValueEventListener urefListener;

        public ViewHolder(View v){
            super(v);
            business_v = v.findViewById(R.id.card_business);
            add_friend_v=v.findViewById(R.id.card_add_friend);
        }
    }
}



