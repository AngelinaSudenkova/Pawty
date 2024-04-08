package com.example.pawty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawty.Model.User;
import com.example.pawty.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    ActivityMainBinding binding;
    FloatingActionButton bottomButton;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            changeFragment(new HomeFragment());
            binding.bottomNavigation.setBackground(null);
            bottomButton = findViewById(R.id.bottomButton);


           ViewPager viewPager = findViewById(R.id.viewPager);
           ViewPagerAdapter viewPagerAdapter = initializeViewPagerAdapter(viewPager);
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();


           if(user == null){
               Intent intent = new Intent(getApplicationContext(), Login.class);
               startActivity(intent);
               finish();
           }

            reference = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(user.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            bottomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("userid", user.getUid());
                    startActivity(intent);

                }
            });

           binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
               switch(item.getItemId()){
                   case R.id.home:
                       changeFragment(new HomeFragment());
                       break;
                   case R.id.settings:
                       changeFragment(new SettingsFragment());
                       break;

                   case R.id.chats:
                       changeFragment(new ChatsFragment());
                       break;

                   case R.id.friend:
                       changeFragment(new FriendsFragment());
                       break;

               }

               return true;
           });



        }

        private void changeFragment(Fragment fragment){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment);
            fragmentTransaction.commit();

        }

        private ViewPagerAdapter initializeViewPagerAdapter(ViewPager viewPager){
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPagerAdapter.addFragment(new HomeFragment(),"Home");
            viewPagerAdapter.addFragment(new FriendsFragment(),"Friends");
            viewPagerAdapter.addFragment(new ChatsFragment(),"Chats");
            viewPagerAdapter.addFragment(new SettingsFragment(),"Settings");
            viewPager.setAdapter(viewPagerAdapter);

            return viewPagerAdapter;
        }


     class ViewPagerAdapter extends FragmentPagerAdapter {

         private ArrayList<Fragment> fragments;
         private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

         @Nullable
         @Override
         public CharSequence getPageTitle(int position) {
             return titles.get(position);
         }
     }

        private void status(String status){
            reference = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(user.getUid());
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", status);
            reference.updateChildren(hashMap);
        }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
