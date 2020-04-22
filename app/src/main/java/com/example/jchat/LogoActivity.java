package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogoActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onStarted();
            }
        },2500);
    }

    protected void onStarted() {
        super.onStart();
        if(currentUser != null)
            verifyUserExistance();
        else
        {
            sendUserToMainActivity();
        }
    }

    protected void sendUserToMainActivity()
    {
        Intent in = new Intent(LogoActivity.this,MainActivity.class);
        startActivity(in);
        finish();
    }

    protected void sendUserToChatMainActivity() {
        Toast.makeText(LogoActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
        Intent in = new Intent(LogoActivity.this, ChatMainActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        finish();
    }

    protected void sendUserToProfileActivity()
    {
        Intent in = new Intent(LogoActivity.this,ProfileActivity.class);
        startActivity(in);
        finish();
    }

    protected void verifyUserExistance()
    {
        String currentUserUid = currentUser.getUid();
        rootRef.child("Users").child(currentUserUid).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.getValue().toString().isEmpty()) {
                        sendUserToProfileActivity();
                    }
                    else
                    {
                        sendUserToChatMainActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });
    }

}
