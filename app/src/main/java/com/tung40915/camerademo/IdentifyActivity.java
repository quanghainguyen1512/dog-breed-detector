package com.tung40915.camerademo;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IdentifyActivity extends AppCompatActivity implements View.OnClickListener {

    final String DatabaseName = "DogBreedDatabase.sqlite";
    SQLiteDatabase database;

    Toolbar toolbar;
    ImageView imageView;

    private static final int REQUEST_CODE_CAMERA = 2500;
    private static final int PICK_IMAGE = 1002;

    RelativeLayout rl;
    Button bt;
    RelativeLayout.LayoutParams params;

    List<DogBreedResult> result;

    Dialog informationDialog;
    TextView info_name;
    TextView info_avgWeight;
    TextView info_avgHeight;
    TextView info_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);

        AnhXa();
        ActionBar();

        database = Database.initDatabase(this,DatabaseName);

        result = new ArrayList<DogBreedResult>();
        int a1,a2,a3,a4,a5;
        Random rand = new Random();

        for(int i=0;i<3;i++)
        {
            a1 = rand.nextInt(3) + 1;
            a2 = rand.nextInt(500) + 300;
            a3 = rand.nextInt(500) + 800;
            a4 = rand.nextInt(1000) + 100;
            a5 = rand.nextInt(1000) + 1100;

            result.add(new DogBreedResult(a1,new RectF(a2,a3,a4,a5)));


        }

        int number = getIntent().getExtras().getInt("MY_KEY");
        if(number == 1)
        {
           openCamera();
        }
        else
        {
            openGallery();
        }

        for (DogBreedResult a: result)
        {
            RectF exam = a.getLocation();
            bt= new Button(this);
            bt.setBackground(getResources().getDrawable(R.drawable.button_border));
            float w = exam.right-exam.left;
            float h = exam.bottom - exam.top;
            params = new RelativeLayout.LayoutParams((int)w,(int)h);
            params.leftMargin = (int) exam.left;
            params.topMargin = (int) exam.left;

            bt.setTag(a);
            rl.addView(bt, params);
            bt.setOnClickListener(IdentifyActivity.this);
        }
    }
    @Override
    public void onClick(View v)
    {
        DogBreedResult result = (DogBreedResult) v.getTag();

        String query = "SELECT * FROM DogBreed WHERE ID = "+result.getID() + ";";
        Cursor cursor = database.rawQuery(query,null);

        cursor.moveToFirst();

        info_name.setText("Name: \t"+cursor.getString(1));
        info_avgWeight.setText("Weight: \t"+cursor.getString(2));
        info_avgHeight.setText("Height: \t"+cursor.getString(3));
        info_description.setText("More Detail: \t"+cursor.getString(4));
        informationDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,   int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode)
        {
            case REQUEST_CODE_CAMERA:
            {
                if(grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                {
                    openCamera();
                }
                else{
                    Toast.makeText(this,"Bạn cần cho phép ứng dụng truy cập máy ảnh để dùng chức năng này",Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

        //Toast.makeText(this,"I got there",Toast.LENGTH_LONG).show();
//        if(requestCode == REQUEST_CODE_CAMERA && grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
//            openCamera();
//        }
//        else if (requestCode == REQUEST_CODE_CAMERA && grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_DENIED)
//        {
//            Toast.makeText(this,"Bạn cần cho phép ứng dụng truy cập máy ảnh để dùng chức năng này",Toast.LENGTH_LONG).show();
//        }
//        else if(requestCode == PICK_IMAGE && grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
//            openGallery();
//        }
//        else if (requestCode == PICK_IMAGE && grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_DENIED)
//        {
//            Toast.makeText(this,"Bạn cần cho phép ứng dụng truy cập bộ sưu tập để dùng chức năng này",Toast.LENGTH_LONG).show();
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {




        if(requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }

        else
            if(resultCode == RESULT_OK && requestCode == PICK_IMAGE && data!= null)
        {
           Uri imageUri = data.getData();
           imageView.setImageURI(imageUri);
        }
        else if(resultCode == RESULT_CANCELED)
            finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void ActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void AnhXa(){
        toolbar = (Toolbar) findViewById(R.id.toolBar_IDENTIFYACTIVITY);
        imageView = (ImageView) findViewById(R.id.imageview_IDENTIFYACTIVITY);
        rl = (RelativeLayout) findViewById(R.id.mylayout);

        informationDialog = new Dialog(this);
        informationDialog.setContentView(R.layout.pop_up);
        informationDialog.setCanceledOnTouchOutside(true);

        info_name = (TextView) informationDialog.findViewById(R.id.PopUpName);
        info_avgWeight  = (TextView) informationDialog.findViewById(R.id.PopUpAvgWeight);
        info_avgHeight  = (TextView) informationDialog.findViewById(R.id.PopUpAvgHeight);
        info_description  = (TextView) informationDialog.findViewById(R.id.PopUpDiscription);



    }
}
