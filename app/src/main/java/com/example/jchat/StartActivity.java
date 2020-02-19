package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference rootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser == null)
        {
            sendUserToMainActivity();
        }
        else
        {
            verifyUserExistance();
        }
    }

    protected void sendUserToMainActivity()
    {
        Intent in = new Intent(StartActivity.this,MainActivity.class);
        startActivity(in);
    }

    protected void sendUserToChatMainActivity()
    {
        Intent in = new Intent(StartActivity.this,ChatMainActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        finish();
    }

    protected void sendUserToProfileActivity()
    {
        Intent in = new Intent(StartActivity.this,ProfileActivity.class);
        startActivity(in);
    }

    protected void verifyUserExistance()
    {
        String currentUserUid = currentUser.getUid();
        rootRef.child("Users").child(currentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists())
                {
                    if(dataSnapshot.child("name").getValue().toString().isEmpty()) {
                        sendUserToProfileActivity();
                    }
                    else
                    {
                        Toast.makeText(StartActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
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
