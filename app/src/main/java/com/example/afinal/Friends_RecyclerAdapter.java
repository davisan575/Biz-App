package com.example.afinal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.snapshot.ChildrenNode;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//import java.net.URI;

public class Friends_RecyclerAdapter extends RecyclerView.Adapter<Friends_RecyclerAdapter.ViewHolder>
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
                String charString = charSequence.toString().toLowerCase();
                if(filterType.equals("Email")) {
                    if (charString.isEmpty()) {
                        users_filtered = usersList;
                    }
                    else {
                        List<User> filteredList = new ArrayList<>();
                        for (User u : users_filtered) {
                            if (u.email.toLowerCase().contains(charString)) {
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
                            if (u.displayname.toLowerCase().contains(charString)) {
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

    static class User {
        public String email;
        public String firstname;
        public String lastname;
        public String displayname;
        public String postKey_name;
        public String company;
        public String phone;
        public String profilepic;
        public String card;
        public String education;
        public String employment;
        public String hobbies;
        public Object timestamp;
        public User(String postKey_name, String email, String displayname, String employment, String education,
                    String company, String phone, String profilepic, String card, String hobbies) {
            this.postKey_name=postKey_name;
            this.displayname=displayname;
            this.education=education;
            this.email=email;
            this.phone=phone;
            this.company=company;
            this.employment=employment;
            this.profilepic=profilepic;
            this.card=card;
            this.hobbies=hobbies;
            this.timestamp= ServerValue.TIMESTAMP;
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
    private ArrayList<String> friendsList;

    DatabaseReference friendsNode = allPostsRef.child(currentUser.getUid()).child("friends");

    public Friends_RecyclerAdapter(RecyclerView recyclerView, Context context){
        usersList =new ArrayList<>();
        users_filtered =new ArrayList<>();
        friendsList =new ArrayList<>();
        r=recyclerView;
        mCtx= context;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        friendsNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> i = snapshot.getChildren();

                for (DataSnapshot iter : i) {
                    friendsList.add(iter.getKey());
                    Log.d("*********FRIENDS", iter.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        allPostsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                if (friendsList.contains(dataSnapshot.getKey())) {
                    User newUser = new User(dataSnapshot.getKey(),
                            dataSnapshot.child("email").getValue().toString(),
                            dataSnapshot.child("displayname").getValue().toString(),
                            dataSnapshot.child("employment").getValue().toString(),
                            dataSnapshot.child("education").getValue().toString(),
                            dataSnapshot.child("company").getValue().toString(),
                            dataSnapshot.child("phone").getValue().toString(),
                            dataSnapshot.child("profilepic").getValue().toString(),
                            dataSnapshot.child("card").getValue().toString(),
                            dataSnapshot.child("hobbies").getValue().toString());

//                Log.d("********SNAPSHOT NAME ", dataSnapshot.child("displayname").getValue().toString());
//                Log.d("*******POSTKEY NAME ", dataSnapshot.getKey());
                    usersList.add(newUser);
                    users_filtered.add(newUser);
//                MyRecyclerAdapter.this.notifyItemInserted(moviesList.size()-1);
                    Friends_RecyclerAdapter.this.notifyItemInserted(users_filtered.size() - 1);
                    r.scrollToPosition(users_filtered.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for(int i=0; i < getFullItemCount(); i++) {
                    if (usersList.get(i).getPostKey_name().equals(snapshot.getKey())) {
                        usersList.remove(i);
                        Friends_RecyclerAdapter.this.notifyItemRemoved(i);
                        Friends_RecyclerAdapter.this.notifyItemRangeChanged(i, getFullItemCount());
                        Friends_RecyclerAdapter.this.notifyDataSetChanged();
                    }
                }
                for(int i=0; i < getItemCount(); i++) {
                    if (users_filtered.get(i).getPostKey_name().equals(snapshot.getKey())) {
                        users_filtered.remove(i);
                        Friends_RecyclerAdapter.this.notifyItemRemoved(i);
                        Friends_RecyclerAdapter.this.notifyItemRangeChanged(i, getItemCount());
                        Friends_RecyclerAdapter.this.notifyDataSetChanged();
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
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card_view, parent,false);
        final ViewHolder vh = new ViewHolder(v);
        v.setClickable(true);
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

        if (friendsList.contains(u.getPostKey_name())) {

//            Log.d("****NOT IN FRIENDS LIST", u.getPostKey_name());
//            if (u.postKey_name.equals(currentUser.getUid())) {
//                holder.delete_friend_v.setVisibility(View.INVISIBLE);
//            } else {
                holder.delete_friend_v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle("Remove " + u.displayname + " from your friends list?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseReference currUser = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
                                DatabaseReference friendsPath = currUser.child("friends");
                                friendsPath.child(users_filtered.get(position).postKey_name).removeValue();
                                holder.delete_friend_v.setVisibility(View.INVISIBLE);
                                Toast.makeText(mCtx, u.displayname + " has been deleted from your friends", Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                    }
                });
//            }

//            holder.delete_friend_v.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(mCtx, FriendsActivity.class);
//                    intent.putExtra("position", position);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mCtx.startActivity(intent);
//                }
//            });

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            String postKey_name = u.postKey_name;

            holder.uref = database.getReference("Users").child(postKey_name);
            String card_name = u.card;
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
                    Toast.makeText(mCtx, "FAIL", Toast.LENGTH_SHORT).show();
                }
            });

            holder.name_v.setText(u.displayname);
        }
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
        public ImageView delete_friend_v;
        public TextView name_v;
        DatabaseReference uref;
        ValueEventListener urefListener;

        public ViewHolder(View v){
            super(v);
            business_v = v.findViewById(R.id.friend_card_business);
            delete_friend_v=v.findViewById(R.id.friend_card_delete_friend);
            name_v=v.findViewById(R.id.friend_card_name);
        }
    }
}



