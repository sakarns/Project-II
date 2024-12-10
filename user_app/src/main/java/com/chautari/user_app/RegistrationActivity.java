package com.chautari.user_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chautari.user_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private EditText usernameEditText, contactNumberEditText, emailEditText, passwordEditText, repeatPasswordEditText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        contactNumberEditText = findViewById(R.id.contactNumberEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);
        Button signupButton = findViewById(R.id.signupButton);
        TextView loginTextView = findViewById(R.id.loginTextView);
        progressBar = findViewById(R.id.progressBar);

        // Registration button logic
        signupButton.setOnClickListener(v -> registerUser());

        // Redirect to Login page if no account
        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    // Register user with Firebase Auth and Realtime Database
    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String contactNumber = contactNumberEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            return;
        }
        if (TextUtils.isEmpty(contactNumber)) {
            contactNumberEditText.setError("Contact Number is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }
        if (!password.equals(repeatPassword)) {
            repeatPasswordEditText.setError("Passwords do not match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Firebase Authentication: Check if already logged in
        if (mAuth.getCurrentUser() != null) {
            // User is already logged in
            Toast.makeText(this, "You are already logged in!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
            finish();
            return;
        }

        // Firebase Authentication: Create User with Email and Password
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                // Registration success, now store additional user data in Firebase Realtime Database
                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid(); // Get current user ID

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String registrationDate = dateFormat.format(new Date());

                // Create a new User object with the registration date
                User userData = new User(username, contactNumber, email, password, registrationDate);

                // Save user data in Firebase Realtime Database
                mDatabase.child(userId).setValue(userData).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Failed to save user data!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Registration failed
                Toast.makeText(RegistrationActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
