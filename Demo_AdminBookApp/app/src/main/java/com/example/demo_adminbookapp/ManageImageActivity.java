package com.example.demo_adminbookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.demo_adminbookapp.adapter.ImageAdapter;
import com.example.demo_adminbookapp.model.ImageModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageImageActivity extends AppCompatActivity {

    FloatingActionButton fab;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    RecyclerView recyclerView;
    List<ImageModel> dataList;
    ImageAdapter adapter;
    SearchView searchView;
    TextView txtNumberImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_image);

        // Khởi tạo và ràng buộc RecyclerView và các thành phần giao diện khác
        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        // Thiết lập LayoutManager cho RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ManageImageActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Hiển thị dialog tiến trình
        AlertDialog.Builder builder = new AlertDialog.Builder(ManageImageActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Khởi tạo danh sách dữ liệu và Adapter cho RecyclerView
        dataList = new ArrayList<>();
        adapter = new ImageAdapter(ManageImageActivity.this, dataList);
        recyclerView.setAdapter(adapter);

        // Hiển thị số lượng ảnh hiện có
        txtNumberImage = findViewById(R.id.txtNumberImage);
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("SlideImage");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                txtNumberImage.setText("Số Ảnh Hiện Có: " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });

        // Lắng nghe sự kiện thay đổi dữ liệu trên Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("SlideImage");
        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ImageModel dataClass = itemSnapshot.getValue(ImageModel.class);
                    if (dataClass != null) {
                        dataClass.setKey(itemSnapshot.getKey());
                        dataList.add(dataClass);
                    }
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });

        // Xử lý tìm kiếm ảnh
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        // Xử lý sự kiện click vào Floating Action Button (FAB) để tải lên ảnh mới
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ManageImageActivity.this, UploadImageActivity.class);
            startActivity(intent);
        });
    }

    // Phương thức để tìm kiếm ảnh trong danh sách hiện tại
    public void searchList(String text) {
        ArrayList<ImageModel> searchList = new ArrayList<>();
        for (ImageModel dataClass : dataList) {
            if (dataClass.getNameImage().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đăng ký lại eventListener khi Activity trở lại hoạt động
        if (databaseReference != null && eventListener != null) {
            databaseReference.addValueEventListener(eventListener);
            // Hoặc addListenerForSingleValueEvent() nếu chỉ cần tải dữ liệu một lần
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký eventListener khi Activity tạm dừng để tránh rò rỉ bộ nhớ
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký eventListener khi Activity bị hủy để tránh rò rỉ bộ nhớ
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
    }
}
