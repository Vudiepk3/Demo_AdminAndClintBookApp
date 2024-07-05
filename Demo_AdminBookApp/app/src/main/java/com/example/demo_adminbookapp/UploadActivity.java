package com.example.demo_adminbookapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.demo_adminbookapp.model.ImageModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Calendar;

public class UploadActivity extends AppCompatActivity {

    ImageView uploadUrlImage;
    Button saveButton;
    EditText uploadNameImage, uploadLinkWeb, uploadNoteImage;
    String imageURL;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Ánh xạ và thiết lập các thành phần giao diện
        uploadUrlImage = findViewById(R.id.uploadUrlImage);
        uploadNameImage = findViewById(R.id.uploadNameImage);
        uploadLinkWeb = findViewById(R.id.uploadLinkWeb);
        uploadNoteImage = findViewById(R.id.uploadNoteImage);
        saveButton = findViewById(R.id.saveButton);

        // Đăng ký ActivityResultLauncher để chọn hình ảnh từ thư viện
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            uri = data.getData();
                            uploadUrlImage.setImageURI(uri);
                        } else {
                            Toast.makeText(UploadActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Xử lý sự kiện khi click vào ImageView để chọn hình ảnh mới
        uploadUrlImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        // Xử lý sự kiện khi click vào nút Save
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveData();
                } catch (Exception e) {
                    Toast.makeText(UploadActivity.this, "Lỗi trong quá trình cập nhật thông tin", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    // Phương thức để lưu dữ liệu lên Firebase
    public void saveData(){

        // Tham chiếu đến thư mục trên Firebase Storage để lưu hình ảnh
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Slide Images")
                .child(uri.getLastPathSegment());

        // Hiển thị dialog tiến trình
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Upload hình ảnh lên Firebase Storage
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // Lấy đường dẫn URL của hình ảnh đã upload
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri urlImage = uriTask.getResult();
                imageURL = urlImage.toString();

                // Gọi phương thức để upload dữ liệu lên Firebase Realtime Database
                uploadData();
                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
            }
        });
    }

    // Phương thức để upload dữ liệu lên Firebase Realtime Database
    public void uploadData(){

        // Lấy các giá trị từ EditText và kiểm tra null
        String nameImage = uploadNameImage.getText().toString().trim();
        String linkWeb = uploadLinkWeb.getText().toString().trim();
        String noteImage = uploadNoteImage.getText().toString().trim();
        nameImage = nameImage.isEmpty() ? "No Name Image" : nameImage;
        linkWeb = linkWeb.isEmpty() ? "No Link Web" : linkWeb;
        noteImage = noteImage.isEmpty() ? "No Note Image" : noteImage;

        // Tạo đối tượng ImageModel mới và đẩy lên Firebase Realtime Database
        ImageModel dataClass = new ImageModel(imageURL, nameImage, linkWeb, noteImage);

        // Lấy ngày giờ hiện tại làm key để lưu vào Firebase Realtime Database
        String currentDate = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        FirebaseDatabase.getInstance().getReference("SlideImage").child(currentDate)
                .setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            // Hiển thị thông báo lưu thành công và kết thúc Activity sau 1 giây
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(UploadActivity.this, "Đã lưu thành công", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }, 1000);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
