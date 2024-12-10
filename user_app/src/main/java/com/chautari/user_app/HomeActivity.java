package com.chautari.user_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.chautari.user_app.ui.CartFragment;
import com.chautari.user_app.ui.CategoriesFragment;
import com.chautari.user_app.ui.HomeFragment;
import com.chautari.user_app.ui.NotificationsFragment;
import com.chautari.user_app.ui.OrderHistoryFragment;
import com.chautari.user_app.ui.OrdersFragment;
import com.chautari.user_app.ui.ProfileFragment;
import com.chautari.user_app.ui.SettingsFragment;
import com.chautari.user_app.ui.WishlistFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView usernameTextView, emailTextView, registrationDateTextView;
    private ImageView editImage, profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupUI();
        displayUserInfo();
        setNavigation();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "HOME");
        }
        setupBackNavigation();
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        editImage = headerView.findViewById(R.id.editImage);
        emailTextView = headerView.findViewById(R.id.email);
        usernameTextView = headerView.findViewById(R.id.username);
        profileImageView = headerView.findViewById(R.id.user_image);
        registrationDateTextView = headerView.findViewById(R.id.registration_date);

        ImageView backMenu = headerView.findViewById(R.id.backMenu);
        backMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                finish();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toggle.syncState();
    }

    private void displayUserInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admins").child(userId);

            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot adminSnapshot) {
                    if (adminSnapshot.exists()) {
                        loadUserData(adminSnapshot);
                    } else {
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                loadUserData(userSnapshot);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.w("HomeActivity", "loadUser:onCancelled", error.toException());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("HomeActivity", "loadAdmin:onCancelled", error.toException());
                }
            });
        } else {
            updateLoginLogoutMenuItems(false);
        }
    }

    private void loadUserData(DataSnapshot snapshot) {
        usernameTextView.setText(snapshot.child("username").getValue(String.class));
        emailTextView.setText(snapshot.child("email").getValue(String.class));
        editImage.setVisibility(View.VISIBLE);
        editImage.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, KYCActivity.class)));

        String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
        if (profileImageUrl != null) {
            byte[] decodedString = Base64.decode(profileImageUrl, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(HomeActivity.this).load(decodedBitmap).transform(new CircleCrop()).into(profileImageView);
        }

        registrationDateTextView.setText(String.format("Since %s", snapshot.child("registrationDate").getValue(String.class)));
        updateLoginLogoutMenuItems(true);
    }

    private void updateLoginLogoutMenuItems(boolean isLoggedIn) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        MenuItem loginItem = navigationView.getMenu().findItem(R.id.menu_login);
        MenuItem logoutItem = navigationView.getMenu().findItem(R.id.menu_logout);

        loginItem.setVisible(!isLoggedIn);
        editImage.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
        logoutItem.setVisible(isLoggedIn);
    }

    private void setNavigation() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationSelection(item);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupBackNavigation() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (currentFragment instanceof HomeFragment) {
                        finish();
                    } else {
                        loadFragment(new HomeFragment(), "HOME");
                    }
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void handleNavigationSelection(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_home) {
            loadFragment(new HomeFragment(), "HOME");
        } else if (itemId == R.id.menu_profile) {
            loadFragment(new ProfileFragment(), "PROFILE");
        } else if (itemId == R.id.menu_settings) {
            loadFragment(new SettingsFragment(), "SETTINGS");
        } else if (itemId == R.id.menu_categories) {
            loadFragment(new CategoriesFragment(), "CATEGORIES");
        } else if (itemId == R.id.menu_cart) {
            loadFragment(new CartFragment(), "CART");
        } else if (itemId == R.id.menu_wishlist) {
            loadFragment(new WishlistFragment(), "WISHLIST");
        } else if (itemId == R.id.menu_notifications) {
            loadFragment(new NotificationsFragment(), "NOTIFICATIONS");
        } else if (itemId == R.id.menu_orders) {
            loadFragment(new OrdersFragment(), "ORDERS");
        } else if (itemId == R.id.order_history) {
            loadFragment(new OrderHistoryFragment(), "ORDER_HISTORY");
        } else if (itemId == R.id.menu_login) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        } else if (itemId == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            updateLoginLogoutMenuItems(false);
            displayUserInfo();
            loadFragment(new HomeFragment(), "HOME");
        } else {
            Toast.makeText(this, "Feature not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, tag).commit();
    }
}
