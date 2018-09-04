package com.example.guyto.petmatev2;

import java.util.Date;

public class Match {
    Date date;
    User user;
    String petName;


    public Match(Date date, User user, String petName) {
        this.date = date;
        this.user = user;
        this.petName = petName;
    }

    public Match(){
        this.date = null;
        this.user = null;
        this.petName = null;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
