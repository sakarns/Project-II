package com.chautari.admin_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.chautari.admin_app.models.Admin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class KYCActivity extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, phoneNumberEditText, dateOfBirthEditText, addressEditText;
    private RadioGroup genderRadioGroup;
    private Spinner positionSpinner;
    private DatabaseReference databaseReference;
    private Button submitButton;
    private ImageView profileImageView;
    private Uri imageUri;
    private ProgressBar progressBar;
    private TextView usernameTextView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Admin currentAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kyc);

        initializeViews();
        initializeFirebaseReference();
        setupImagePicker();
        setupPositionSpinner();
        loadUserData();

        submitButton.setOnClickListener(v -> submitKYCForm());
    }

    private void initializeViews() {
        fullNameEditText = findViewById(R.id.full_name);
        emailEditText = findViewById(R.id.email);
        phoneNumberEditText = findViewById(R.id.phone_number);
        dateOfBirthEditText = findViewById(R.id.date_of_birth);
        addressEditText = findViewById(R.id.address);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        positionSpinner = findViewById(R.id.spinner_position);
        submitButton = findViewById(R.id.submit_button);
        profileImageView = findViewById(R.id.profile_picture);
        progressBar = findViewById(R.id.progressBar);
        Button uploadPictureButton = findViewById(R.id.upload_picture_button);
        CheckBox termsCheckbox = findViewById(R.id.terms_checkbox);
        ImageView backArrow = findViewById(R.id.backArrow);
        usernameTextView = findViewById(R.id.username_title);

        backArrow.setOnClickListener(v -> finish());
        submitButton.setEnabled(false);

        termsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> submitButton.setEnabled(isChecked));

        uploadPictureButton.setOnClickListener(v -> openImagePicker());
    }

    private void initializeFirebaseReference() {
        String adminId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Admins").child(adminId);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Glide.with(KYCActivity.this)
                                .load(imageUri)
                                .transform(new CircleCrop())
                                .into(profileImageView);
                    }
                });
    }

    private void setupPositionSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.position_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(adapter);
        // Set the default selection to the first item, which is "Select"
        positionSpinner.setSelection(0);
        positionSpinner.setBackgroundColor(Color.BLACK);
    }


    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentAdmin = dataSnapshot.getValue(Admin.class);
                    if (currentAdmin != null) {
                        populateUserData(currentAdmin);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(KYCActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(Admin admin) {
        usernameTextView.setText(String.format("Howdy! %s", admin.username));
        fullNameEditText.setText(admin.fullName);
        emailEditText.setText(admin.email);
        phoneNumberEditText.setText(admin.contactNumber);
        dateOfBirthEditText.setText(admin.dateOfBirth);
        addressEditText.setText(admin.address);

        String gender = admin.gender;
        if ("Male".equals(gender)) {
            genderRadioGroup.check(R.id.radio_male);
        } else if ("Female".equals(gender)) {
            genderRadioGroup.check(R.id.radio_female);
        } else {
            genderRadioGroup.check(R.id.radio_none);
        }

        if (admin.profileImageUrl != null) {
            byte[] decodedString = Base64.decode(admin.profileImageUrl, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(KYCActivity.this)
                    .load(decodedBitmap)
                    .transform(new CircleCrop())
                    .into(profileImageView);
        }
    }

    private void submitKYCForm() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String contactNumber = phoneNumberEditText.getText().toString().trim();
        String dateOfBirth = dateOfBirthEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String gender = getSelectedGender();
        String position = positionSpinner.getSelectedItem().toString();

        if (fullName.isEmpty() || email.isEmpty() || contactNumber.isEmpty() || dateOfBirth.isEmpty() || address.isEmpty() || gender.isEmpty() || "Select".equals(position)) {
            Toast.makeText(this, "Please fill all fields and select a valid position", Toast.LENGTH_SHORT).show();
            return;
        }

        showPasswordDialog(fullName, email, contactNumber, dateOfBirth, address, gender, position);
    }

    private void showPasswordDialog(String fullName, String email, String contactNumber, String dateOfBirth, String address, String gender, String position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");

        final EditText passwordInput = new EditText(this);
        builder.setView(passwordInput);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();
            validatePassword(password, fullName, contactNumber, email, dateOfBirth, address, gender, position);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void validatePassword(String password, String fullName, String contactNumber, String email, String dateOfBirth, String address, String gender, String position) {
        databaseReference.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String storedPassword = dataSnapshot.getValue(String.class);
                if (storedPassword != null && storedPassword.equals(password)) {
                    if (imageUri != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        uploadProfileImageToDatabase(imageUri, fullName, contactNumber, email, dateOfBirth, address, gender, position);
                    } else {
                        updateAdminData(fullName, contactNumber, email, dateOfBirth, address, gender, position, null);
                    }
                } else {
                    Toast.makeText(KYCActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(KYCActivity.this, "Failed to validate password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProfileImageToDatabase(Uri imageUri, String fullName, String contactNumber, String email, String dateOfBirth, String address, String gender, String position) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            String profileImageUrl = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            updateAdminData(fullName, contactNumber, email, dateOfBirth, address, gender, position, profileImageUrl);
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(KYCActivity.this, "Failed to upload profile image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAdminData(String fullName, String contactNumber, String email, String dateOfBirth, String address, String gender, String position, String profileImageUrl) {
        if (profileImageUrl != null) {
            databaseReference.child("profileImageUrl").setValue(profileImageUrl);
        }

        databaseReference.child("fullName").setValue(fullName);
        databaseReference.child("contactNumber").setValue(contactNumber);
        databaseReference.child("email").setValue(email);
        databaseReference.child("dateOfBirth").setValue(dateOfBirth);
        databaseReference.child("address").setValue(address);
        databaseReference.child("gender").setValue(gender);
        databaseReference.child("position").setValue(position);

        progressBar.setVisibility(View.GONE);
        Toast.makeText(KYCActivity.this, "KYC data updated successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(KYCActivity.this, HomeActivity.class));
        finish();
    }

    private String getSelectedGender() {
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.radio_male) {
            return "Male";
        } else if (selectedGenderId == R.id.radio_female) {
            return "Female";
        } else if (selectedGenderId == R.id.radio_none) {
            return "None";
        }
        return "";
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
}
