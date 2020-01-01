package com.example.jchat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ChatAdapter extends ArrayAdapter {

    private final Activity context;
    private final String contactNames[];
    private final String lastMessages[];
    private final String contactImageURLs[];

    public ChatAdapter(Activity context,String contactNames[],String lastMessages[],String contactImageURLs[])
    {
        super(context,R.layout.chat_layout,contactNames);
        this.context = context;
        this.contactNames = contactNames;
        this.lastMessages = lastMessages;
        this.contactImageURLs = contactImageURLs;
    }

    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.chat_layout,null,true);

        TextView contactName = (TextView)rowView.findViewById(R.id.textView1);
        TextView lastMessage = (TextView)rowView.findViewById(R.id.textView2);
        ImageView profilePic = (ImageView)rowView.findViewById(R.id.imageView);
        contactName.setText(contactNames[position]);
        lastMessage.setText(lastMessages[position]);
        profilePic.setImageResource(R.mipmap.ic_launcher_round);
        return rowView;
    }
}
