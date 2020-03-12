package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatMainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    FirebaseUser currentUser;
    String currentUserId;
    String contactNames[],status[],friendUid[],chatId[];
    ChatAdapter adt;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        populateDetails();
        lv = (ListView)findViewById(R.id.messages);

        Toolbar mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        mTopToolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
    }

    private void populateDetails() {
        rootRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot dats = dataSnapshot.child(currentUserId).child("friends");
                int n = (int)dats.getChildrenCount();
                contactNames= new String[n];
                status = new String[n];
                friendUid = new String[n];
                chatId = new String[n];
                int i=0;
                for(DataSnapshot ds:dats.getChildren() )
                {
                    friendUid[i] = dataSnapshot.child(ds.getKey()).getValue().toString();
                    chatId[i] = ds.getValue().toString();
                    contactNames[i] = dataSnapshot.child(ds.getKey()).child("name").getValue().toString();
                    status[i] = dataSnapshot.child(ds.getKey()).child("status").getValue().toString();
                    i+=1;
                }
                adt = new ChatAdapter(ChatMainActivity.this,contactNames,status,null);
                lv.setAdapter(adt);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        Intent in = new Intent(ChatMainActivity.this,SingleChatActivity.class);
                        in.putExtra("name",contactNames[position]);
                        in.putExtra("chatId",chatId[position]);
                        in.putExtra("friendUId",friendUid);
                        startActivity(in);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.options_logout)
        {
            mAuth.signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            sendUserToLoginActvity();
        }
        else if(item.getItemId() == R.id.options_settings)
        {
            sendUserToProfileActivity();
        }
        else if(item.getItemId() == R.id.options_find_friends)
        {
            sendUserToFindFriendsActvity();
        }
        return true;
    }

    private void sendUserToFindFriendsActvity()
    {
        Intent in = new Intent(ChatMainActivity.this,FindFriendsActivity.class);
        startActivity(in);
    }

    public void sendUserToLoginActvity()
    {
        Intent in = new Intent(ChatMainActivity.this,MainActivity.class);
        startActivity(in);
    }

    protected void sendUserToProfileActivity()
    {
        Intent in = new Intent(ChatMainActivity.this,ProfileActivity.class);
        startActivity(in);
    }

}
/*ListView lv = (ListView)findViewById(R.id.messages);
        ChatAdapter adt = new ChatAdapter(this,contactNames,messages,null);
        lv.setAdapter(adt);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = contactNames[position];
                Intent in = new Intent(ChatMainActivity.this,SingleChatActivity.class);
                in.putExtra("name",name);
                startActivity(in);
            }
        });*/
