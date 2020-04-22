package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    FirebaseUser currentUser;
    String currentUserId,currentUserEmail;
    ArrayList<String> userIds,userNames,userEmails;
    ArrayList<Double> lats,longs;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the Supp ortMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        userIds = new ArrayList<String>();
        userEmails = new ArrayList<String>();
        userNames = new ArrayList<String>();
        lats = new ArrayList<Double>();
        longs = new ArrayList<Double>();
        context = this;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(!currentUserEmail.equals(marker.getTag().toString()))
                {
                    Intent in = new Intent(MapsActivity.this,FindFriendsActivity.class);
                    in.putExtra("selected_email",marker.getTag().toString());
                    in.putExtra("selected_name",marker.getTitle());
                    startActivity(in);
                    finish();
                }
                else
                {
                    marker.setTitle(" Me ");
                }
                return false;
            }
        });
        getNearbyUsers(googleMap);
    }


    // Firebase Users Retrieval
    private void getNearbyUsers(final GoogleMap googleMap) {
        rootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double mylat = Double.parseDouble(dataSnapshot.child(currentUserId).child("location").child("latitude").getValue().toString());
                double mylon = Double.parseDouble(dataSnapshot.child(currentUserId).child("location").child("longitude").getValue().toString());
                currentUserEmail = dataSnapshot.child(currentUserId).child("email").getValue().toString();
                for(DataSnapshot dats : dataSnapshot.getChildren())
                {
                    String uid = dats.child("uid").getValue().toString();
                    String name = dats.child("name").getValue().toString();
                    String email = dats.child("email").getValue().toString();
                    double otherlat = Double.parseDouble(dats.child("location").child("latitude").getValue().toString());
                    double otherlon = Double.parseDouble(dats.child("location").child("longitude").getValue().toString());
                    double dist = distance(mylat,mylon,otherlat,otherlon);
                    if(dist<100)
                    {
                        userIds.add(uid);
                        userNames.add(name);
                        userEmails.add(email);
                        lats.add(otherlat);
                        longs.add(otherlon);
                    }
                }
                mMap = googleMap;
                mMap.setMinZoomPreference(10.0f);
                for(int i=0;i<userIds.size();i++)
                {
                    LatLng userloc = new LatLng(lats.get(i),longs.get(i));
                    MarkerOptions options = new MarkerOptions().position(userloc);
                    if(userIds.get(i).equals(currentUserId))
                    {
                        Marker m = mMap.addMarker(options.title(" Me "));
                        m.setTag(userEmails.get(i));
                        m.showInfoWindow();
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(userloc));
                    }
                    else
                    {
                        IconGenerator iconFactory = new IconGenerator(context);
                        iconFactory.setBackground(getResources().getDrawable(R.drawable.ic_user_name));
                        iconFactory.setTextAppearance(R.style.bText);
                        Marker m = mMap.addMarker(options.title(userNames.get(i)));
                        m.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(userNames.get(i))));
                        m.setTag(userEmails.get(i));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public void onBackPressed() {
        Intent in = new Intent(MapsActivity.this,FindFriendsActivity.class);
        startActivity(in);
        finish();
    }
}
