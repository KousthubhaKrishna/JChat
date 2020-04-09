package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SingleChatActivity extends AppCompatActivity {

    private Date date;
    private SimpleDateFormat sdf,stf;
    private String chatId,friendUid;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUserId;
    private DatabaseReference rootRef;
    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        Intent in = getIntent();
        Toolbar tb = (Toolbar) findViewById(R.id.single_chat_toolbar);
        tb.setTitle(in.getStringExtra("name"));
        chatId = in.getStringExtra("chatId");
        friendUid = in.getStringExtra("friendUid");
        System.out.println("I am printing "+friendUid);

        sdf = new SimpleDateFormat("dd-MMM-yyyy");
        stf = new SimpleDateFormat("HH:mm");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesView = (RecyclerView)findViewById(R.id.display_chat);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesView.setLayoutManager(linearLayoutManager);
        userMessagesView.setAdapter(messageAdapter);
        userMessagesView = (RecyclerView)findViewById(R.id.display_chat);

        rootRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();
                userMessagesView.scrollToPosition(messagesList.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendMessage(View view)
    {
        EditText ed = (EditText)findViewById(R.id.single_chat_message);
        String mes = ed.getText().toString();
        if(mes.length()<1) {
            Toast.makeText(this, "Type Message", Toast.LENGTH_SHORT).show();
            return;
        }
        date = new Date();
        Message message = new Message(ed.getText().toString(),date,sdf.format(date),stf.format(date),currentUserId,friendUid);
        String messageID = rootRef.push().getKey();
        rootRef.child(messageID).setValue(message);
        ed.setText("");
    }
}
