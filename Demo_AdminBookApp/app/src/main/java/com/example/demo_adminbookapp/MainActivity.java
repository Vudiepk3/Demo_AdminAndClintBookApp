package com.example.demo_adminbookapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demo_adminbookapp.databinding.ActivityMainBinding;
import com.example.demo_adminbookapp.documentsactivity.ManageFilePDFActivity;

public class MainActivity extends AppCompatActivity  {
    // Ràng buộc các thành phần giao diện
    ActivityMainBinding binding;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ràng buộc giao diện
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.documentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent documentIntent = new Intent(MainActivity.this, ManageFilePDFActivity.class);
                startActivity(documentIntent);
            }
        });
        binding.imageCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent(MainActivity.this,ManageImageActivity.class);
                startActivity(imageIntent);
            }
        });

    }


}