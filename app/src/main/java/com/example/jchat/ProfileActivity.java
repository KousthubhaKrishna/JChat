package com.example.jchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText name,status;
    Button update;
    String name_string,status_string;
    static int chosenLangpos = 1;
    ArrayList<Integer> lang_codes;
    DatabaseReference rootRef,dr;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;
    String languages_array[];
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        dr = rootRef.child("Users").child(currentUserId);
        displayData();

        languages_array = getResources().getStringArray(R.array.languages_array);
        lang_codes = new ArrayList<Integer>();
        lang_codes.add(FirebaseTranslateLanguage.EN);
        lang_codes.add(FirebaseTranslateLanguage.HI);
        lang_codes.add(FirebaseTranslateLanguage.TE);
        lang_codes.add(FirebaseTranslateLanguage.TA);
        Spinner spinner = (Spinner) findViewById(R.id.languages_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);
    }

    protected void initialiseFields()
    {
        name_string = name.getText().toString();
        status_string = status.getText().toString();
    }

    protected void sendUserToChatMainActivity()
    {
        Intent in = new Intent(ProfileActivity.this,ChatMainActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(in);
        finish();
    }

    public void updateProfile(View view)
    {
        initialiseFields();
        if(name_string.isEmpty())
            Toast.makeText(this, "Please give your name ... ", Toast.LENGTH_SHORT).show();
        else if(status_string.isEmpty())
            Toast.makeText(this, "Please Write your Status ... ", Toast.LENGTH_SHORT).show();
        else
        {
            dr.child("uid").setValue(currentUserId);
            dr.child("name").setValue(name_string);
            dr.child("status").setValue(status_string);
            dr.child("language").setValue(lang_codes.get(chosenLangpos).toString());
            if(chosenLangpos != 0)
            {
                System.out.println("Calling update Language Preferences");
                updateLanguagePreferences();
            }
            else
            {
                System.out.println("Not Calling update Language Preferences");
                Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                sendUserToChatMainActivity();
            }
        }
    }

    public void displayData()
    {
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = (EditText)findViewById(R.id.profile_name);
                status = (EditText)findViewById(R.id.profile_status);
                if(dataSnapshot.child("name").exists())
                {
                    name.setText(dataSnapshot.child("name").getValue().toString());
                    status.setText(dataSnapshot.child("status").getValue().toString());
                }
                else
                {
                    System.out.println("Not Present");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if(pos>0)
        {
            chosenLangpos = pos-1;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void updateLanguagePreferences()
    {
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Downloading and Updating Language Preferences .. Please Wait ..");
        loadingBar.setMessage("Please wait..");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(lang_codes.get(chosenLangpos))
                        .setTargetLanguage(FirebaseTranslateLanguage.EN)
                        .build();
        final FirebaseTranslator englishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();
        englishTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                System.out.println("Model 1 Downloaded Success");
                                FirebaseTranslatorOptions options =
                                        new FirebaseTranslatorOptions.Builder()
                                                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                                                .setTargetLanguage(lang_codes.get(chosenLangpos))
                                                .build();
                                final FirebaseTranslator myTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
                                FirebaseModelDownloadConditions conditions1 = new FirebaseModelDownloadConditions.Builder()
                                        .build();
                                myTranslator.downloadModelIfNeeded(conditions1).addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void v) {
                                                System.out.println("Model 2 Downloaded Success");
                                                if (!ProfileActivity.this.isFinishing() && loadingBar != null) {
                                                    loadingBar.dismiss();
                                                }
                                                Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                                finish();
                                                System.out.println("Su came here");
                                            }
                                        }
                                ).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("Model 2 Download Failed");
                                    }
                                });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                                System.out.println("Model 1 Download Failed");
                            }
                        });
    }
}
