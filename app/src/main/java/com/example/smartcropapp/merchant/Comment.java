package com.example.smartcropapp.merchant;

public class Comment {
    public long id;
    public String text;
    public long userId;
    public String userName;
    public String userPhoto;

    public Comment(long id, String text, long userId, String userName, String userPhoto) {
        this.id = id;
        this.text = text;
        this.userId = userId;
        this.userName = userName;
        this.userPhoto = userPhoto;
    }
}
