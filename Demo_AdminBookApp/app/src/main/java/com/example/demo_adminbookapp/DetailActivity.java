package com.example.demo_adminbookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DetailActivity extends AppCompatActivity {

    TextView detailNameImage, detailLinkWeb, detailNoteImage;
    ImageView detailImage;
    FloatingActionButton deleteButton, editButton;
    String key = "";
    String imageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Ánh xạ các thành phần giao diện
        detailNameImage = findViewById(R.id.detailNameImage);
        detailLinkWeb = findViewById(R.id.detailLinkWeb);
        detailNoteImage = findViewById(R.id.detailNoteImage);
        detailImage = findViewById(R.id.detailUrlImage);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);

        // Nhận dữ liệu từ Intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            detailNameImage.setText(bundle.getString("NameImage"));
            detailLinkWeb.setText(bundle.getString("LinkWeb"));
            detailNoteImage.setText(bundle.getString("NoteImage"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("UrlImage");

            // Load hình ảnh sử dụng Glide
            Glide.with(this).load(bundle.getString("UrlImage")).into(detailImage);
        }

        // Xử lý sự kiện khi người dùng nhấn nút Xóa
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hiển thị dialog xác nhận xóa
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                builder.setTitle("Xác nhận xóa");
                builder.setMessage("Bạn thực sự muốn xóa ảnh?");
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Xử lý xóa ảnh và dữ liệu tương ứng trên Firebase
                        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SlideImage");
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);

                        // Xóa ảnh trên Firebase Storage
                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Nếu xóa thành công, xóa dữ liệu trong Firebase Realtime Database và hiển thị thông báo
                                reference.child(key).removeValue();
                                Toast.makeText(DetailActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), ManageImageActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Nếu xảy ra lỗi trong quá trình xóa ảnh
                                Toast.makeText(DetailActivity.this, "Xóa không thành công: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Người dùng chọn hủy bỏ xóa
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Xử lý sự kiện khi người dùng nhấn nút Sửa
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chuyển sang Activity UpdateActivity để chỉnh sửa thông tin ảnh
                Intent intent = new Intent(DetailActivity.this, UpdateActivity.class)
                        .putExtra("NameImage", detailNameImage.getText().toString())
                        .putExtra("LinkWeb", detailLinkWeb.getText().toString())
                        .putExtra("NoteImage", detailNoteImage.getText().toString())
                        .putExtra("UrlImage", imageUrl)
                        .putExtra("Key", key);
                startActivity(intent);
            }
        });
    }
}
