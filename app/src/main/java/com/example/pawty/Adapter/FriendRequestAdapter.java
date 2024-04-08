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

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {



        private Context mContext;
        private List<User> mUsers;

    public FriendRequestAdapter() {

    }



    public FriendRequestAdapter(Context mContext, List<User> mUsers, boolean isChat) {
            this.mContext = mContext;
            this.mUsers = mUsers;
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
            return new FriendRequestAdapter.ViewHolder(view);
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


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Intent intent = new Intent(mContext, FriendRequestActivity.class);
                        intent.putExtra("userid", user.getId());
                        mContext.startActivity(intent);
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

            public ViewHolder(View itemView){
                super(itemView);

                username = itemView.findViewById(R.id.usernameF);
                profileImage = itemView.findViewById(R.id.profileImage);

            }
        }

    }


