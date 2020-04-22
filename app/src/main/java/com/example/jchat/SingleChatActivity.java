package com.example.jchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SingleChatActivity extends AppCompatActivity {
    private Date date;
    private SimpleDateFormat sdf,stf;
    private String chatId,friendUid;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUserId;
    private DatabaseReference rootRef,rf,ref,root;
    final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    RecyclerView userMessagesView;
    private int mylangcode=11;
    boolean isLocked = false;
    String onoroff;


    @Override
    protected void onResume() {
        super.onResume();
        if(!MyApplication.wasInBg){
            Date date = new Date();
            root.child("Online").child(currentUserId).setValue("last seen at " +stf.format(date)+" on "+sdf.format(date));
        }
        else {
            root.child("Online").child(currentUserId).setValue("online");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        Intent in = getIntent();
        TextView name = findViewById(R.id.frndTv);
        final TextView online = findViewById(R.id.onoroffTv);
        name.setText(in.getStringExtra("name"));
        chatId = in.getStringExtra("chatId");
        friendUid = in.getStringExtra("friendUId");

        sdf = new SimpleDateFormat("MMM d");
        stf = new SimpleDateFormat("h:mm a");

        //getTranslateService();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        root = FirebaseDatabase.getInstance().getReference();
        root.child("Online").child(currentUserId).setValue("online");
        rootRef = rootRef.child("Chats").child(chatId);


        userMessagesView = (RecyclerView)findViewById(R.id.display_chat);
        messageAdapter = new MessageAdapter(messagesList,chatId);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesView.setLayoutManager(linearLayoutManager);
        userMessagesView.setAdapter(messageAdapter);

        rf = FirebaseDatabase.getInstance().getReference();
        rf.child("Users").child(currentUserId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String code = dataSnapshot.child("language").getValue().toString();
                        mylangcode = Integer.parseInt(code);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

        root.child("Online").child(friendUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onoroff = dataSnapshot.getValue().toString();
                online.setText(onoroff);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rootRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                if(message.senderUId.equals(currentUserId))
                {
                    messagesList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    userMessagesView.scrollToPosition(messagesList.size() - 1);
                }
                else if(message.rec_mes.length()>0)
                {
                    messagesList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    userMessagesView.scrollToPosition(messagesList.size() - 1);
                }
                else
                {
                    translateAndAdd(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Message message = dataSnapshot.getValue(Message.class);
                int ind = 0;
                for(Message lo : messagesList)
                {
                    if(lo.mid.equals(message.mid))
                    {
                        break;
                    }
                    ind++;
                }
                messagesList.remove(ind);
                messageAdapter.notifyDataSetChanged();
                userMessagesView.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("friends").child(friendUid).getRef();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ImageView im = (ImageView)findViewById(R.id.lockIv);
                if(dataSnapshot.child("locked").getValue().toString().equals("true")) { ;
                    isLocked = true;
                    im.setImageResource(R.drawable.ic_lock);
                }
                else {
                    isLocked = false;
                    im.setImageResource(R.drawable.ic_unlock);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void sendMessage(View view)
    {
        EditText ed = (EditText)findViewById(R.id.single_chat_message);
        String mes = ed.getText().toString().trim();
        if(mes.length()==0) {
            Toast.makeText(this, "Type Message", Toast.LENGTH_SHORT).show();
            return;
        }
        date = new Date();
        translateAndSave(mes);
        ed.setText("");
    }


    public void translateAndSave(final String text)
    {
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
                                String messageID = rootRef.push().getKey();
                                final Message message = new Message(messageID,text,date,sdf.format(date),stf.format(date),currentUserId,friendUid);
                                message.mes = translatedText;
                                rootRef.child(messageID).setValue(message);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("Translation Failed");
                                System.out.println(e);
                            }
                        });
    }

    public void translateAndAdd(final DataSnapshot dataSnapshot)
    {
        final Message message = dataSnapshot.getValue(Message.class);
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
                                    message.rec_mes = translatedText;
                                    rf.child("Chats").child(chatId).child(dataSnapshot.getKey()).child("rec_mes").setValue(translatedText);
                                    messagesList.add(message);
                                    messageAdapter.notifyDataSetChanged();
                                    userMessagesView.scrollToPosition(messagesList.size() - 1);
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

    public void toggleLock(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ImageView im = (ImageView)findViewById(R.id.lockIv);
        builder.setTitle("Confirm");
        if(isLocked)
        {
            builder.setMessage("Unlock this chat ?");
            builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ref.child("locked").setValue("false");
                    isLocked = false;
                    Toast.makeText(SingleChatActivity.this, "Unlocked", Toast.LENGTH_SHORT).show();
                    im.setImageResource(R.drawable.ic_unlock);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        else
        {
            builder.setMessage("Lock this chat ?");
            builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ref.child("locked").setValue("true");
                    isLocked = true;
                    Toast.makeText(SingleChatActivity.this, "Locked", Toast.LENGTH_SHORT).show();
                    im.setImageResource(R.drawable.ic_lock);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        AlertDialog a1 = builder.create();
        a1.show();
    }

    @Override
    public void onBackPressed() {
        Intent in = new Intent(SingleChatActivity.this,ChatMainActivity.class);
        startActivity(in);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        Date date = new Date();
        root.child("Online").child(currentUserId).setValue("last seen at " +stf.format(date)+" on "+sdf.format(date));
        super.onPause();
    }

}
