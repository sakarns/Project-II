package com.chautari.admin_app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chautari.admin_app.R;
import com.chautari.admin_app.adapter.CategoryAdapter;
import com.chautari.admin_app.models.Category;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Set up RecyclerView for horizontal scrolling
        RecyclerView categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));  // Horizontal layout
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(getContext(), (ArrayList<Category>) categoryList);
        categoryRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchCategories();

        return view;
    }

    private void fetchCategories() {
        // Fetch categories from Firestore
        CollectionReference categoriesRef = db.collection("Categories");
        categoriesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Category> newCategoryList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String documentId = document.getId();
                    String name = document.getString("name");
                    String description = document.getString("description");
                    String image = document.getString("image");

                    // Create new Category object
                    Category category = new Category(name, description, image);
                    category.setDocumentId(documentId);  // Set document ID
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
}
