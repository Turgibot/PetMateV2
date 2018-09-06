package com.example.guyto.petmatev2;

import com.example.guyto.petmatev2.Match;
import com.example.guyto.petmatev2.Pet;

import java.util.ArrayList;
import java.util.List;


public class User {


    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private List<Pet> pets;
    private List<Match> matches;



    public User(){
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.password = "";
        this.phone = "";
        this.pets = null;
        this.matches = null;
    }

    public User(String firstName, String lastName, String email, String password, String phone, List<Pet> pets, List<Match> matches) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.pets = pets;
        this.matches = matches;
    }
    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
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

}
