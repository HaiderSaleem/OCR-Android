package com.debugger.ocr.activity;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.debugger.ocr.R;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGallery = findViewById(R.id.btn_gallery);
        Button btnCamera = findViewById(R.id.btn_camera);

        btnGallery.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, GalleryActivity.class);
            startActivity(i);
        });

        btnCamera.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(i);
        });
    }

}