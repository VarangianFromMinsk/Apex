package com.example.myapplication.main.Models;

public class Model_User {

    private String name;
    private String email;
    private String FirebaseId;
    private String online;
    private String dayOnline;
    private String timeonline;
    private String avatarMockUpResourse;
    private String nick;
    private String telephone;
    private String background;
    boolean isBlocked = false;

    public Model_User() {
    }

    public Model_User(String name, String email, String firebaseId, String online, String dayOnline, String timeonline, String avatarMockUpResourse, String nick, String telephone, String background, boolean isBlocked) {
        this.name = name;
        this.email = email;
        FirebaseId = firebaseId;
        this.online = online;
        this.dayOnline = dayOnline;
        this.timeonline = timeonline;
        this.avatarMockUpResourse = avatarMockUpResourse;
        this.nick = nick;
        this.telephone = telephone;
        this.background = background;
        this.isBlocked = isBlocked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirebaseId() {
        return FirebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        FirebaseId = firebaseId;
    }

    public String getAvatarMockUpResourse() {
        return avatarMockUpResourse;
    }

    public void setAvatarMockUpResourse(String avatarMockUpResourse) {
        this.avatarMockUpResourse = avatarMockUpResourse;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getDayOnline() {
        return dayOnline;
    }

    public void setDayOnline(String dayOnline) {
        this.dayOnline = dayOnline;
    }

    public String getTimeonline() {
        return timeonline;
    }

    public void setTimeonline(String timeonline) {
        this.timeonline = timeonline;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
