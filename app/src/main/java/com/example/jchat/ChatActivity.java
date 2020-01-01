package com.example.jchat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Error Check","Entered");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_);

        ListView lv = (ListView)findViewById(R.id.messages);

        String names[] = {"Kousthubha","Rohith"};
        String messages[] = {"Hi","Hello"};

        ChatAdapter adt = new ChatAdapter(this,names,messages,null);
        lv.setAdapter(adt);

    }
}