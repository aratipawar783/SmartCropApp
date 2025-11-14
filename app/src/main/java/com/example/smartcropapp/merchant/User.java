package com.example.smartcropapp.merchant;

public class User {
    public long id;
    public String name, phone, email, location, crops, bio, photoUri;
    public User(long id,String name,String phone,String email,String location,String crops,String bio,String photoUri){
        this.id=id; this.name=name; this.phone=phone; this.email=email; this.location=location; this.crops=crops; this.bio=bio; this.photoUri=photoUri;
    }
    public String getName(){ return name==null?"":name; }
    public String getPhone(){ return phone==null?"":phone; }
    public String getPhoto(){ return photoUri; }
}
