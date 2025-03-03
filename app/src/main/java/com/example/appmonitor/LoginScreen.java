package com.example.appmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appmonitor.Utilities.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginScreen extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin, btnRegister;
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);
        findViews();
        initViews();
    }


    private void saveInSharedPreference(String username,String password) {
        SharedPreferencesManager.init(this);
        SharedPreferencesManager.getInstance().putString("username",username);
        SharedPreferencesManager.getInstance().putString("password",password);
        SharedPreferencesManager.getInstance().putBoolean("isLoggedIn", true);
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Please enter username and password");
            return;
        }

        databaseRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String dbPassword = dataSnapshot.child("password").getValue(String.class);
                    if (dbPassword != null && dbPassword.equals(password)) {
                        saveInSharedPreference(username,password);
                        navigateToMain(username);
                    } else {
                        showToast("Incorrect password");
                    }
                } else {
                    showToast("User not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Database error: " + databaseError.getMessage());
            }
        });
    }


    private void navigateToMain(String username) {
        Intent intent = new Intent(LoginScreen.this, MainActivity.class);
        intent.putExtra("Username", username);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(LoginScreen.this, message, Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginScreen.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void findViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }
}
