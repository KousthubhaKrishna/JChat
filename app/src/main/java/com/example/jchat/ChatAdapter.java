package com.example.jchat;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatAdapter extends ArrayAdapter {

    private final Activity context;
    private final String contactNames[];
    private final String lastMessages[];
    private final String contactImageURLs[];

    public ChatAdapter(Activity context,String contactNames[],String lastMessages[],String contactImageURLs[])
    {
        super(context,R.layout.activity_chat_main,contactNames);
        this.context = context;
        this.contactNames = contactNames;
        this.lastMessages = lastMessages;
        this.contactImageURLs = contactImageURLs;
    }

    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.chat_layout,null,true);

        TextView contactName = (TextView)rowView.findViewById(R.id.message_name);
        TextView lastMessage = (TextView)rowView.findViewById(R.id.message_context);
        ImageView profilePic = (ImageView)rowView.findViewById(R.id.message_pic);
        contactName.setText(contactNames[position]);
        lastMessage.setText(lastMessages[position]);
        profilePic.setImageResource(R.drawable.user_icon);
        return rowView;
    }
}
