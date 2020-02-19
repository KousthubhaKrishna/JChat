package com.example.jchat;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SingleChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        Intent in = getIntent();
        Toolbar tb = (Toolbar) findViewById(R.id.single_chat_toolbar);
        tb.setTitle(in.getStringExtra("name"));
    }
}
