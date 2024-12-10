package com.chautari.admin_app.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chautari.admin_app.R;
import com.chautari.admin_app.models.Category;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AddCategoriesFragment extends Fragment {

    private static final String TAG = "AddCategoriesFragment";
    private FirebaseFirestore firestore;
    private EditText categoryNameEditText, categoryDescriptionEditText;
    private ImageView categoryImageView;
    private ProgressBar progressBar;
    private Uri imageUri;
    private String documentId;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        InputStream imageStream = requireActivity().getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        categoryImageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "Image file not found", e);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        categoryNameEditText = view.findViewById(R.id.category_name);
        categoryDescriptionEditText = view.findViewById(R.id.category_description);
        categoryImageView = view.findViewById(R.id.category_picture);
        Button editCategoryButton = view.findViewById(R.id.edit_category_button);
        progressBar = view.findViewById(R.id.progressBar);

        documentId = getArguments() != null ? getArguments().getString("documentId") : null;

        if (documentId != null) {
            loadCategoryData(documentId);
        }

        categoryImageView.setOnClickListener(v -> openImagePicker());
        editCategoryButton.setOnClickListener(v -> {
            if (documentId == null) {
                addCategory();
            } else {
                updateCategory(documentId);
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadCategoryData(String documentId) {
        progressBar.setVisibility(View.VISIBLE);
        DocumentReference docRef = firestore.collection("Categories").document(documentId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Category category = documentSnapshot.toObject(Category.class);
                if (category != null) {
                    categoryNameEditText.setText(category.getName());
                    categoryDescriptionEditText.setText(category.getDescription());

                    if (category.getImage() != null) {
                        byte[] decodedBytes = Base64.decode(category.getImage(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        categoryImageView.setImageBitmap(bitmap);
                    }
                }
            }
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Failed to load category", e);
            Toast.makeText(getContext(), "Failed to load category data", Toast.LENGTH_SHORT).show();
        });
    }

    private void addCategory() {
        String categoryName = categoryNameEditText.getText().toString().trim();
        String categoryDescription = categoryDescriptionEditText.getText().toString().trim();

        if (categoryName.isEmpty() || categoryDescription.isEmpty() || imageUri == null) {
            Toast.makeText(getContext(), "Please fill in all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String base64Image = convertImageToBase64(imageUri);

        if (base64Image == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Failed to process the image.", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference categoriesRef = firestore.collection("Categories");
        Category category = new Category(categoryName, categoryDescription, base64Image);

        categoriesRef.add(category).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Category added successfully.", Toast.LENGTH_SHORT).show();
                reloadFragment();
            } else {
                Log.e(TAG, "Failed to add category", task.getException());
                Toast.makeText(getContext(), "Failed to add category.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategory(String documentId) {
        String categoryName = categoryNameEditText.getText().toString().trim();
        String categoryDescription = categoryDescriptionEditText.getText().toString().trim();

        if (categoryName.isEmpty() || categoryDescription.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String base64Image = imageUri != null ? convertImageToBase64(imageUri) : null;

        DocumentReference categoryRef = firestore.collection("Categories").document(documentId);

        categoryRef.update(
                "name", categoryName,
                "description", categoryDescription,
                "imageBase64", base64Image
        ).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Category updated successfully.", Toast.LENGTH_SHORT).show();
                reloadFragment();
            } else {
                Log.e(TAG, "Failed to update category", task.getException());
                Toast.makeText(getContext(), "Failed to update category.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream imageStream = requireActivity().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Image file not found", e);
            return null;
        }
    }

    private void reloadFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AddCategoriesFragment())
                .commit();
    }

}
