package com.example.guyto.petmatev2;

import java.util.Date;

public class Match {
    Date date;
    String email;
    String petName;


    public Match(Date date, String email, String petName) {
        this.date = date;
        this.email = email;
        this.petName = petName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }
}
