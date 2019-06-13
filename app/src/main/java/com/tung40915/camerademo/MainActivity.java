package com.tung40915.camerademo;

import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hai.classifier.Classifier;
import com.hai.classifier.DogClassifier;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    final String DatabaseName = "DogBreed.sqlite";

    SQLiteDatabase database;

    private static final String MODEL_PATH = "mod.tflite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "dog_labels.txt";
    private static final int INPUT_SIZE = 224;

    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnToggleCamera,btnMore;
    private ImageView imageViewResult;
    private CameraView cameraView;

    private String id;

    Dialog informationDialog;
    TextView info_name;
    TextView info_avgWeight;
    TextView info_avgHeight;
    TextView info_description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.cameraView);
        imageViewResult = findViewById(R.id.imageViewResult);
        textViewResult = findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());

        btnToggleCamera = findViewById(R.id.btnToggleCamera);
        btnDetectObject = findViewById(R.id.btnDetectObject);
        btnMore = findViewById(R.id.btnMoreInfo);

        btnMore.setVisibility(View.INVISIBLE);

        informationDialog = new Dialog(this);
        informationDialog.setContentView(R.layout.pop_up);
        informationDialog.setCanceledOnTouchOutside(true);

        info_name = (TextView) informationDialog.findViewById(R.id.PopUpName);
        info_avgWeight  = (TextView) informationDialog.findViewById(R.id.PopUpAvgWeight);
        info_avgHeight  = (TextView) informationDialog.findViewById(R.id.PopUpAvgHeight);
        info_description  = (TextView) informationDialog.findViewById(R.id.PopUpDiscription);

        database = Database.initDatabase(this,DatabaseName);




        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap = cameraKitImage.getBitmap();

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

                imageViewResult.setImageBitmap(bitmap);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                if(results.size() != 0)
                {
                    String display = "Breed: "+ results.get(0).getTitle() + "\n" + "Confidence: "+results.get(0).getConfidence();
                    textViewResult.setText(display);
                    btnMore.setVisibility(View.VISIBLE);
                    id = results.get(0).getId();
                }
                else
                    {
                        textViewResult.setText("");
                        btnMore.setVisibility(View.INVISIBLE);
                        id = "";
                    }



            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.toggleFacing();
            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(id != "")
                {
                    String query = "SELECT * FROM DogBreed WHERE ID = "+id + ";";
                    Cursor cursor = database.rawQuery(query,null);


                    if(cursor!=null)
                    {
                        cursor.moveToFirst();
                        info_name.setText("Name: \t"+cursor.getString(1));
                        info_avgWeight.setText("Weight: \t"+cursor.getString(2));
                        info_avgHeight.setText("Height: \t"+cursor.getString(3));
                        info_description.setText("More Detail: \t"+cursor.getString(4));
                        informationDialog.show();
                    }
                }
            }
        });

        initTensorFlowAndLoadModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = DogClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }
}
