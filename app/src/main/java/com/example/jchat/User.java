package com.example.jchat;

public class User{
    public String uid,name,status,email,friends,language,dp,pass_code;

    public User(String uid,String name,String status,String email,String friends,String language,String dp)
    {
        this.uid = uid;
        this.name = name;
        this.status = status;
        this.email = email;
        this.friends = friends;
        this.language = language;
        this.dp = dp;
        pass_code = "";
    }
}

