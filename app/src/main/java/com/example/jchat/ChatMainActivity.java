package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatMainActivity extends AppCompatActivity {

    SimpleDateFormat sdf,stf;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    FirebaseUser currentUser;
    String currentUserId;
    ArrayList<String> contactNames, status, friendUid, chatId, profile_url;
    ChatAdapter adt;
    ListView lv;
    TabLayout tabLayout;
    String emailqr, nameqr;
    boolean flag = true;

    //Location Variable
    private static final int REQUEST_CODE_LOCATION_PERMISSION=1;
    private TextView textLatLong;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);

        sdf = new SimpleDateFormat("MMM d");
        stf = new SimpleDateFormat("h:mm a");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Online").child(currentUserId).setValue("Online");

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

        // Permission for location access
        progressBar = findViewById(R.id.progressBar);
        if(ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ChatMainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
        }
        else {
            getCurrentLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!MyApplication.wasInBg){
            Date date = new Date();
            rootRef.child("Online").child(currentUserId).setValue("last seen at " +stf.format(date)+" on "+sdf.format(date));
        }
        else {
            flag=false;
            rootRef.child("Online").child(currentUserId).setValue("online");
        }
    }

    public void populateDetails(int index) {
        final String category = getCategory(index);
        rootRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                //Qr code
                emailqr = dataSnapshot.child(currentUserId).child("email").getValue().toString();
                nameqr = dataSnapshot.child(currentUserId).child("name").getValue().toString();
                //
                final DataSnapshot dats = dataSnapshot.child(currentUserId).child("friends");
                for (DataSnapshot ds : dats.getChildren()) {
                    GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {
                    };
                    HashMap<String, String> hm = ds.getValue(t);
                    String cid = "", chid = "";
                    for (HashMap.Entry<String, String> entry : hm.entrySet()) {
                        cid = entry.getValue();
                        chid = entry.getKey();
                        break;
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
                final String[] statusArray = status.toArray(new String[status.size()]);
                String[] profileArray= profile_url.toArray(new String[profile_url.size()]);
                adt = new ChatAdapter(ChatMainActivity.this, contactNamesArray, statusArray,profileArray );
                lv.setAdapter(adt);
                registerForContextMenu(lv);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final Intent in = new Intent(ChatMainActivity.this, SingleChatActivity.class);
                        in.putExtra("name", contactNames.get(position));
                        in.putExtra("chatId", chatId.get(position));
                        in.putExtra("friendUId", friendUid.get(position));
                        String lock_status =dats.child(friendUid.get(position)).child("locked").getValue().toString();
                        if(lock_status.equals("true"))
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ChatMainActivity.this);
                            builder.setMessage("Password Check");
                            LayoutInflater layoutInflater = getLayoutInflater();
                            final View v = layoutInflater.inflate(R.layout.activity_test,null);
                            final EditText ed = v.findViewById(R.id.passcode_check);
                            //ed.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            //ed.setHint("Enter Pass-Code");
                            builder.setView(v);
                            builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }

                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                            final AlertDialog alert1 = builder.create();
                            alert1.show();

                            alert1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    final String passcode = ed.getText().toString();
                                    String db_passcode = dataSnapshot.child(currentUserId).child("pass_code").getValue().toString();
                                    if (db_passcode.equals(passcode)) {
                                        alert1.dismiss();
                                        startActivity(in);
                                        flag=false;
                                    } else {
                                        Toast.makeText(ChatMainActivity.this, "Invalid Pass Code", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            alert1.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    alert1.dismiss();
                                }
                            });
                        }
                        else {
                            startActivity(in);
                            flag=false;
                        }
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
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set Password");
            builder.setMessage("Enter the password for your chats");
            LayoutInflater layoutInflater = getLayoutInflater();
            final View v = layoutInflater.inflate(R.layout.activity_lock,null);
            final EditText password = v.findViewById(R.id.password_lock);
            final EditText passcode = v.findViewById(R.id.passcode_lock);
            builder.setView(v);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            final AlertDialog alert1 = builder.create();
            alert1.show();
            alert1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final String passwordEntered = password.getText().toString();
                    final String passcodeEntered = passcode.getText().toString();
                    if( passwordEntered.isEmpty() || passcodeEntered.isEmpty())
                    {
                        Toast.makeText(ChatMainActivity.this, "Provide both Password and Pass-Code", Toast.LENGTH_SHORT).show();
                    }
                    else if( passcodeEntered.length() < 4)
                    {
                        Toast.makeText(ChatMainActivity.this, "Enter Pass-Code of at least 4 digits", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mAuth.signInWithEmailAndPassword(emailqr, passwordEntered)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            rootRef.child("Users").child(currentUserId).child("pass_code").setValue(passcodeEntered);
                                            Toast.makeText(ChatMainActivity.this, "Chat Lock Set", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ChatMainActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
               }
            });

            alert1.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    alert1.dismiss();
                }
            });
        }
        return true;
    }

    private void sendUserToFindFriendsActivity() {
        Intent in = new Intent(ChatMainActivity.this, FindFriendsActivity.class);
        startActivity(in);
        finish();
    }

    public void sendUserToLoginActivity() {
        Intent in = new Intent(ChatMainActivity.this, MainActivity.class);
        startActivity(in);
        finish();
    }

    protected void sendUserToProfileActivity() {
        Intent in = new Intent(ChatMainActivity.this, ProfileActivity.class);
        startActivity(in);
        finish();
    }

    public void showQr(View view) {
        CardView card = (CardView)findViewById(R.id.card);
        ImageView qrImage = (ImageView)findViewById(R.id.qr_image);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Date date = new Date();
        rootRef.child("Online").child(currentUserId).setValue("last seen at " +stf.format(date)+" on "+sdf.format(date));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Date date = new Date();
        rootRef.child("Online").child(currentUserId).setValue("last seen at " +stf.format(date)+" on "+sdf.format(date));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length>0){
            getCurrentLocation();
        }else{
            Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation(){
        progressBar.setVisibility(View.VISIBLE);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(ChatMainActivity.this)
                .requestLocationUpdates(locationRequest,new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(ChatMainActivity.this)
                                .removeLocationUpdates(this);
                        if(locationResult!=null && locationResult.getLocations().size()>0){
                            int latestLocationIndex = locationResult.getLocations().size()-1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            rootRef.child("Users").child(currentUserId).child("location").child("latitude").setValue(latitude);
                            rootRef.child("Users").child(currentUserId).child("location").child("longitude").setValue(longitude);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }, Looper.getMainLooper());
    }
}
