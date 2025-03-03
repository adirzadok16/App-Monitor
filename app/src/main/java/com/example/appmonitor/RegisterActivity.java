package com.example.appmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appmonitor.Utilities.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername, editTextPassword , editTextFullName;
    private MaterialButton btnRegister , bthBack;
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Users");





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        findViews();
        initViews();
    }

    private void initViews() {
        btnRegister.setOnClickListener(v->{
            User user = new User(editTextFullName.getText().toString() ,editTextUsername.getText().toString() ,editTextPassword.getText().toString());
            databaseRef.child(user.getUsername()).setValue(user)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FirebaseSave", "App added successfully: " + user.getName());
                        } else {
                            Log.e("FirebaseSave", "Error adding app: " + task.getException().getMessage());
                        }
                    });
            backToLogin();
        });


        bthBack.setOnClickListener(v->{
           backToLogin();
        });
    }

    private void backToLogin(){
        Intent intent = new Intent(RegisterActivity.this, LoginScreen.class);
        startActivity(intent);
    }

    private void findViews() {
        editTextUsername = findViewById(R.id.register_ET_username);
        editTextPassword = findViewById(R.id.register_ET_password);
        btnRegister = findViewById(R.id.register_MB_register);
        editTextFullName = findViewById(R.id.register_ET_name);
        bthBack = findViewById(R.id.register_MB_back);
    }
}