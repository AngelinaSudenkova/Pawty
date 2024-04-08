package com.example.pawty.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pawty.FriendRequestActivity;
import com.example.pawty.MessageActivity;
import com.example.pawty.Model.Chat;
import com.example.pawty.Model.User;
import com.example.pawty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;
    String lastMessageTxt;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            holder.profileImage.setImageResource(R.mipmap.icon);
        }else{
            Glide.with(mContext).load(user.getImageURL()).into(holder.profileImage);
        }

        if(isChat){
            checkLastMessage(user.getId(), holder.lastMessage);
        }else{
            holder.lastMessage.setVisibility(View.GONE);
        }


        if(isChat){
            if ((user.getStatus().equals("online"))){
                holder.imageOn.setVisibility(View.VISIBLE);
                holder.imageOff.setVisibility(View.GONE);
            }else{
                holder.imageOn.setVisibility(View.GONE);
                holder.imageOff.setVisibility(View.VISIBLE);
            }
        }else{
            holder.imageOn.setVisibility(View.GONE);
            holder.imageOff.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isChat) {
                    Intent intent = new Intent(mContext, MessageActivity.class);
                    intent.putExtra("userid", user.getId());
                    mContext.startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, FriendRequestActivity.class);
                    intent.putExtra("userid", user.getId());
                    mContext.startActivity(intent);
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView profileImage;
        private ImageView imageOn;
        private ImageView imageOff;
        private TextView lastMessage;

        public ViewHolder(View itemView){
            super(itemView);

            username = itemView.findViewById(R.id.usernameF);
            profileImage = itemView.findViewById(R.id.profileImage);
            imageOn = itemView.findViewById(R.id.imgOn);
            imageOff = itemView.findViewById(R.id.imgOff);
            lastMessage = itemView.findViewById(R.id.lastMessage);

        }
    }

    private void checkLastMessage(String userid, TextView lastMessage){
        lastMessageTxt = "default";
        FirebaseUser fuser  = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Chat chat = snapshot1.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(fuser.getUid())
                    ) {
                        lastMessageTxt = chat.getMessage();

                    }

                }

                switch (lastMessageTxt){
                    case "default" :
                        lastMessage.setText("No message");
                        break;
                    default:
                        lastMessage.setText(lastMessageTxt);
                        break;

                }
                lastMessageTxt = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
