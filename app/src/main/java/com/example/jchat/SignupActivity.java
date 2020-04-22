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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    DatabaseReference rootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public void createUser(View view)
    {
        EditText email = (EditText)findViewById(R.id.jemail);
        String emailString = email.getText().toString();
        EditText password = (EditText)findViewById(R.id.jpassword);
        String passwordString = password.getText().toString();

        if(emailString.isEmpty() || passwordString.isEmpty() || emailString==null || passwordString == null)
        {
            Toast.makeText(SignupActivity.this, "Please provide both E-mail and Password", Toast.LENGTH_LONG).show();
        }
        else
        {
            loadingBar = new ProgressDialog(this);
            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Please wait..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            loadingBar.dismiss();
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                String userId = user.getUid();
                                rootRef.child("Users").child(userId).setValue(new User(user.getUid(),"","",user.getEmail(),"","11",""));
                                Toast.makeText(SignupActivity.this, "Account Created", Toast.LENGTH_LONG).show();
                                sendUserToMainActivity(null);
                            } else {
                                Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public void sendUserToMainActivity(View view)
    {
        Intent in = new Intent(SignupActivity.this,MainActivity.class);
        startActivity(in);
        finish();
    }
}


