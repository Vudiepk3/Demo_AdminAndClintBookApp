package com.example.demo_adminbookapp.documentsactivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.example.demo_adminbookapp.R;
import com.example.demo_adminbookapp.databinding.ActivityManageFilePdfactivityBinding;
import com.example.demo_adminbookapp.model.DocumentModel;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class ManageFilePDFActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // Ràng buộc các thành phần giao diện
    ActivityManageFilePdfactivityBinding binding;

    // Khởi tạo Firebase storage và database
    FirebaseStorage storage;
    FirebaseDatabase database;

    // URI của tệp đã chọn
    Uri file;

    // URI của tệp PDF đã chọn
    private Uri pdfFileUri;

    // Tên tệp của tệp PDF đã chọn
    private String fileName;

    // Mảng các tên môn học
    String[] courses = {"Toán Học", "Văn Học", "Tiếng Anh",
            "Vật Lý", "Hoá Học", "Sinh Học",
            "Lịch Sử", "Địa Lý", "Giáo Dục Công Dân", "Thông Tin Khác"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ràng buộc giao diện
        binding = ActivityManageFilePdfactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo các thành phần Firebase
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Thiết lập click listener để chọn tệp PDF
        binding.dataTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
            }
        });

        // Khởi tạo và thiết lập Spinner
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        // Tạo và thiết lập ArrayAdapter cho Spinner
        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_spinner_item, courses);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ad);

        // Thiết lập click listener để tải tệp lên
        binding.btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = binding.spinner.getSelectedItem().toString();
                String title = binding.editTitle.getText().toString();
                if (pdfFileUri == null) {
                    showToast("Vui lòng chọn tệp PDF");
                } else if (title.isEmpty()) {
                    showToast("Nhập tiêu đề");
                } else {
                    uploadFile(pdfFileUri, title, subject);
                }
            }
        });

        // Thiết lập click listener để hủy chọn
        binding.cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetViews();
            }
        });
        binding.btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showAllDocumentActivity = new Intent(ManageFilePDFActivity.this, ShowAllDocumentActivity.class);
                startActivity(showAllDocumentActivity);
            }

        });

    }

    // Launcher để chọn tệp PDF
    private ActivityResultLauncher<String> launcher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    pdfFileUri = uri;
                    if ((uri != null)) {
                        assert DocumentFile.fromSingleUri(ManageFilePDFActivity.this, uri) != null;
                        fileName = DocumentFile.fromSingleUri(ManageFilePDFActivity.this, uri).getName();
                    } else {
                        fileName = null;
                    }
                    binding.editTitle.setText(fileName != null ? fileName : "");
                    if (uri != null) {
                        binding.pdfLogo.setVisibility(View.GONE);
                        binding.cancle.setVisibility(View.VISIBLE);
                        binding.dataTransfer.setVisibility(View.VISIBLE);
                    }
                }
            });

    // Yêu cầu quyền truy cập bộ nhớ
    private void requestStoragePermission() {
        Dexter.withContext(ManageFilePDFActivity.this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        launcher.launch("application/pdf");
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        showToast("Quyền bị từ chối");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    // Tải tệp lên Firebase Storage
    private void uploadFile(Uri fileUri, String title, String subject) {
        // Hiển thị hộp thoại tiến trình khi tải lên tệp
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Đang tải tệp lên...");
        dialog.show();

        // Tạo tham chiếu đến vị trí lưu trữ trên Firebase Storage với tên tệp duy nhất
        StorageReference reference = storage.getReference().child("DocumentsUpload/" + System.currentTimeMillis() + ".pdf");

        // Bắt đầu quá trình tải lên tệp
        reference.putFile(fileUri)
                // Xử lý sự kiện khi tải lên thành công
                .addOnSuccessListener(taskSnapshot -> {
                    // Lấy URL tải xuống của tệp đã tải lên
                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Tạo đối tượng DocumentModel với thông tin về tệp và đẩy dữ liệu lên cơ sở dữ liệu Firebase
                        DocumentModel model = new DocumentModel();
                        model.setTitle(title);
                        model.setSubjectName(subject);
                        model.setLinkDocument(uri.toString());
                        database.getReference().child("documents").push().setValue(model);

                        // Đóng hộp thoại tiến trình và thông báo cho người dùng rằng tệp đã được tải lên thành công
                        dialog.dismiss();
                        showToast("Tệp đã được tải lên");

                        // Đặt lại giao diện người dùng
                        resetViews();
                    });
                })
                // Theo dõi tiến trình tải lên và cập nhật hộp thoại tiến trình
                .addOnProgressListener(snapshot -> {
                    // Tính toán phần trăm hoàn thành và cập nhật nội dung của hộp thoại tiến trình
                    float percentage = (float) (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    dialog.setMessage("Đang tải lên: " + (int) percentage + "%");
                });
    }

    // Đặt lại các thành phần giao diện sau khi chọn hoặc hủy
    private void resetViews() {
        binding.pdfLogo.setVisibility(View.VISIBLE);
        binding.cancle.setVisibility(View.GONE);
        binding.dataTransfer.setVisibility(View.VISIBLE);
        binding.editTitle.setText("");
        pdfFileUri = null;
    }

    // Hiển thị thông báo toast
    private void showToast(String message) {
        Toast.makeText(ManageFilePDFActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Xử lý sự kiện khi một mục được chọn
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Xử lý sự kiện khi không có mục nào được chọn
    }
}