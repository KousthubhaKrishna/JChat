package com.example.jchat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    public String mes,dateString,timeString,senderUId,receiverUid;
    Date date;

    public Message()
    {}

    public Message(String mes,Date date,String dateString,String timeString,String senderUId,String receiverUid)
    {
        this.mes = mes;
        this.date = date;
        this.dateString = dateString;
        this.timeString = timeString;
        this.senderUId = senderUId;
        this.receiverUid = receiverUid;
    }
}
