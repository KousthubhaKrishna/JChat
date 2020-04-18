package com.example.jchat;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    public String mes,dateString,timeString,senderUId,receiverUid,send_mes,rec_mes;
    Date date;

    public Message()
    {}

    public Message(String send_mes,Date date,String dateString,String timeString,String senderUId,String receiverUid)
    {
        this.send_mes = send_mes;
        this.date = date;
        this.dateString = dateString;
        this.timeString = timeString;
        this.senderUId = senderUId;
        this.receiverUid = receiverUid;
    }

}

