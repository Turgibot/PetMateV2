package com.example.guyto.petmatev2;

public class Match {
    private User user;
    private Pet srcPet;
    private Pet targetPet;


    public Match(User user, Pet srcPet, Pet targetPet) {
        this.user = user;
        this.srcPet = srcPet;
        this.targetPet = targetPet;
    }

    public Match(){
        this.user = null;
        this.srcPet = null;
        this.targetPet = null;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Pet getSrcPet() {
        return srcPet;
    }

    public void setSrcPet(Pet srcPet) {
        this.srcPet = srcPet;
    }

    public Pet getTargetPet() {
        return targetPet;
    }

    public void setTargetPet(Pet targetPet) {
        this.targetPet = targetPet;
    }
}
