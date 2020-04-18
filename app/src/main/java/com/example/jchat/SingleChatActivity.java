package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;


import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3.TranslationServiceSettings;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class SingleChatActivity extends AppCompatActivity {

    private Date date;
    private SimpleDateFormat sdf,stf;
    private String chatId,friendUid;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUserId;
    private DatabaseReference rootRef,rf;
    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesView;
    private int mylangcode=11;
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

        //getTranslateService();
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

        rf = FirebaseDatabase.getInstance().getReference();
        rf.child("Users").child(currentUserId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String code = dataSnapshot.child("language").getValue().toString();
                        mylangcode = Integer.parseInt(code);
                        System.out.println("My language code "+code);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
        rootRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                translateAndAdd(dataSnapshot);
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
        translateAndSave(mes);
        ed.setText("");
    }


    public void translateAndSave(String text)
    {
        final Message message = new Message(text,date,sdf.format(date),stf.format(date),currentUserId,friendUid);
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(mylangcode)
                        .setTargetLanguage(FirebaseTranslateLanguage.EN)
                        .build();
        final FirebaseTranslator englishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        englishTranslator.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                // Translation successful.
                                System.out.println("Translation Save Success "+translatedText);
                                message.mes = translatedText;
                                String messageID = rootRef.push().getKey();
                                rootRef.child(messageID).setValue(message);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                // ...
                                System.out.println("Translation Failed");
                                System.out.println(e);
                            }
                        });
        //
    }

    public void translateAndAdd(final DataSnapshot dataSnapshot)
    {
        final Message message = dataSnapshot.getValue(Message.class);
        System.out.println("My lang code "+mylangcode);
        if(message.rec_mes==null)
        {
            FirebaseTranslatorOptions options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                            .setTargetLanguage(mylangcode)
                            .build();
            final FirebaseTranslator englishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
            englishTranslator.translate(message.mes)
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@NonNull String translatedText) {
                                    // Translation successful.
                                    System.out.println("Translation Add Success "+translatedText);
                                    message.rec_mes = translatedText;
                                    rf.child("Chats").child(chatId).child(dataSnapshot.getKey()).child("res_mes").setValue(translatedText);
                                    messagesList.add(message);
                                    messageAdapter.notifyDataSetChanged();
                                    userMessagesView.scrollToPosition(messagesList.size()-1);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Error.
                                    // ...
                                    System.out.println("Translation Failed");
                                    System.out.println(e);
                                }
                            });
        }
        else
        {
            messagesList.add(message);
            messageAdapter.notifyDataSetChanged();
            userMessagesView.scrollToPosition(messagesList.size()-1);
        }
    }
}
