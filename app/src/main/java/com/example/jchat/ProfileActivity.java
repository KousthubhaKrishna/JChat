package com.example.jchat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;

import static android.os.Build.VERSION_CODES.M;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText name,status;
    Button update;
    String name_string,status_string,dp;
    static int chosenLangpos = 1;
    ArrayList<Integer> lang_codes;
    DatabaseReference rootRef,dr;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    StorageReference storageReference;
    String currentUserId;
    String languages_array[];
    ProgressDialog loadingBar;
    ImageView profile;
    String profilePhoto="dp";
    String storagePath= "Users_Profile_Images/";

    private static final int CAMERA_REQUEST_CODE= 100;
    private static final int STORAGE_REQUEST_CODE= 200;
    private static final int IMAGE_PICK_GALLERY_CODE= 300;
    private static final int IMAGE_PICK_CAMERA_CODE= 400;

    String cameraPermissions[];
    String storagePermissions[];
    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        update=findViewById(R.id.profile_update);
        profile=findViewById(R.id.dp);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        dr = rootRef.child("Users").child(currentUserId);
        storageReference= FirebaseStorage.getInstance().getReference();
        cameraPermissions= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
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
        profile.setClickable(true);
        profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });
        update.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                updateProfile(v);
            }
        });
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
            /*try {
                if(dp.equals("")) {
                    Picasso.get().load(R.drawable.user_icon).into(profile);
                }
                else{
                    Picasso.get().load(dp).into(profile);
                }
            } catch (Exception e) {
                Picasso.get().load(R.drawable.user_icon).into(profile);
            }*/
        }
    }



    private boolean checkStoragePermission()
    {
        boolean result= ContextCompat.checkSelfPermission( ProfileActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @RequiresApi(api = M)
    private void requestStoragePermission()
    {
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission()
    {
        boolean result1= ContextCompat.checkSelfPermission( ProfileActivity.this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);

        boolean result2= ContextCompat.checkSelfPermission( ProfileActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result2 && result1;
    }

    @RequiresApi(api = M)
    private void requestCameraPermission()
    {
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void showImagePicDialog()
    {
        String options[]={"Camera","Gallery","Remove"};
        AlertDialog.Builder builder=new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle( "Select Option..." );
        builder.setItems( options, new DialogInterface.OnClickListener() {
            @RequiresApi(api = M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                } else {
                    dr.child("dp").setValue("");
                }

            }
        } );
        builder.create().show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:
            {
                if(grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted)
                    {
                        pickFromCamera();
                    }
                    else
                    {
                        Toast.makeText(ProfileActivity.this,"Please enable camera and storage permission",Toast.LENGTH_SHORT ).show();
                    }


                }
            }
            break;
            case STORAGE_REQUEST_CODE:
            {
                if(grantResults.length>0)
                {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted)
                    {
                        pickFromGallery();
                    }
                    else
                    {
                        Toast.makeText(ProfileActivity.this,"Please enable storage permission",Toast.LENGTH_SHORT ).show();
                    }
                }
            }
            break;

        }


        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK)
        {
            if(requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                image_uri=data.getData();
                uploadProfilePhoto(image_uri);
            }

            if(requestCode== IMAGE_PICK_CAMERA_CODE)
            {
                uploadProfilePhoto(image_uri);
            }
        }


        super.onActivityResult( requestCode, resultCode, data );
    }

    private void uploadProfilePhoto(Uri uri) {

        //pd.show();
        //String storagePath="";
        String filePathAndName = storagePath+ ""+profilePhoto+"_"+currentUserId;
        StorageReference storageReference2nd= storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful())
                        {
                            dr.child(profilePhoto).setValue(downloadUri.toString())
                                    .addOnSuccessListener( new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //pd.dismiss();
                                            Toast.makeText(ProfileActivity.this,"Image Updated",Toast.LENGTH_SHORT).show();
                                        }
                                    } )
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // pd.dismiss();
                                            Toast.makeText(ProfileActivity.this,"Error Occured",Toast.LENGTH_SHORT).show();
                                        }
                                    } );
                        }
                        else{
                            //pd.dismiss();
                            Toast.makeText(ProfileActivity.this,"Some error occured",Toast.LENGTH_SHORT ).show();
                        }
                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //pd.dismiss();
                        Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                } );

    }

    private void pickFromCamera() {

        ContentValues values= new ContentValues( );
        System.out.println( "Camera" );
        values.put( MediaStore.Images.Media.TITLE,"Temp pic");
        values.put( MediaStore.Images.Media.DESCRIPTION,"Temp description");
        image_uri=ProfileActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult( cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }


    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType( "image/*");
        startActivityForResult( galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }


    public void displayData()
    {
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String dp=""+dataSnapshot.child("dp").getValue().toString();
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

                try {
                    if(dp.equals("")) {
                        Picasso.get().load(R.drawable.user_icon).into(profile);
                    }
                    else{
                        Picasso.get().load(dp).into(profile);
                    }
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.user_icon).into(profile);
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
