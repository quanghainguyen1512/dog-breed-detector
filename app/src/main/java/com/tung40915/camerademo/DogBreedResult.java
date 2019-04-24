package com.tung40915.camerademo;

import android.graphics.RectF;

public class DogBreedResult {
    private int ID;
    private RectF location;

    public DogBreedResult()
    {
    }

    public DogBreedResult(int ID, RectF location) {
        this.ID = ID;
        this.location = location;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public RectF getLocation() {
        return location;
    }

    public void setLocation(RectF location) {
        this.location = location;
    }
}
