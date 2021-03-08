package com.jackz314.keepfit.models;

import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class User {

    private String uid;
    private String biography;
    private String email;
    private String name;
    private String profilePic;
    private Date birthday;
    private Long height;
    private Long weight;
    private Boolean sex;

    public User(){
        name = "";
    }

    public User(DocumentSnapshot doc) {
        if (!doc.exists()) return;
        uid = doc.getId();
        biography = doc.getString("biography");
        email = doc.getString("email");
        name = doc.getString("name");
        profilePic = doc.getString("profile_pic");
        birthday = doc.getDate("birthday");
        height = doc.getLong("height");
        weight = doc.getLong("weight");
        sex = doc.getBoolean("sex");
    }

    public User(String uid, String biography, String email, String name, String profilePic, Date birthday, Long height, Long weight, Boolean sex) {
        this.uid = uid;
        this.biography = biography;
        this.email = email;
        this.name = name;
        this.profilePic = profilePic;
        this.birthday = birthday;
        this.height = height;
        this.weight = weight;
        this.sex = sex;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    @NotNull
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", biography='" + biography + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", profile_pic='" + profilePic + '\'' +
                ", birthday=" + birthday +
                ", height=" + height +
                ", weight=" + weight +
                ", sex=" + sex +
                '}';
    }
}
