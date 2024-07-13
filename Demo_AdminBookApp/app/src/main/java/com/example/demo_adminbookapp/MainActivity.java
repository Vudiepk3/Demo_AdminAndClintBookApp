package com.example.demo_adminbookapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.demo_adminbookapp.databinding.ActivityMainBinding;
import com.example.demo_adminbookapp.documentsactivity.ManageDocumentActivity;
import com.example.demo_adminbookapp.documentsactivity.UploadDocumentActivity;

public class MainActivity extends AppCompatActivity {
    CardView documentCard, imageCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các thành phần giao diện
        documentCard = findViewById(R.id.documentCard);
        imageCard = findViewById(R.id.imageCard);

        documentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent documentIntent = new Intent(MainActivity.this, ManageDocumentActivity.class);
                startActivity(documentIntent);
            }
        });

        imageCard.setOnClickListener(new View.OnClickListener() {@Override
        public void onClick(View v) {
            Intent imageIntent = new Intent(MainActivity.this, ManageImageActivity.class);
            startActivity(imageIntent);
        }
        });
    }
}