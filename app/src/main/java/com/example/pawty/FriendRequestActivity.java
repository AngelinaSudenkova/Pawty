package com.example.pawty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pawty.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestActivity extends AppCompatActivity {

    TextView username;
    Button buttonAdd, buttonSent, buttonCancel, sendMessageButton;
    CircleImageView profileImage;
    DatabaseReference reference, friendRequestRef, friendsRef;
    FirebaseAuth mAuth;

    String CURRENT_STATE;

    String friendId, senderId, saveCurrentDate;
    User friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        username = findViewById(R.id.username);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSent = findViewById(R.id.buttonSent);
        profileImage = findViewById(R.id.profileImage);
        sendMessageButton = findViewById(R.id.buttonSendMessage);
        CURRENT_STATE = "not_friends";
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        friendId = intent.getStringExtra("userid");
        senderId = mAuth.getCurrentUser().getUid();

        friendsRef = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Friends");
        friendRequestRef = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("FriendRequests");
        reference = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(friendId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                friend = snapshot.getValue(User.class);
                username.setText(friend.getUsername());
                if(friend.getImageURL().equals("default")){
                    profileImage.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(getApplicationContext()).load(friend.getImageURL()).into(profileImage);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFullScreenImage();
            }
        });


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMessageActivity();
            }
        });

        buttonCancel.setVisibility(View.GONE);
        buttonCancel.setEnabled(false);

        if(!senderId.equals(friendId)){
            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buttonAdd.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){
                        buttonSent.setVisibility(View.GONE);
                        buttonCancel.setVisibility(View.GONE);
                        sendFriendRequest();
                    }
                }
            });
        }else{
            buttonSent.setVisibility(View.GONE);
            buttonCancel.setVisibility(View.GONE);
            buttonAdd.setVisibility(View.GONE);
        }

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CURRENT_STATE.equals("request_sent")){
                    cancelFriendRequest();
                }
                if(CURRENT_STATE.equals("friends")){
                    deleteFromFriends();
                }
            }
        });

        buttonSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CURRENT_STATE.equals("request_received")){
                    acceptFriendRequest();
                }
            }
        });


        maintainButtons();


    }

    private void maintainButtons(){
        friendRequestRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(friendId)){
                    String requestType = snapshot.child(friendId).child("request_type").getValue().toString();
                    if(requestType.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        buttonAdd.setVisibility(View.GONE);
                        buttonSent.setVisibility(View.VISIBLE);
                        buttonSent.setEnabled(false);
                        buttonCancel.setVisibility(View.VISIBLE);
                        buttonCancel.setEnabled(true);

                    } else if (requestType.equals("received")) {
                        CURRENT_STATE = "request_received";
                        buttonAdd.setVisibility(View.GONE);
                        buttonAdd.setEnabled(false);             buttonSent.setText("Accept a friend request");
                        buttonSent.setVisibility(View.VISIBLE);
                        buttonSent.setEnabled(true);
                        int redColor = ContextCompat.getColor(FriendRequestActivity.this, R.color.red);
                        buttonCancel.setBackgroundColor(redColor);
                        buttonCancel.setText("Decline a friend request");
                        buttonCancel.setVisibility(View.VISIBLE);
                        buttonCancel.setEnabled(true);

                        buttonCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(CURRENT_STATE.equals("request_received")){
                                    cancelFriendRequest();
                                }else if(CURRENT_STATE.equals("friends")){
                                    deleteFromFriends();
                                }

                            }
                        });
                    }
                }else{
                    friendsRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(friendId)){
                                CURRENT_STATE = "friends";
                                buttonSent.setText("You are friends now!");
                                buttonSent.setVisibility(View.VISIBLE);
                                buttonSent.setEnabled(false);
                                buttonAdd.setVisibility(View.GONE);
                                buttonAdd.setEnabled(false);
                                buttonCancel.setText("Delete from friends");
                                buttonCancel.setVisibility(View.VISIBLE);
                                buttonCancel.setEnabled(true);
                            } else {
                                CURRENT_STATE = "not_friends";
                                buttonAdd.setVisibility(View.VISIBLE);
                                buttonSent.setVisibility(View.GONE);
                                buttonSent.setEnabled(false);
                                buttonCancel.setVisibility(View.GONE);
                                buttonCancel.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openFullScreenImage() {
        // Create a new instance of the FullScreenImageFragment
        FullImageFragment fragment = new FullImageFragment();

        // Pass the image URL or resource to the fragment
        Bundle args = new Bundle();
        if (friend != null && !friend.getImageURL().equals("default")) {
            args.putString("imageUrl", friend.getImageURL());
        } else {
            args.putInt("imageRes", R.mipmap.icon);
        }
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void goToMessageActivity(){
        Intent intent = new Intent(FriendRequestActivity.this, MessageActivity.class);
        intent.putExtra("userid", friend.getId());
        startActivity(intent);
    }

    private void sendFriendRequest(){
    friendRequestRef.child(senderId).child(friendId)
            .child("request_type").setValue("sent")
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        friendRequestRef.child(friendId).child(senderId)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            buttonAdd.setVisibility(View.GONE);
                                            CURRENT_STATE = "request_sent";
                                            buttonSent.setVisibility(View.VISIBLE);
                                            buttonSent.setEnabled(false);
                                            buttonCancel.setVisibility(View.VISIBLE);
                                            buttonCancel.setEnabled(true);
                                        }
                                    }
                                });
                    }
                }
            });
    }

    private void cancelFriendRequest(){
        friendRequestRef.child(senderId).child(friendId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(friendId).child(senderId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                buttonCancel.setVisibility(View.GONE);
                                                CURRENT_STATE = "not_friends";
                                                buttonAdd.setVisibility(View.VISIBLE);
                                                buttonAdd.setEnabled(true);
                                                buttonSent.setVisibility(View.GONE);
                                                buttonCancel.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void acceptFriendRequest(){
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        friendsRef.child(senderId).child(friendId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendsRef.child(friendId).child(senderId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                friendRequestRef.child(senderId).child(friendId)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    friendRequestRef.child(friendId).child(senderId)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        buttonSent.setText("You are friends now!");
                                                                        buttonSent.setVisibility(View.VISIBLE);
                                                                        buttonSent.setEnabled(false);
                                                                        CURRENT_STATE = "friends";
                                                                        buttonAdd.setVisibility(View.GONE);
                                                                        buttonAdd.setEnabled(false);
                                                                        buttonCancel.setText("Delete from friends");
                                                                        buttonCancel.setVisibility(View.VISIBLE);
                                                                        buttonCancel.setEnabled(true);
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });

    }

    private void deleteFromFriends(){
        friendsRef.child(senderId).child(friendId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(friendId).child(senderId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                buttonCancel.setVisibility(View.GONE);
                                                CURRENT_STATE = "not_friends";
                                                buttonAdd.setVisibility(View.VISIBLE);
                                                buttonAdd.setEnabled(true);
                                                buttonSent.setVisibility(View.GONE);
                                                buttonCancel.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}