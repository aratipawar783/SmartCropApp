package com.example.smartcropapp.merchant;

public class Post {
    public long id, userId;
    public String imageUri, caption, expectedPrice;
    public long createdAt;
    // fetched metadata
    public String userName, userPhoto, userLocation, userPhone;
    public Post(long id,long userId,String imageUri,String caption,String expectedPrice,long createdAt){
        this.id=id; this.userId=userId; this.imageUri=imageUri; this.caption=caption; this.expectedPrice=expectedPrice; this.createdAt=createdAt;
    }
}
