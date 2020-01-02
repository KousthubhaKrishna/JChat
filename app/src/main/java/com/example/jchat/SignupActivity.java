package com.example.jchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
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

            mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loadingBar.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(SignupActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        }
                    });
        }

    }

    public void back(View view)
    {
        Intent in = new Intent(SignupActivity.this,MainActivity.class);
        startActivity(in);
    }
}
