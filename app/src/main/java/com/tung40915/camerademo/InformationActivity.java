package com.tung40915.camerademo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class InformationActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView breed,height,weight,detail;

    final String DatabaseName = "DogBreed.sqlite";
    SQLiteDatabase database;

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);


        toolbar = (Toolbar) findViewById(R.id.toolBar_IDENTIFYACTIVITY);
        height = (TextView) findViewById(R.id.tvHeight);
        breed = (TextView) findViewById(R.id.tvBreed);
        weight = (TextView) findViewById(R.id.tvWeight);
        detail = (TextView) findViewById(R.id.tvDetail);

        detail.setMovementMethod(new ScrollingMovementMethod());
        ActionBar();

        database = Database.initDatabase(this,DatabaseName);



        Bundle b = getIntent().getExtras();

        if(b != null)
            id = b.getString("key");

        Handle(id);

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

    private void Handle(String id)
    {
        if(id != "")
        {
            String query = "SELECT * FROM DogBreed WHERE ID = "+id + ";";
            Cursor cursor = database.rawQuery(query,null);


            if(cursor!=null)
            {
                cursor.moveToFirst();
                breed.setText(cursor.getString(1));
                weight.setText(cursor.getString(2));
                height.setText(cursor.getString(3));
                detail.setText(cursor.getString(4));

            }

        }
    }
}
