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

import com.google.firebase.auth.FirebaseAuth;

public class ChatMainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        mAuth = FirebaseAuth.getInstance();

        ListView lv = (ListView)findViewById(R.id.messages);
        final String contactNames[] = {"Kousthubha","Krishna"};
        String messages[] = {"Hellow","Hi"};
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
        });

        Toolbar mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        mTopToolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
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
