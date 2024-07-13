package com.example.demo_adminbookapp.documentsactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.demo_adminbookapp.R;
import com.example.demo_adminbookapp.adapter.DocumentAdapter;
import com.example.demo_adminbookapp.model.DocumentModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageDocumentActivity extends AppCompatActivity {
    private List<DocumentModel> mList = new ArrayList<>();
    private DocumentAdapter documentAdapter;
    private RecyclerView recyclerView;
    private TextView txtNumberDocument;
    private FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_document);
        // Khởi tạo và hiển thị danh sách các tài liệu
        loadDocument();
        upLoadDocument();
    }

    private void loadDocument() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Cấu hình FirebaseRecyclerOptions để hiển thị danh sách tài liệu từ Firebase Realtime Database
        FirebaseRecyclerOptions<DocumentModel> options =
                new FirebaseRecyclerOptions.Builder<DocumentModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("documents"), DocumentModel.class)
                        .build();

        // Khởi tạo adapter và kết nối với RecyclerView
        documentAdapter = new DocumentAdapter(options);
        recyclerView.setAdapter(documentAdapter);
        documentAdapter.startListening();

        // Hiển thị dialog tiến trình khi đang tải dữ liệu
        AlertDialog.Builder builder = new AlertDialog.Builder(ManageDocumentActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Hiển thị số lượng tài liệu hiện có dựa trên số lượng mục con của "documents" trong Firebase Database
        txtNumberDocument = findViewById(R.id.txtNumberDocument);
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("documents");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                txtNumberDocument.setText("Số Đề Thi Hiện Có: " + count); // Hiển thị số lượng tài liệu
                dialog.dismiss(); // Đóng dialog khi đã có dữ liệu
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss(); // Đóng dialog nếu có lỗi xảy ra
            }
        });
    }
    private void upLoadDocument(){
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ManageDocumentActivity.this, UploadDocumentActivity.class);
            startActivity(intent);
        });
    }
}
