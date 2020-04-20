package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatMainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    FirebaseUser currentUser;
    String currentUserId;
    ArrayList<String> contactNames, status, friendUid, chatId, profile_url;
    ChatAdapter adt;
    ListView lv;
    TabLayout tabLayout;
    String emailqr,nameqr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        lv = (ListView) findViewById(R.id.messages);
        contactNames = new ArrayList<String>();
        status = new ArrayList<String>();
        friendUid = new ArrayList<String>();
        chatId = new ArrayList<String>();
        profile_url = new ArrayList<String>();

        populateDetails(0);
        Toolbar mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        mTopToolbar.getOverflowIcon().setColorFilter(Color.parseColor("#008EFF"), PorterDuff.Mode.SRC_ATOP);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                System.out.println(tab.getPosition());
                populateDetails(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                contactNames.clear();
                status.clear();
                friendUid.clear();
                chatId.clear();
                profile_url.clear();
                lv.setAdapter(null);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public void populateDetails(int index) {
        final String category = getCategory(index);
        rootRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Qr code
                emailqr = dataSnapshot.child(currentUserId).child("email").getValue().toString();
                nameqr = dataSnapshot.child(currentUserId).child("name").getValue().toString();
                //
                DataSnapshot dats = dataSnapshot.child(currentUserId).child("friends");
                for (DataSnapshot ds : dats.getChildren()) {
                    GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {
                    };
                    HashMap<String, String> hm = ds.getValue(t);
                    String cid = "", chid = "";
                    for (HashMap.Entry<String, String> entry : hm.entrySet()) {
                        cid = entry.getValue();
                        chid = entry.getKey();
                    }
                    if (cid.equals(category)) {
                        profile_url.add(dataSnapshot.child(ds.getKey()).child("dp").getValue().toString());
                        friendUid.add(ds.getKey());
                        chatId.add(chid);
                        contactNames.add(dataSnapshot.child(ds.getKey()).child("name").getValue().toString());
                        status.add(dataSnapshot.child(ds.getKey()).child("status").getValue().toString());
                    }
                }
                String[] contactNamesArray = contactNames.toArray(new String[contactNames.size()]);
                String[] statusArray = status.toArray(new String[status.size()]);
                String[] profileArray= profile_url.toArray(new String[profile_url.size()]);
                adt = new ChatAdapter(ChatMainActivity.this, contactNamesArray, statusArray,profileArray );
                lv.setAdapter(adt);
                registerForContextMenu(lv);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent in = new Intent(ChatMainActivity.this, SingleChatActivity.class);
                        in.putExtra("name", contactNames.get(position));
                        in.putExtra("chatId", chatId.get(position));
                        in.putExtra("friendUId", friendUid);
                        startActivity(in);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected String getCategory(int index) {
        if (index == 2) {
            return "Others";
        } else if (index == 1) {
            return "Business";
        } else {
            return "Personal";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose Category");
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.categories_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DatabaseReference curRef = rootRef.child("Users").child(currentUserId).child("friends").child(friendUid.get(info.position)).child(chatId.get(info.position));
        if (item.getItemId() == R.id.categories_personal) {
            curRef.setValue("Personal");
        }
        if (item.getItemId() == R.id.categories_business) {
            curRef.setValue("Business");
        }
        if (item.getItemId() == R.id.categories_others) {
            curRef.setValue("Others");
        }
        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.options_logout) {
            mAuth.signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            sendUserToLoginActivity();
        } else if (item.getItemId() == R.id.options_settings) {
            sendUserToProfileActivity();
        } else if (item.getItemId() == R.id.options_find_friends) {
            sendUserToFindFriendsActivity();
        }
        return true;
    }

    private void sendUserToFindFriendsActivity() {
        Intent in = new Intent(ChatMainActivity.this, FindFriendsActivity.class);
        startActivity(in);
    }

    public void sendUserToLoginActivity() {
        Intent in = new Intent(ChatMainActivity.this, MainActivity.class);
        startActivity(in);
    }

    protected void sendUserToProfileActivity() {
        Intent in = new Intent(ChatMainActivity.this, ProfileActivity.class);
        startActivity(in);
    }

    public void showQr(View view) {
        CardView card = (CardView)findViewById(R.id.card);
        ImageView qrImage = findViewById(R.id.qr_image);
        MultiFormatWriter multiFormatWriter = null;
        if(multiFormatWriter == null)
        {
            multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(emailqr+" "+nameqr, BarcodeFormat.QR_CODE,200,200);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
        if(card.getAlpha() == 0)
        {
            Log.i("Card Test","Visi");
            card.animate().alpha(1).setDuration(200);
            lv.animate().alpha((float)0.2).setDuration(200);
        }
        else
        {
            Log.i("Card Test","Not visi");
            card.animate().alpha(0).setDuration(200);
            lv.animate().alpha((float)1).setDuration(200);
        }
    }
}
