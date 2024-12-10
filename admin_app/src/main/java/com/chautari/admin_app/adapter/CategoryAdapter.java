package com.chautari.admin_app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chautari.admin_app.R;
import com.chautari.admin_app.models.Category;
import com.chautari.admin_app.ui.ViewCategoriesFragment;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final ArrayList<Category> categoryList;

    public CategoryAdapter(Context context, ArrayList<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList != null ? categoryList : new ArrayList<>();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        String documentId = category.getDocumentId();

        holder.categoryName.setText(category.getName());

        // Decoding the Base64 image if image is in Base64 format
        try {
            byte[] decodedString = android.util.Base64.decode(category.getImage(), android.util.Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Glide.with(context).load(decodedBitmap).into(holder.categoryImage);
        } catch (IllegalArgumentException e) {
            Glide.with(context).load(R.drawable.ic_category).into(holder.categoryImage);
        }

        // Navigate to ViewCategoriesFragment with the correct documentId
        holder.viewButton.setOnClickListener(view -> navigateToViewCategoryFragment(documentId));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    private void navigateToViewCategoryFragment(String documentId) {
        if (context instanceof FragmentActivity) {
            ViewCategoriesFragment fragment = new ViewCategoriesFragment();
            Bundle args = new Bundle();
            args.putString("documentId", documentId);
            fragment.setArguments(args);

            ((FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryName;
        Button viewButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryName = itemView.findViewById(R.id.categoryName);
            viewButton = itemView.findViewById(R.id.viewButton);
        }
    }
}
