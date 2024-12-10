package com.chautari.admin_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chautari.admin_app.models.Admin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, repeatPasswordEditText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String selectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Admins");

        //check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            Toast.makeText(this, "You are already logged in!", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }


        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);
        Spinner positionSpinner = findViewById(R.id.spinner_position);
        Button signupButton = findViewById(R.id.signupButton);
        TextView loginTextView = findViewById(R.id.loginTextView);
        progressBar = findViewById(R.id.progressBar);

        List<String> positionOptions = Arrays.asList(
                "Select", "Founder", "CEO", "Accountant", "Manager", "Assistant Manager", "Receptionist", "Other"
        );

        ArrayAdapter<String> adapter = getStringArrayAdapter(positionOptions);
        positionSpinner.setAdapter(adapter);

        positionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPosition = "";
            }
        });

        signupButton.setOnClickListener(v -> registerAdmin());
        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter(List<String> positionOptions) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, positionOptions) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.BLACK);
                view.setBackgroundColor(Color.WHITE);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void registerAdmin() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
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
        if (TextUtils.isEmpty(selectedPosition) || selectedPosition.equals("Select")) {
            Toast.makeText(this, "Please select a position", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                String registrationDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                // Remove password from adminData
                Admin adminData = new Admin(username, email, selectedPosition, password, registrationDate);

                mDatabase.child(userId).setValue(adminData).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        String errorMessage = task1.getException() != null ? task1.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Failed to save admin data: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

    }
}
