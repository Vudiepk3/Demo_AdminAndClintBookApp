package com.example.demo_bookapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen); // Đặt layout cho hoạt động splash screen

        // Ẩn thanh công cụ nếu nó tồn tại
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Tạo Handler để chuyển sang MainActivity sau 2.5 giây (2500 milliseconds)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class)); // Chuyển sang MainActivity
                finish(); // Đóng SplashScreenActivity để người dùng không thể quay lại màn hình này
            }
        }, 2500); // Thời gian chờ 2.5 giây
    }
}
