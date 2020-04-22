package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    FirebaseUser currentUser;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public void sendUserToSignupActvity(View view)
    {
        Intent in = new Intent(MainActivity.this,SignupActivity.class);
        startActivity(in);
        finish();
    }

    public void sendUserToChatMainActivity()
    {
        Intent in = new Intent(MainActivity.this, ChatMainActivity.class);
        //in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(in);
    }

    public void loginCheck(View view)
    {
        EditText email = (EditText)findViewById(R.id.jemail);
        String emailString = email.getText().toString().trim();
        EditText password = (EditText)findViewById(R.id.jpassword);
        String passwordString = password.getText().toString().trim();

        if(emailString.isEmpty() || passwordString.isEmpty())
        {

            Toast.makeText(MainActivity.this, "Please provide both E-mail and Password", Toast.LENGTH_LONG).show();
        }
        else
        {
            loadingBar = new ProgressDialog(this);
            loadingBar.setTitle("Logging In");
            loadingBar.setMessage("Please wait..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(emailString, passwordString)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            loadingBar.dismiss();
                            if (task.isSuccessful()) {
                                currentUser = mAuth.getCurrentUser();
                                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                verifyUserExistance();
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    protected void sendUserToMainActivity()
    {
        Intent in = new Intent(MainActivity.this,MainActivity.class);
        startActivity(in);
    }

    protected void verifyUserExistance()
    {
        String currentUserUid = currentUser.getUid();
        rootRef.child("Users").child(currentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists())
                {
                    if(dataSnapshot.child("name").getValue().toString().isEmpty()) {
                        sendUserToProfileActivity();
                    }
                    else
                    {
                        sendUserToChatMainActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError);
            }
        });
    }

    protected void sendUserToProfileActivity()
    {
        Intent in = new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(in);
        finish();
    }
}
