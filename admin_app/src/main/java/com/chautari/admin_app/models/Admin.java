package com.chautari.admin_app.models;

public class Admin {
    public String username;
    public String contactNumber;
    public String email;
    public String password;
    public String fullName;
    public String dateOfBirth;
    public String address;
    public String gender;
    public String position;
    public String profileImageUrl;
    public String registrationDate;


    // Empty constructor required for Firebase deserialization
    public Admin() {
    }

    // Constructor for registration/login
    public Admin(String username, String email, String position, String password, String registrationDate) {
        this.username = username;
        this.email = email;
        this.position = position;
        this.password = password;
        this.registrationDate = registrationDate;
    }

    // Constructor for Admin with KYC information
    public Admin(String fullName, String contactNumber, String email, String dateOfBirth, String address, String gender, String position,String profileImageUrl) {
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.gender = gender;
        this.position = position;
        this.profileImageUrl = profileImageUrl;
    }

    // Constructor for Admin with KYC information
    public Admin(String fullName, String contactNumber, String email, String dateOfBirth, String address, String gender, String profileImageUrl) {
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
    }

    public Admin(String username,String email, String profileImageUrl) {
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }
}