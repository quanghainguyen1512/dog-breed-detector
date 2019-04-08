package com.tung40915.camerademo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class IdentifyActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView imageView;

    int REQUEST_CODE_CAMERA = 1;
    int PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);

        AnhXa();
        ActionBar();

        int number = getIntent().getExtras().getInt("MY_KEY");
        if(number == 1)
        {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(IdentifyActivity.this, new String[] {Manifest.permission.CAMERA},REQUEST_CODE_CAMERA);
            }
            else
                openCamera();

        }
        else
        {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(IdentifyActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},PICK_IMAGE);
            }
            else
                openGallery();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_CAMERA && grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
        else if (requestCode == REQUEST_CODE_CAMERA && grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_DENIED)
        {
            Toast.makeText(this,"Bạn cần cho phép ứng dụng truy cập máy ảnh để dùng chức năng này",Toast.LENGTH_LONG).show();
        }
        else if(requestCode == PICK_IMAGE && grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }
        else if (requestCode == PICK_IMAGE && grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_DENIED)
        {
            Toast.makeText(this,"Bạn cần cho phép ứng dụng truy cập bộ sưu tập để dùng chức năng này",Toast.LENGTH_LONG).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }

        else if(resultCode == RESULT_OK && requestCode == PICK_IMAGE && data!= null)
        {
           Uri imageUri = data.getData();
           imageView.setImageURI(imageUri);
        }
        else if(resultCode == RESULT_CANCELED && requestCode == PICK_IMAGE)
            finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
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
    }
}
