package com.example.pawty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pawty.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestActivity extends AppCompatActivity {

    TextView username;
    Button buttonAdd, buttonSent, buttonCancel;
    CircleImageView profileImage;
    DatabaseReference reference;

    String friendId;
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

        Intent intent = getIntent();
        friendId = intent.getStringExtra("userid");

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



    }

    private void openFullScreenImage() {
        // Create a new instance of the FullScreenImageFragment
        FullImageFragment fragment = new FullImageFragment();

        // Pass the image URL or resource to the fragment
        Bundle args = new Bundle();
        if (friend != null && !friend.getImageURL().equals("default")) {
            args.putString("imageUrl", friend.getImageURL());
        } else {
            args.putInt("imageRes", R.mipmap.icon); // Replace with your default image resource
        }
        fragment.setArguments(args);

        // Replace the fragment container with the FullScreenImageFragment
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment) // Use android.R.id.content to replace the whole activity's layout
                .addToBackStack(null)
                .commit();
    }
}