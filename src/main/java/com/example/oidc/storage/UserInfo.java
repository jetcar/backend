package com.example.oidc.storage;

public class UserInfo {
    private String sub;
    private String name;
    private String email;
    private String country;
    private String phoneNumber;
    private String nonce;

    public UserInfo() {}

    public UserInfo(String sub, String name, String email, String country, String phoneNumber, String nonce) {
        this.sub = sub;
        this.name = name;
        this.email = email;
        this.country = country;
        this.phoneNumber = phoneNumber;
        this.nonce = nonce;
    }

    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }
}