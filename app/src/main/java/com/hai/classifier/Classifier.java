package com.hai.classifier;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;

import com.hai.utils.Logger;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


public abstract class Classifier {

    private static final Logger LOGGER = new Logger();

    public enum Device {
        CPU,
        NNAPI,
        GPU
    }
    private static final int MAX_RESULTS = 3;

    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    private final int[] intValues = new int[getImageSizeX() * getImageSizeY()];

    private final Interpreter.Options tfliteOptions = new Interpreter.Options();

    private MappedByteBuffer tfliteModel;

    private List<String> labels;

    private GpuDelegate gpuDelegate = null;

    protected Interpreter tflite;

    protected ByteBuffer imgData = null;

    public abstract int getImageSizeX();

    public abstract int getImageSizeY();

    protected abstract String getModelPath();

    protected abstract String getLabelPath();

    protected abstract int getNumBytesPerChannel();

    protected abstract void addPixelValue(int pixelValue);

    protected abstract float getProbability(int labelIndex);

    protected abstract float getNormalizedProbability(int labelIndex);

    protected abstract void runInference();

    protected int getNumLabels() {
        return labels.size();
    }

    public static class Recognition {
        public Recognition(String id, String title, Float score) {
            this.id = id;
            this.title = title;
            this.score = score;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getScore() {
            return score;
        }

        private final String id;

        private final String title;

        private final Float score;

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (score != null) {
                resultString += String.format("(%.1f%%) ", score * 100.0f);
            }
            return resultString.trim();
        }
    }

    protected Classifier(Activity activity, Device device, int numThreads) throws IOException {
        tfliteModel = loadModelFile(activity);
        switch (device) {
            case NNAPI:
                tfliteOptions.setUseNNAPI(true);
                break;
            case GPU:
                gpuDelegate = new GpuDelegate();
                tfliteOptions.addDelegate(gpuDelegate);
                break;
            case CPU:
                break;
        }
        tfliteOptions.setNumThreads(numThreads);
        tflite = new Interpreter(tfliteModel, tfliteOptions);
        labels = loadLabelList(activity);
        imgData =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE
                                * getImageSizeX()
                                * getImageSizeY()
                                * DIM_PIXEL_SIZE
                                * getNumBytesPerChannel());
        imgData.order(ByteOrder.nativeOrder());
        LOGGER.d("Created a Tensorflow Lite Image Classifier.");
    }

    private List<String> loadLabelList(Activity activity) throws IOException {
        List<String> labels = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(getLabelPath())));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < getImageSizeX(); ++i) {
            for (int j = 0; j < getImageSizeY(); ++j) {
                final int val = intValues[pixel++];
                addPixelValue(val);
            }
        }
        long endTime = SystemClock.uptimeMillis();
        LOGGER.v("Timecost to put values into ByteBuffer: " + (endTime - startTime));
    }

    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessImage");
        convertBitmapToByteBuffer(bitmap);
        Trace.endSection();

        Trace.beginSection("inference");
        long start = SystemClock.uptimeMillis();
        runInference();
        long end = SystemClock.uptimeMillis();
        Trace.endSection();
        LOGGER.v("Timecost to run model inference: " + ( - start));

        PriorityQueue<Recognition> pq = new PriorityQueue<>(3,
                new Comparator<Recognition>() {
                    @Override
                    public int compare(Recognition l, Recognition r) {
                        return Float.compare(r.getScore(), l.getScore());
                    }
                });
        for (int i = 0; i < labels.size(); i++) {
            pq.add(new Recognition(
                    "" + i,
                    labels.size() > i ? labels.get(i) : "unknown",
                    getNormalizedProbability(i)
            ));
        }

        final ArrayList<Recognition> result = new ArrayList<>();

        int size = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < size; i++) {
            result.add(pq.poll());
        }
        Trace.endSection();
        return result;
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        }
        tfliteModel = null;
    }
}