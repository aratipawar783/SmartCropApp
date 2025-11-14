package com.example.smartcropapp.merchant;
public class PostItem {
    public long id, userId;
    public String imageUri, caption, price, userName, userPhoto, location, userPhone;
    public PostItem(long id,long userId,String imageUri,String caption,String price,String userName,String userPhoto,String location,String userPhone){
        this.id=id;this.userId=userId;this.imageUri=imageUri;this.caption=caption;this.price=price;this.userName=userName;this.userPhoto=userPhoto;this.location=location;this.userPhone=userPhone;
    }
}
