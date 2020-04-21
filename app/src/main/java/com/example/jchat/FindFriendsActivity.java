package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FindFriendsActivity extends AppCompatActivity {

    String searchEmail;
    EditText searchEmailView;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        searchEmailView = (EditText)findViewById(R.id.find_friend_email);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        Intent in  = getIntent();
        String scanned_email = in.getStringExtra("scanned_email");
        if(scanned_email!=null)
        {
            Log.i("Add Friend","Via qr");
            searchEmailView.setText(scanned_email);
            searchUsers(null);
        }
    }

    public void searchUsers(View view)
    {
        searchEmail = searchEmailView.getText().toString();
        rootRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String friendUid = searchUsersList(dataSnapshot,searchEmail);
                if(friendUid != null) {
                    if(!dataSnapshot.child(currentUserId).child("friends").child(friendUid).exists())
                    {
                        DatabaseReference curRef = rootRef.child("Users").child(currentUserId).child("friends");
                        String chatId = curRef.child(friendUid).push().getKey();
                        curRef.child(friendUid).child(chatId).setValue("Personal");
                        curRef.child(friendUid).child("locked").setValue("false");
                        rootRef.child("Users").child(friendUid).child("friends").child(currentUserId).child(chatId).setValue("Personal");
                        rootRef.child("Users").child(friendUid).child("friends").child(currentUserId).child("locked").setValue("false");
                        rootRef.child("Chats").child(chatId).setValue("");
                        Toast.makeText(FindFriendsActivity.this, "Successfully Added", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(FindFriendsActivity.this, "Already Added", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                    Toast.makeText(FindFriendsActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            public String searchUsersList(DataSnapshot dataSnapshot,String searchEmail)
            {
                boolean found = false;
                String friendKey="",friendUid = null;
                for(DataSnapshot ds:dataSnapshot.getChildren() )
                {
                    friendKey = dataSnapshot.child(ds.getKey()).child("email").getValue().toString();
                    friendUid = dataSnapshot.child(ds.getKey()).child("uid").getValue().toString();
                    System.out.println(friendKey);
                    System.out.println("KK "+dataSnapshot.child(currentUserId).child("friends").child(friendUid).exists());
                    if(friendKey.equals(searchEmail)) {
                        System.out.println("Returning FriendUid");
                        return friendUid;
                    }
                }
                System.out.println("Returning NUll");
                return null;
            }

        });
    }

    public void sendToQrActivity(View view) {
        Intent in = new Intent(FindFriendsActivity.this,ScannerActivity.class);
        startActivity(in);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(FindFriendsActivity.this,ChatMainActivity.class);
        startActivity(in);
        finish();
    }

    public void sendUserToLocationActivity(View view) {
        Intent in = new Intent(FindFriendsActivity.this,LocationActivity.class);
        startActivity(in);
        finish();
    }
}
