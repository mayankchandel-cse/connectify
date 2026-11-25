package com.connectify.model;

import java.sql.Timestamp;

/** POJO for one row of the Users table. */
public class User {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String bio;
    private String profilePic;
    private String dob;          // kept as String (yyyy-MM-dd) for simplicity
    private String gender;
    private int postCount;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String name, String email, String password,
                String bio, String profilePic, String dob, String gender) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.profilePic = profilePic;
        this.dob = dob;
        this.gender = gender;
    }

    public User(String name, String email, String password, String bio, String dob, String gender) {
        this(0, name, email, password, bio, "default.png", dob, gender);
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + name + "', email='" + email + "', bio='" + bio + "'}";
    }
}
