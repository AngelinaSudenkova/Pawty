package com.example.pawty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.pawty.Adapter.UserAdapter;
import com.example.pawty.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;

    EditText searchUsers;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();
        readUsers();


        return view;
    }


    private void readUsers() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
        DatabaseReference friendsRequestRef = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("FriendRequests").child(firebaseUser.getUid());//child(friendId).child("request_type").setValue("sent");
        friendsRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mUsers.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String friendId = dataSnapshot.getKey().toString();
                        DatabaseReference friendRef = reference.child(friendId);
                        String requestType = snapshot.child(friendId).child("request_type").getValue().toString();
                        if (requestType.equals("received")) {
                            friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        User user = snapshot.getValue(User.class);
                                        assert user != null;
                                        assert firebaseUser != null;
                                        if (!user.getId().equals(firebaseUser.getUid())) {
                                            mUsers.add(user);
                                        }
                                    }
                                    userAdapter = new UserAdapter(getContext(), mUsers, false);
                                    recyclerView.setAdapter(userAdapter);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }

                userAdapter = new UserAdapter(getContext(), mUsers, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}