package com.example.demo_bookapp;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo_bookapp.adapter.DocumentAdapter;
import com.example.demo_bookapp.model.DocumentModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowDocumentActivity extends AppCompatActivity {
    DatabaseReference databaseReference; // Tham chiếu cơ sở dữ liệu Firebase
    ValueEventListener eventListener; // Listener cho thay đổi dữ liệu
    RecyclerView recyclerView; // RecyclerView để hiển thị danh sách tài liệu
    List<DocumentModel> dataList; // Danh sách dữ liệu tài liệu
    DocumentAdapter adapter; // Adapter cho RecyclerView
    androidx.appcompat.widget.SearchView searchView; // Thanh tìm kiếm
    TextView txtNumberImage; // TextView để hiển thị số lượng tài liệu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_document); // Đặt layout cho hoạt động

        recyclerView = findViewById(R.id.recyclerView); // Lấy đối tượng RecyclerView từ layout

        searchView = findViewById(R.id.search); // Lấy đối tượng SearchView từ layout
        searchView.clearFocus(); // Bỏ focus khỏi SearchView

        GridLayoutManager gridLayoutManager = new GridLayoutManager(ShowDocumentActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager); // Đặt LayoutManager cho RecyclerView

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ShowDocumentActivity.this);
        builder.setCancelable(false); // Không cho phép hủy dialog khi nhấn ngoài
        builder.setView(R.layout.progress_layout); // Đặt layout cho dialog
        AlertDialog dialog = builder.create(); // Tạo dialog
        dialog.show(); // Hiển thị dialog


        String subjectName = getIntent().getStringExtra("subjectName"); // Lấy tên môn học từ Intent
        txtNumberImage = findViewById(R.id.txtNumberDocument); // Lấy đối tượng TextView từ layout

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("documents");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = 0; // Biến đếm số lượng tài liệu có cùng tên với subjectName
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    DocumentModel dataClass = itemSnapshot.getValue(DocumentModel.class);
                    if (dataClass != null) {
                        assert subjectName != null;
                        if (subjectName.equals(dataClass.getSubjectName())) {
                            count++; // Tăng biến đếm khi tìm thấy tài liệu có tên môn học trùng khớp
                        }
                    }
                }
                txtNumberImage.setText("Số Tài Liệu Hiện Có: " + count); // Hiển thị số lượng tài liệu
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("documents");
        dialog.show(); // Hiển thị dialog

        dataList = new ArrayList<>(); // Khởi tạo danh sách dữ liệu
        adapter = new DocumentAdapter(ShowDocumentActivity.this, dataList); // Khởi tạo adapter
        recyclerView.setAdapter(adapter); // Đặt adapter cho RecyclerView
        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear(); // Xóa dữ liệu cũ
                for (DataSnapshot itemSnapshot: snapshot.getChildren()) {
                    DocumentModel dataClass = itemSnapshot.getValue(DocumentModel.class); // Lấy dữ liệu từ Firebase
                    if (dataClass != null && dataClass.getSubjectName() != null && dataClass.getSubjectName().equals(subjectName)) {
                        dataList.add(dataClass); // Thêm dữ liệu mới vào danh sách
                    }
                }
                adapter.notifyDataSetChanged(); // Thông báo cho adapter để cập nhật dữ liệu
                dialog.dismiss(); // Đóng dialog
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss(); // Đóng dialog nếu có lỗi
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText); // Tìm kiếm danh sách tài liệu theo từ khóa
                return true;
            }
        });
    }

    public void searchList(String text) {
        ArrayList<DocumentModel> searchList = new ArrayList<>(); // Danh sách kết quả tìm kiếm
        for (DocumentModel dataClass : dataList) {
            if (dataClass.getTitle().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(dataClass); // Thêm tài liệu vào danh sách kết quả nếu tiêu đề chứa từ khóa
            }
        }
        if (searchList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy tài liệu", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo nếu không có tài liệu nào trong danh sách kết quả
        }
        adapter.searchDataList(searchList); // Cập nhật dữ liệu trong adapter
    }

}
