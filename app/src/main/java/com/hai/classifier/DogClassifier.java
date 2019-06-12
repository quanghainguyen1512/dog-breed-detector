package com.hai.classifier;

import android.app.Activity;

import java.io.IOException;

public class DogClassifier extends Classifier {

    protected DogClassifier(Activity activity, Device device, int numThreads) throws IOException {
        super(activity, device, numThreads);
    }

    private float[][] labelProbArray = null;

    @Override
    public int getImageSizeX() {
        return 224;
    }

    @Override
    public int getImageSizeY() {
        return 224;
    }

    @Override
    protected String getModelPath() {
        return "inception.tflite";
    }

    @Override
    protected String getLabelPath() {
        return "dog_labels.txt";
    }

    @Override
    protected int getNumBytesPerChannel() {
        return 4;
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        imgData.putFloat(((pixelValue >> 16) & 0xFF) / 255);
        imgData.putFloat(((pixelValue >> 8) & 0xFF) / 255);
        imgData.putFloat((pixelValue & 0xFF) / 255);
    }

    @Override
    protected float getProbability(int labelIndex) {
        return 0;
    }

    @Override
    protected float getNormalizedProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    @Override
    protected void runInference() {
        tflite.run(imgData, labelProbArray);
    }
}
