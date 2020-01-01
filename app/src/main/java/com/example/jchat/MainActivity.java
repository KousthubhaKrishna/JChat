package com.example.jchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void loginCheck(View view)
    {
        EditText email = (EditText)findViewById(R.id.jemail);
        String emailString = email.getText().toString();
        EditText password = (EditText)findViewById(R.id.jpassword);
        String passwordString = password.getText().toString();
        if(emailString.equals("") && passwordString.equals(""))
        {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            Intent in = new Intent(this,ChatActivity.class);
            Log.i("Error Check","Entered");
            startActivity(in);
        }
        else
        {
            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
        }
    }
}
