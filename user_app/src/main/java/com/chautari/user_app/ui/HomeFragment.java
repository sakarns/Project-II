package com.chautari.user_app.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chautari.user_app.R;
import com.chautari.user_app.adapter.CategoryAdapter;
import com.chautari.user_app.adapter.ProductAdapter;
import com.chautari.user_app.models.Category;
import com.chautari.user_app.models.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter, popularProductAdapter, recommendationAdapter;
    private List<Category> categoryList;
    private ArrayList<Product> productList, popularProductList, recommendationProductList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerViews
        RecyclerView categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        RecyclerView newProductRecyclerView = view.findViewById(R.id.newProductRecyclerView);
        RecyclerView popularProductRecyclerView = view.findViewById(R.id.popularProductRecyclerView);
        RecyclerView recommendationRecyclerView = view.findViewById(R.id.recommendationRecyclerView);

        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        newProductRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularProductRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        categoryList = new ArrayList<>();
        productList = new ArrayList<>();
        popularProductList = new ArrayList<>();
        recommendationProductList = new ArrayList<>();

        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
        productAdapter = new ProductAdapter(getContext(), productList);
        popularProductAdapter = new ProductAdapter(getContext(), popularProductList);
        recommendationAdapter = new ProductAdapter(getContext(), recommendationProductList);

        categoryRecyclerView.setAdapter(categoryAdapter);
        newProductRecyclerView.setAdapter(productAdapter);
        popularProductRecyclerView.setAdapter(popularProductAdapter);
        recommendationRecyclerView.setAdapter(recommendationAdapter);

        loadCategories();
        loadProducts();
        loadPopularProducts();
        loadRecommendations();

        return view;
    }

    // Load categories from Firestore
    private void loadCategories() {
        db.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        categoryList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Category category = document.toObject(Category.class);
                            categoryList.add(category);
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error loading categories", e));
    }

    // Load new products from Firestore
    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        productList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                        }
                        productAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error loading products", e));
    }

    // Load popular products based on views or purchase count
    private void loadPopularProducts() {
        db.collection("products")
                .orderBy("viewsCount", Query.Direction.DESCENDING) // Assuming "viewsCount" tracks the popularity
                .limit(5) // Limit to top 5 popular products
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        popularProductList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Product product = document.toObject(Product.class);
                            popularProductList.add(product);
                        }
                        popularProductAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error loading popular products", e));
    }

    // Load recommendations based on user data (e.g., purchase history or viewed products)
    private void loadRecommendations() {
        // For simplicity, we will load products from the same collection but filter or rank them differently
        // Ideally, this should be based on user data and product similarity (e.g., collaborative filtering or content-based filtering)
        db.collection("products")
                .orderBy("rating", Query.Direction.DESCENDING) // Order by rating for recommendations
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        recommendationProductList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Product product = document.toObject(Product.class);
                            recommendationProductList.add(product);
                        }
                        recommendationAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error loading recommendations", e));
    }
}
