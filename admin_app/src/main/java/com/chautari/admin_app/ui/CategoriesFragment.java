package com.chautari.admin_app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chautari.admin_app.R;
import com.chautari.admin_app.adapter.CategoryAdapter;
import com.chautari.admin_app.models.Category;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private CategoryAdapter adapter;
    private ArrayList<Category> categoryList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        // Changed to GridLayoutManager for a grid layout
        RecyclerView categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns for grid
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(getContext(), categoryList);
        categoryRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchCategories();

        return view;
    }

    private void fetchCategories() {
        CollectionReference categoriesRef = db.collection("Categories");
        categoriesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Category> newCategoryList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String documentId = document.getId(); // Access document ID
                    String name = document.getString("name"); // Access "name" field
                    String description = document.getString("description"); // Access "description" field
                    String image = document.getString("image"); // Access "image" field

                    // Create a new Category object with the fetched data
                    Category category = new Category(name, description, image);
                    category.setDocumentId(documentId);
                    newCategoryList.add(category);
                }

                // Only update the adapter if the data has changed
                if (!newCategoryList.equals(categoryList)) {
                    int oldSize = categoryList.size();
                    categoryList.clear();
                    categoryList.addAll(newCategoryList);
                    adapter.notifyItemRangeInserted(oldSize, newCategoryList.size());
                }
            } else {
                Toast.makeText(getActivity(), "Error fetching categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCategories();
    }

}
