package com.example.afinal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeolocationFragment extends Fragment implements OnMapReadyCallback, ItemClickListener{
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/geofire");
    private GoogleMap mMap;
    private Circle mySearchCircle;
    private GeoFire geoFire = new GeoFire(ref);
    private GeoQuery geoQuery = null;
    private List<String> keyList = null;
    private HashMap<String, Marker> markerList = null;
    private HashMap<String, User> key_to_User = null;
    private RecyclerView geoRecyclerView;
    private GeolocationRecyclerAdapter geoRecyclerAdapter;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 5 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    SimpleDateFormat localDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static final int REQUEST_FOR_LOCATION = 0012;
    View v;
    Context c;

    public void newLocation(Location lastLocation) {
        if (geoQuery != null) {
            geoQuery.setCenter(new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
            //mySearchCircle.setCenter(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        }
        else {
            geoQuery = geoFire.queryAtLocation(new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), 1);
            //mySearchCircle = mMap.addCircle(new CircleOptions().center(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())).radius(.25).strokeWidth(0f).fillColor(0x550000FF));
            geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                @Override
                public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                    final String userKey = dataSnapshot.getKey();
                    if (key_to_User.containsKey(userKey))
                        return;
                    database.getReference("Users/" + userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User u = dataSnapshot.getValue(User.class);
                            Log.d("onDataEntered", "Post model just created");
                            if(!userKey.equals(currentUser.getUid())) {
                                Toast.makeText(c, "User, "+u.displayname+", has entered your area.", Toast.LENGTH_SHORT).show();
                                key_to_User.put(userKey, u);
                                keyList.add(userKey);
                                if(markerList.containsKey(userKey))
                                {
                                    Marker prev = markerList.get(userKey);
                                    prev.setPosition(new LatLng(location.latitude,location.longitude));
                                }
                                if(!markerList.containsKey(userKey))
                                {
                                    markerList.put(userKey, mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_grey))));
                                }
                            }
                            geoRecyclerAdapter.notifyItemInserted(keyList.size() - 1);
                            geoRecyclerView.scrollToPosition(keyList.size() - 1);
                            Log.d("onDataEntered", "Here");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onDataExited(DataSnapshot dataSnapshot) {
                    final String userKey = dataSnapshot.getKey();
                    if (!key_to_User.containsKey(userKey))
                        return;
                    database.getReference("Users/" + userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int i = 0;
                            if(!userKey.equals(currentUser.getUid())) {
                                Toast.makeText(c, "User, "+key_to_User.get(userKey).displayname+", has left your area.", Toast.LENGTH_SHORT).show();
                                key_to_User.remove(userKey);
                                for(String curKey : keyList)
                                {
                                    if (curKey.equals(userKey))
                                    {
                                        break;
                                    }
                                    i++;
                                }
                                keyList.remove(i);
                                if(markerList.containsKey(userKey))
                                {
                                    Marker prev = markerList.get(userKey);
                                    prev.remove();
                                    markerList.remove(userKey);
                                }
                            }
                            geoRecyclerAdapter.notifyDataSetChanged();
                            Log.d("onDataExited", "Here");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                    final String userKey = dataSnapshot.getKey();
                    if (!key_to_User.containsKey(userKey))
                        return;
                    database.getReference("Users/" + userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User u = dataSnapshot.getValue(User.class);
                            Log.d("onDataEntered", "Post model just created");
                            if (!userKey.equals(currentUser.getUid())) {
                                Toast.makeText(c, "User, " + u.displayname + ", is on the move!", Toast.LENGTH_SHORT).show();
                                if (markerList.containsKey(userKey)) {
                                    Marker prev = markerList.get(userKey);
                                    prev.setPosition(new LatLng(location.latitude, location.longitude));
                                }
                                if (!markerList.containsKey(userKey)) {
                                    markerList.put(userKey, mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_grey))));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {


                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.geolocation_fragment, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        keyList = new ArrayList<>();
        key_to_User = new HashMap<>();
        markerList = new HashMap<>();
        Bundle args = getArguments();
        //c = args.c;
        c = container.getContext();
        //Inflate the layout for this fragment

        geoRecyclerView = v.findViewById(R.id.geo_recylcer_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(c);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.scrollToPosition(0);
        geoRecyclerView.setLayoutManager(layoutManager);
        geoRecyclerAdapter= new GeolocationRecyclerAdapter(key_to_User, keyList, this, c);
        geoRecyclerView.setAdapter(geoRecyclerAdapter);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(c);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(1);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(c);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
// TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(c, "We need permission to access your location.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_FOR_LOCATION);
            getFragmentManager().popBackStack();
        }
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    return;
                }

                Location lastLocation = locationResult.getLastLocation();
                //if(!initialLoad){
                //  initialLoad =true;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())).zoom(12).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // }
                newLocation(lastLocation);
                final String lastLat = String.valueOf(lastLocation.getLatitude());
                final String lastLng = String.valueOf(lastLocation.getLongitude());
                geoFire.setLocation(currentUser.getUid(), new GeoLocation(Double.parseDouble(lastLat),Double.parseDouble(lastLng)));
                if(markerList.containsKey(currentUser.getUid()))
                {
                    Marker previous = markerList.get(currentUser.getUid());
                    previous.setPosition(new LatLng(Double.parseDouble(lastLat), Double.parseDouble(lastLng)));
                    if(mySearchCircle != null)
                    {
                        mySearchCircle.setCenter(new LatLng(Double.parseDouble(lastLat), Double.parseDouble(lastLng)));
                    }
                    if (mySearchCircle == null) {
                        mySearchCircle = mMap.addCircle(new CircleOptions().center(new LatLng(Double.parseDouble(lastLat), Double.parseDouble(lastLng))).radius(1000).strokeWidth(1f).fillColor(0x500084d3));
                    }

                }
                if(!markerList.containsKey(currentUser.getUid()))
                {
                    markerList.put(currentUser.getUid(),mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lastLat), Double.parseDouble(lastLng))).icon(BitmapDescriptorFactory.fromResource(R.drawable.my_purple_marker))));
                    mySearchCircle = mMap.addCircle(new CircleOptions().center(new LatLng(Double.parseDouble(lastLat), Double.parseDouble(lastLng))).radius(1000).strokeWidth(1f).fillColor(0x500084d3));
                }
            }
        };
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(c);
        mapFragment.getMapAsync(this);
        //Source
        //https://developer.android.com/training/permissions/requesting#java
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(c, "We need permission to access your location.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_FOR_LOCATION);
            //getFragmentManager().popBackStack();
            return v;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
            final String lat = String.valueOf(location.getLatitude());
            final String lng = String.valueOf(location.getLongitude());
            geoFire.setLocation(currentUser.getUid(), new GeoLocation(Double.parseDouble(lat),Double.parseDouble(lng)));
            if(markerList.containsKey(currentUser.getUid()))
            {
                Marker previous = markerList.get(currentUser.getUid());
                previous.setPosition(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            }
            if(!markerList.containsKey(currentUser.getUid()))
            {
                markerList.put(currentUser.getUid(),mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))).icon(BitmapDescriptorFactory.fromResource(R.drawable.my_purple_marker))));
            }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users");
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                // This is where I would read lat long and then parse remaining users lat/long to see who is within a certain radius
            }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });

        geoRecyclerAdapter.setOnListItemClickListener(new onListItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(c, "Running on item click", Toast.LENGTH_SHORT).show();
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                String currUser = keyList.get(position);
                User u =key_to_User.get(currUser);
                //String currUser = my_AddFriends_RecyclerAdapter.getItem(position).getPostKey_name();
                //final DatabaseReference userRef= db.getReference("Users").child(currUser);

                final FirebaseStorage storage = FirebaseStorage.getInstance();
                //final AddFriends_RecyclerAdapter.User u =key_to_User.get(currUser);

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
                                Intent intent = new Intent(c, UserDetailActivity.class);
                                intent.putExtra("card", cardURI.toString());
                                intent.putExtra("profilepic", picURI.toString());
                                intent.putExtra("phone", u.phone);
                                intent.putExtra("displayname", u.displayname);
                                intent.putExtra("email", u.email);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(c, e.getMessage() + " (profile_pic)", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(c, exception.getMessage() + " (business card)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        return v;

    }

    @Override
    public void onAttach (Context context) {
        Log.d("onAttach", "Running onAttach Fragment");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        Log.d("OnDetach", "Running on detach");
        super.onDetach();
        v = null;
    }

    @Override
    public void onItmeClick(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(12).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            c, R.raw.silvermapstyle));

            if (!success) {
                Log.e("UTARZ", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("UTARZ", "Can't find style. Error: ", e);
        }
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //show the image
                String uid = "";
                for (Map.Entry mapElement : markerList.entrySet()) {
                    if (mapElement.getValue().equals(marker))
                    {
                        uid = mapElement.getKey().toString();
                    }
                }
                if(uid.equals(currentUser.getUid()))
                {
                    Toast.makeText(c, "That's you silly!", Toast.LENGTH_SHORT).show();

                }
                else if(!uid.equals(currentUser.getUid())) {
                    Toast.makeText(c, "That's "+key_to_User.get(uid).displayname, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        geoRecyclerAdapter.setOnListItemClickListener(new onListItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                String userClickedKey = keyList.get(position);
                final DatabaseReference geoRef = db.getReference("geofire/"+userClickedKey+"/l");
            }
        });
//        LatLng latLngCenter = new LatLng(INITIAL_CENTER.latitude, INITIAL_CENTER.longitude);
//        this.searchCircle = mMap.addCircle(new CircleOptions().center(latLngCenter).radius(10));
//        this.searchCircle = this.mMap.addCircle(new
//                CircleOptions().center(latLngCenter).radius(1000));
//        this.searchCircle.setFillColor(Color.argb(66, 255, 0, 255));
//        this.searchCircle.setStrokeColor(Color.argb(66, 0, 0, 0));
//        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCenter,
//                INITIAL_ZOOM_LEVEL));
//        this.map.setOnCameraChangeListener(this);


    }
}