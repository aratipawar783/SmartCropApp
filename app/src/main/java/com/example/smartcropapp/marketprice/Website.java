package com.example.smartcropapp.marketprice;

public class Website {
    public String name;
    public String url;
    public String description;
    public String logo; // drawable name
    public String color; // hex color
    public String font; // asset font
    public boolean isFavorite = false; // <--- must be public

    public Website(String name, String url, String description, String logo, String color, String font) {
        this.name = name;
        this.url = url;
        this.description = description;
        this.logo = logo;
        this.color = color;
        this.font = font;
    }
}
