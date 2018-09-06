package com.example.guyto.petmatev2;

import java.util.ArrayList;
import java.util.List;


public class User {


    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private List<Pet> pets;
    private List<Like> likes;



    public User(){
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.password = "";
        this.phone = "";
        this.pets = null;
        this.likes = null;
    }

    public User(String firstName, String lastName, String email, String password, String phone, List<Pet> pets, List<Like> likes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.pets = pets;
        this.likes = likes;
    }
    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }

    public void addPet(Pet p){
        if(this.pets == null){
            this.pets = new ArrayList<>();
        }
        this.pets.add(p);
    }
    public void addToLikes(Like like){
        if(this.likes == null){
            this.likes = new ArrayList<>();
        }
        this.likes.add(like);
    }

}
