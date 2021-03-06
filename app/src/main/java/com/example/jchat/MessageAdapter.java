package com.example.jchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> userMessagesList;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String currentUserId;
    String chatid;
    View rootView;

    public MessageAdapter(List<Message> userMessagesList,String chatid)
    {
        this.chatid = chatid;
        this.userMessagesList=userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessage,receiverMessage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessage = (TextView)itemView.findViewById(R.id.sent_message);
            receiverMessage = (TextView)itemView.findViewById(R.id.received_message);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        rootView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_message_layout,parent,false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        return new MessageViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        final Message message = userMessagesList.get(position);
        if(message.senderUId.equals(currentUserId) )
        {
            holder.senderMessage.setText(message.send_mes);
            holder.senderMessage.setVisibility(View.VISIBLE);
            holder.receiverMessage.setVisibility(View.INVISIBLE);
        }
        else
        {
            holder.receiverMessage.setText(message.rec_mes);
            holder.receiverMessage.setVisibility(View.VISIBLE);
            holder.senderMessage.setVisibility(View.INVISIBLE);
        }
        holder.senderMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                rootRef.child("Chats").child(chatid).child(message.mid).setValue(null);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

}
