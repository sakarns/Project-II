package com.chautari.admin_app.models;

// Category class to represent category data
public class Category {

    public String name;
    public String description;
    public String image;
    public String documentId;

    public Category(String name, String description, String image) {
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public Category(String name, String description, String image, String documentId) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.documentId = documentId;
    }

    public Category(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public Category(String documentId) {
        this.documentId = documentId;
    }

    public Category() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
