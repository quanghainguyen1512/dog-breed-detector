package com.tung40915.camerademo;

import android.graphics.RectF;

public class DogBreed {
    private int id;
    private String name;
    private float avgHeight;
    private float avgWeight;
    private String description;

    public DogBreed()
    {
    }

    public DogBreed(int id, String name, float avgHeight, float avgWeight, String description) {
        this.id = id;
        this.name = name;
        this.avgHeight = avgHeight;
        this.avgWeight = avgWeight;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getAvgHeight() {
        return avgHeight;
    }

    public float getAvgWeight() {
        return avgWeight;
    }

    public String getDescription() {
        return description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvgHeight(float avgHeight) {
        this.avgHeight = avgHeight;
    }

    public void setAvgWeight(float avgWeight) {
        this.avgWeight = avgWeight;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
