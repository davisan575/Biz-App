package com.example.afinal;

import com.google.firebase.database.ServerValue;

public class User {
    public String email;
    public String firstname;
    public String lastname;
    public String displayname;
    public String company;
    public String phone;
    public String profilepic;
    public String card;
    public Object timestamp;
    public User(String email, String fn, String ln, String displayname, String company, String phone, String profilepic, String card) {
        this.firstname=fn;
        this.lastname=ln;
        this.displayname=displayname;
        this.email=email;
        this.phone=phone;
        this.company=company;
        this.profilepic=profilepic;
        this.card=card;
        this.timestamp= ServerValue.TIMESTAMP;
    }
    public Object getTimestamp(){
        return timestamp;
    }
    public User() {
        this.timestamp= ServerValue.TIMESTAMP;
    }
}

