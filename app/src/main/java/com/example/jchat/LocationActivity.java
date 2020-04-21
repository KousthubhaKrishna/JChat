package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.NumberPicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LocationActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    FirebaseUser currentUser;
    String currentUserId;
    ArrayList<String> userIds;
    ArrayList<Double> lats,longs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        userIds = new ArrayList<String>();
        lats = new ArrayList<Double>();
        longs = new ArrayList<Double>();
        getNearbyUsers();
    }

    private void getNearbyUsers() {
        rootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double mylat = Double.parseDouble(dataSnapshot.child(currentUserId).child("location").child("latitude").getValue().toString());
                double mylon = Double.parseDouble(dataSnapshot.child(currentUserId).child("location").child("longitude").getValue().toString());
                for(DataSnapshot dats : dataSnapshot.getChildren())
                {
                    String uid = dats.child("uid").getValue().toString();
                    if(!uid.equals(currentUserId))
                    {
                        double otherlat = Double.parseDouble(dats.child("location").child("latitude").getValue().toString());
                        double otherlon = Double.parseDouble(dats.child("location").child("longitude").getValue().toString());
                        double dist = distance(mylat,mylon,otherlat,otherlon);
                        System.out.println(uid+" "+dist);
                        if(dist<20)
                        {
                            userIds.add(uid);
                            lats.add(otherlat);
                            longs.add(otherlon);
                        }
                    }
                }
                System.out.println(userIds);
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

}
