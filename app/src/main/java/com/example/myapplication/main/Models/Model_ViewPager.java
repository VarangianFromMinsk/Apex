package com.example.myapplication.main.Models;

public class Model_ViewPager {

    String title, description;
    int image;
    int price;

    public Model_ViewPager(String title, String description, int image, int price) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
