package com.example.pawty;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawty.Model.Coordinates;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    EditText username;
    EditText email;
    EditText password;
    EditText repeatedPassword;
    Button registerButton;
    TextView toLoginActivity;
    TextView errorT;


    FirebaseAuth mAuth;
    DatabaseReference dbReference;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        repeatedPassword = findViewById(R.id.repeatPassword);
        registerButton = findViewById(R.id.registerButton);
        toLoginActivity = findViewById(R.id.loginText);
        errorT = findViewById(R.id.error);




        toLoginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailTxt = email.getText().toString().trim();
                String passwordTxt = password.getText().toString().trim();
                String repeatedPasswordTxt = repeatedPassword.getText().toString().trim();



                if(TextUtils.isEmpty(emailTxt)){
                    errorT.setText("Please enter email");
                    return;

                }

                if(TextUtils.isEmpty(passwordTxt) || TextUtils.isEmpty(repeatedPasswordTxt)){
                    errorT.setText("Please enter password");
                    return;

                }

                if(!validatePasswords(passwordTxt, repeatedPasswordTxt)){
                    errorT.setText("Please put two equal values for passwords");
                    return;
                }

                mAuth.createUserWithEmailAndPassword(emailTxt, passwordTxt)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    String usernameTxt = username.getText().toString().trim();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String userId = user.getUid();
                                    dbReference = FirebaseDatabase.getInstance("https://pawty-db5ff-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child(userId);

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("id", userId);
                                    hashMap.put("username", usernameTxt);
                                    hashMap.put("imageURL","default");
                                    hashMap.put("status", "offline");
                                    hashMap.put("search", usernameTxt.toLowerCase());
                                    hashMap.put("coordinates", new Coordinates(0,0));

                                    dbReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });
                                    Toast.makeText(Register.this, "Account has been created",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    private boolean validatePasswords(String expectedPass, String repeatedPass){
        if(expectedPass.equals(repeatedPass)) return true;
        return false;
    }
}