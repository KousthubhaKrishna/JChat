package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatMainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    FirebaseUser currentUser;
    String currentUserId;
    ArrayList<String> contactNames, status, friendUid, chatId;
    ChatAdapter adt;
    ListView lv;
    TabLayout tabLayout;

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
                        friendUid.add(ds.getKey());
                        chatId.add(chid);
                        contactNames.add(dataSnapshot.child(ds.getKey()).child("name").getValue().toString());
                        status.add(dataSnapshot.child(ds.getKey()).child("status").getValue().toString());
                    }
                }
                String[] contactNamesArray = contactNames.toArray(new String[contactNames.size()]);
                String[] statusArray = status.toArray(new String[status.size()]);
                adt = new ChatAdapter(ChatMainActivity.this, contactNamesArray, statusArray, null);
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
            sendUserToLoginActvity();
        } else if (item.getItemId() == R.id.options_settings) {
            sendUserToProfileActivity();
        } else if (item.getItemId() == R.id.options_find_friends) {
            sendUserToFindFriendsActvity();
        }
        return true;
    }

    private void sendUserToFindFriendsActvity() {
        Intent in = new Intent(ChatMainActivity.this, FindFriendsActivity.class);
        startActivity(in);
    }

    public void sendUserToLoginActvity() {
        Intent in = new Intent(ChatMainActivity.this, MainActivity.class);
        startActivity(in);
    }

    protected void sendUserToProfileActivity() {
        Intent in = new Intent(ChatMainActivity.this, ProfileActivity.class);
        startActivity(in);
    }

}
