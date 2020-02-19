package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    EditText name,status;
    Button update;
    String name_string,status_string;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        displayData();
    }

    protected void initialiseFields()
    {
        name_string = name.getText().toString();
        status_string = status.getText().toString();
    }

    protected void sendUserToChatMainActivity()
    {
        Intent in = new Intent(ProfileActivity.this,ChatMainActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        finish();
    }

    public void updateProfile(View view)
    {
        initialiseFields();
        if(name_string.isEmpty())
            Toast.makeText(this, "Please give your name ... ", Toast.LENGTH_SHORT).show();
        else if(status_string.isEmpty())
            Toast.makeText(this, "Please Write your Status ... ", Toast.LENGTH_SHORT).show();
        else
        {
            DatabaseReference dr = rootRef.child("Users").child(currentUserId);
            dr.child("uid").setValue(currentUserId);
            dr.child("name").setValue(name_string);
            dr.child("status").setValue(status_string);
            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
            sendUserToChatMainActivity();
        }
    }

    public void displayData()
    {
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = (EditText)findViewById(R.id.profile_name);
                status = (EditText)findViewById(R.id.profile_status);
                if(dataSnapshot.child("name").exists())
                {
                    name.setText(dataSnapshot.child("name").getValue().toString());
                    status.setText(dataSnapshot.child("status").getValue().toString());
                }
                else
                {
                    System.out.println("Not Present");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });
    }
}
