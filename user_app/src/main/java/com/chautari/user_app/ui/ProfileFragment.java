package com.chautari.user_app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.chautari.user_app.KYCActivity;
import com.chautari.user_app.R;
import com.chautari.user_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private ImageView profilePicture;
    private TextView fullName;
    private TextView email;
    private TextView dateOfBirth;
    private TextView gender;
    private TextView contactNumber;
    private TextView registrationDate;
    private TextView welcomeMessage;
    private TextView address;
    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        profilePicture = view.findViewById(R.id.profile_picture);
        fullName = view.findViewById(R.id.full_name);
        email = view.findViewById(R.id.email);
        dateOfBirth = view.findViewById(R.id.date_of_birth);
        gender = view.findViewById(R.id.gender);
        contactNumber = view.findViewById(R.id.contact_number);
        registrationDate = view.findViewById(R.id.registration_date);
        address = view.findViewById(R.id.address);
        Button editProfileButton = view.findViewById(R.id.edit_profile_button);
        welcomeMessage = view.findViewById(R.id.welcome_message);

        // Get the current Firebase user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Get the current user's UID
            String userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            // Load user data
            loadUserData();

            // Set edit button to open KYCActivity
            editProfileButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), KYCActivity.class);
                startActivity(intent);
            });
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadUserData() {

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve and display user data
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        // Display user details
                        fullName.setText(user.fullName);
                        email.setText(user.email);
                        dateOfBirth.setText(user.dateOfBirth);
                        gender.setText(user.gender);
                        contactNumber.setText(user.contactNumber);
                        address.setText(user.address);
                        registrationDate.setText(String.format("You are a registered user since: %s", user.registrationDate));

                        // Display the username in the welcome message
                        welcomeMessage.setText(String.format(user.username));

                        // Decode and display profile image
                        if (user.profileImageUrl != null) {
                            try {
                                byte[] decodedString = Base64.decode(user.profileImageUrl, Base64.DEFAULT);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                Glide.with(ProfileFragment.this)
                                        .load(decodedBitmap)
                                        .transform(new CircleCrop())
                                        .into(profilePicture);
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Error loading profile image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        // Add the listener to the Firebase reference to listen for changes
        userRef.addValueEventListener(userListener);
    }
}
