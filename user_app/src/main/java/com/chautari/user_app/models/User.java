package com.chautari.user_app.models;

public class User {
    public String username;
    public String contactNumber;
    public String email;
    public String password;
    public String fullName;
    public String dateOfBirth;
    public String address;
    public String gender;
    public String profileImageUrl;
    public String registrationDate;

    // Constructor for registration/login
    public User(String username, String contactNumber, String email, String password, String registrationDate) {
        this.username = username;
        this.contactNumber = contactNumber;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
    }

    // Constructor for User with KYC information
    public User(String fullName, String contactNumber, String email, String dateOfBirth, String address, String gender, String profileImageUrl) {
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
    }

    // Empty constructor required for Firebase deserialization
    public User() {}
}
