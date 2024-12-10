package com.chautari.user_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.chautari.user_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KYCActivity extends AppCompatActivity {
    private EditText fullNameEditText, emailEditText, phoneNumberEditText, dateOfBirthEditText, addressEditText;
    private RadioGroup genderRadioGroup;
    private DatabaseReference databaseReference;
    private Button submitButton;
    private ImageView profileImageView;
    private Uri imageUri;
    private ProgressBar progressBar;
    private TextView usernameTextView;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kyc);

        initializeViews();
        initializeImagePickerLauncher();
        checkIfUserIsAdmin();

        submitButton.setOnClickListener(v -> submitKYCForm());
    }

    private void initializeViews() {
        fullNameEditText = findViewById(R.id.full_name);
        emailEditText = findViewById(R.id.email);
        phoneNumberEditText = findViewById(R.id.phone_number);
        dateOfBirthEditText = findViewById(R.id.date_of_birth);
        addressEditText = findViewById(R.id.address);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        submitButton = findViewById(R.id.submit_button);
        profileImageView = findViewById(R.id.profile_picture);
        progressBar = findViewById(R.id.progressBar);
        Button uploadPictureButton = findViewById(R.id.upload_picture_button);
        CheckBox termsCheckbox = findViewById(R.id.terms_checkbox);
        ImageView backArrow = findViewById(R.id.backArrow);
        usernameTextView = findViewById(R.id.username_title);

        backArrow.setOnClickListener(v -> finish());
        submitButton.setEnabled(false);

        termsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showTermsAndConditionsDialog();
            } else {
                submitButton.setEnabled(false);
            }
        });

        uploadPictureButton.setOnClickListener(v -> pickImage());
    }

    private void initializeImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                if (!isDestroyed() && !isFinishing()) {
                    Glide.with(this)
                            .load(imageUri)  // Or your desired image source
                            .transform(new CircleCrop())
                            .into(profileImageView);
                }
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void checkIfUserIsAdmin() {
        String userEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();

        // Reference to both Users and Admins based on the email
        Query userRef = FirebaseDatabase.getInstance().getReference("Users").orderByChild("email").equalTo(userEmail);
        Query adminRef = FirebaseDatabase.getInstance().getReference("Admins").orderByChild("email").equalTo(userEmail);

        // Start showing the progress bar while loading the data
        progressBar.setVisibility(View.VISIBLE);

        // Check if the user is an admin or a regular user
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // If the user is an admin
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference("Admins").child(userId);
                    loadUserData();  // Load data for admin
                } else {
                    // If the user is not an admin, check if they are a regular user
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // If the user is a regular user
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                                loadUserData();  // Load data for regular user
                            } else {
                                // Handle case where user is not found in either Admins or Users
                                progressBar.setVisibility(View.GONE);  // Hide progress bar
                                Toast.makeText(KYCActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressBar.setVisibility(View.GONE);  // Hide progress bar
                            Toast.makeText(KYCActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);  // Hide progress bar
                Toast.makeText(KYCActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        populateUserData(user);
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

    private void populateUserData(User user) {
        usernameTextView.setText(String.format("Howdey! %s", user.username));
        fullNameEditText.setText(user.fullName);
        emailEditText.setText(user.email);
        phoneNumberEditText.setText(user.contactNumber);
        dateOfBirthEditText.setText(user.dateOfBirth);
        addressEditText.setText(user.address);

        String gender = user.gender;
        if ("Male".equals(gender)) {
            genderRadioGroup.check(R.id.radio_male);
        } else if ("Female".equals(gender)) {
            genderRadioGroup.check(R.id.radio_female);
        } else {
            genderRadioGroup.check(R.id.radio_other);
        }

        if (user.profileImageUrl != null) {
            byte[] decodedString = Base64.decode(user.profileImageUrl, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(KYCActivity.this).load(decodedBitmap).transform(new CircleCrop()).into(profileImageView);
        }
    }

    private void submitKYCForm() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String contactNumber = phoneNumberEditText.getText().toString().trim();
        String dateOfBirth = dateOfBirthEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String gender = getSelectedGender();

        if (fullName.isEmpty() || email.isEmpty() || contactNumber.isEmpty() || dateOfBirth.isEmpty() || address.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showPasswordDialog(fullName, email, contactNumber, dateOfBirth, address, gender);
    }

    private void showPasswordDialog(String fullName, String email, String contactNumber, String dateOfBirth, String address, String gender) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your password");
        final EditText passwordEditText = new EditText(this);
        passwordEditText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
        builder.setView(passwordEditText);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String password = passwordEditText.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            } else {
                uploadUserData(fullName, email, contactNumber, dateOfBirth, address, gender);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void uploadUserData(String fullName, String email, String contactNumber, String dateOfBirth, String address, String gender) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("fullName", fullName);
        userUpdates.put("email", email);
        userUpdates.put("contactNumber", contactNumber);
        userUpdates.put("dateOfBirth", dateOfBirth);
        userUpdates.put("address", address);
        userUpdates.put("gender", gender);

        if (imageUri != null) {
            // If a new image is selected, convert it to Base64 and update the profile image
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                Toast.makeText(KYCActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
            if (bitmap != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                userUpdates.put("profileImageUrl", encodedImage);
            }
        }

        databaseReference.updateChildren(userUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(KYCActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(KYCActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTermsAndConditionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms and Conditions").setMessage("Welcome to Cautari Oil Mill. By using our services, you agree to comply with and be bound by the following terms and conditions.\n\n" + "1. Acceptance of Terms\n" + "By accessing or using Cautari Oil Mill, you confirm that you have read, understood, and agreed to these Terms and Conditions. If you do not agree, you may not use our services.\n\n" + "2. User Accounts\n" + "You may be required to register for an account to access certain features. You are responsible for maintaining the confidentiality of your account information and for all activities that occur under your account.\n\n" + "3. Privacy Policy\n" + "Your privacy is important to us. Please review our Privacy Policy to understand how we collect, use, and protect your information.\n\n" + "4. Product Information and Pricing\n" + "We strive to ensure accuracy in product descriptions, prices, and availability. Prices and availability of products are subject to change without notice.\n\n" + "5. Order Processing\n" + "Your order is considered accepted once you receive an order confirmation. We reserve the right to cancel or refuse any order.\n\n" + "6. Payment\n" + "We accept multiple payment methods. You agree to pay for all items you purchase, including applicable taxes and shipping charges.\n\n" + "7. Shipping and Delivery\n" + "Delivery times may vary based on your location. We aim to deliver within the estimated time frame, but are not responsible for delays.\n\n" + "8. Returns and Refunds\n" + "Please refer to our Return and Refund Policy. Items must be returned in original condition within the specified period.\n\n" + "9. Intellectual Property\n" + "All content and materials on Cautari Oil Mill are our intellectual property. You may not use or reproduce any content without consent.\n\n" + "10. Limitation of Liability\n" + "We are not liable for any indirect, incidental, or consequential damages arising from your use of our services.\n\n" + "11. Changes to Terms and Conditions\n" + "We reserve the right to update these terms at any time. Continued use after changes constitutes acceptance of the new terms.\n\n" + "12. Contact Us\n" + "If you have questions regarding these Terms and Conditions, please contact us at [mainalisakaar222@gmail.com].\n\n" + "Thank you for choosing Cautari Oil Mill. We hope you enjoy your shopping experience!").setPositiveButton("Accept", (dialog, which) -> submitButton.setEnabled(true)).setNegativeButton("Decline", (dialog, which) -> {
            CheckBox termsCheckbox = findViewById(R.id.terms_checkbox);
            termsCheckbox.setChecked(false);
            submitButton.setEnabled(false);
        }).show();
    }

    private String getSelectedGender() {
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_male) {
            return "Male";
        } else if (selectedId == R.id.radio_female) {
            return "Female";
        } else if (selectedId == R.id.radio_other) {
            return "Other";
        } else {
            return "";
        }
    }
}