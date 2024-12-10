package com.chautari.admin_app.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.chautari.admin_app.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ViewCategoriesFragment extends Fragment {

    private ImageView categoryPicture;
    private TextView categoryName, categoryDescription;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String categoryId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryPicture = view.findViewById(R.id.category_picture);
        categoryName = view.findViewById(R.id.category_name);
        categoryDescription = view.findViewById(R.id.category_description);
        progressBar = view.findViewById(R.id.progressBar);
        Button editCategoryButton = view.findViewById(R.id.edit_category_button);
        Button deleteCategoryButton = view.findViewById(R.id.delete_category_button);

        // Get categoryId passed from CategoryFragment
        if (getArguments() != null) {
            categoryId = getArguments().getString("documentId");
        }

        // Initialize Firestore reference for Categories
        db = FirebaseFirestore.getInstance();

        if (categoryId != null) {
            loadCategoryData();
        } else {
            Toast.makeText(getContext(), "Category ID is missing", Toast.LENGTH_SHORT).show();
        }

        editCategoryButton.setOnClickListener(v -> {
            // Pass categoryId to AddCategoriesFragment for editing
            Bundle bundle = new Bundle();
            bundle.putString("categoryId", categoryId);
            Navigation.findNavController(view).navigate(R.id.action_viewCategoriesFragment_to_addCategoriesFragment, bundle);
        });

        deleteCategoryButton.setOnClickListener(v -> {
            if (categoryId != null) {
                deleteCategory();
                requireView();
            } else {
                Toast.makeText(getContext(), "Category ID is not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategoryData() {
        progressBar.setVisibility(View.VISIBLE);

        // Fetch the category data from Firestore
        DocumentReference categoryRef = db.collection("Categories").document(categoryId);
        categoryRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String name = document.getString("name");
                    String description = document.getString("description");
                    String base64Image = document.getString("image"); // Base64 image string

                    if (name != null && description != null) {
                        categoryName.setText(name);
                        categoryDescription.setText(description);
                    }

                    // Decode the Base64 string to Bitmap and set it to the ImageView
                    if (base64Image != null) {
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        Glide.with(requireContext())
                                .load(decodedBitmap)
                                .circleCrop()
                                .into(categoryPicture);
                    } else {
                        Glide.with(requireContext())
                                .load(R.drawable.ic_category)  // Default image if Base64 is null
                                .circleCrop()
                                .into(categoryPicture);
                    }
                } else {
                    Toast.makeText(getContext(), "Category not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Failed to load category", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void deleteCategory() {
        progressBar.setVisibility(View.VISIBLE);

        // Reference to the document in Firestore
        DocumentReference categoryRef = db.collection("Categories").document(categoryId);

        categoryRef.delete().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Category deleted successfully", Toast.LENGTH_SHORT).show();
                // Navigate back to CategoriesFragment and refresh it
                Navigation.findNavController(requireActivity(), R.id.fragment_container)
                        .navigate(R.id.action_viewCategoriesFragment_to_categoriesFragment);
            } else {
                Toast.makeText(getContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
