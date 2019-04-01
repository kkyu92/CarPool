package com.example.kks.carpool.model;

import com.google.gson.annotations.SerializedName;

import okhttp3.MultipartBody;

public class Upload {

    private String image;
    private String dishes_name;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDishes_name() {
        return dishes_name;
    }

    public void setDishes_name(String dishes_name) {
        this.dishes_name = dishes_name;
    }
}
