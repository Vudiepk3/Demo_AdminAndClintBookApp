package com.example.demo_bookapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.github.clans.fab.FloatingActionButton;
import java.io.File;

public class ViewPDFActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    WebView pdfView;
    FloatingActionButton btnDownload, btnShare;
    ProgressDialog progressDialog;
    private long downloadId;
    private BroadcastReceiver onDownloadComplete;
    private String fileUrl; // Biến để lưu trữ URL của file PDF

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdfactivity);

        pdfView = findViewById(R.id.viewPdf);
        btnShare = findViewById(R.id.btnShare);
        btnDownload = findViewById(R.id.btnDownload);

        initializeWebView();
        setupDownloadButton();
        setupShareButton();
        registerDownloadReceiver();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        pdfView.getSettings().setJavaScriptEnabled(true);

        fileUrl = getIntent().getStringExtra("pdf");

        pdfView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dismissProgressDialog();
            }
        });

        loadPdfFile(fileUrl);
    }

    private void loadPdfFile(String fileUrl) {
        try {
            String encodedUrl = Uri.encode(fileUrl);
            pdfView.loadUrl("http://docs.google.com/gview?embedded=true&url=" + encodedUrl);
            showProgressDialog("Đang mở tài liệu...");
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Lỗi", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDownloadButton() {
        btnDownload.setOnClickListener(v -> {
            showDownloadConfirmationDialog();
        });
    }

    private void setupShareButton() {
        btnShare.setOnClickListener(v -> {
            sharePdfLink();
        });
    }

    private void showDownloadConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn có muốn tải File xuống ?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        String filename = getIntent().getStringExtra("title");
                        String fileUrl = getIntent().getStringExtra("pdf");
                        if (ContextCompat.checkSelfPermission(ViewPDFActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            downloadFile(fileUrl, filename);
                        } else {
                            ActivityCompat.requestPermissions(ViewPDFActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void registerDownloadReceiver() {
        onDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    dismissProgressDialog();

                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    Uri downloadUri = downloadManager.getUriForDownloadedFile(downloadId);
                    if (downloadUri != null) {
                        String filePath = downloadUri.getPath();
                        Snackbar.make(findViewById(android.R.id.content), "Tải xuống thành công", Snackbar.LENGTH_LONG)
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        super.onDismissed(snackbar, event);
                                        Toast.makeText(ViewPDFActivity.this, "Tài liệu được lưu tại:: " + filePath, Toast.LENGTH_LONG).show();
                                    }
                                })
                                .show();
                    }
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
        }
    }

    private void downloadFile(String pdfLink, String fileName) {
        try {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(pdfLink);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(fileName)
                    .setMimeType("application/pdf")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, File.separator + fileName);

            downloadId = downloadManager.enqueue(request);
            showProgressDialog("Đang tải tài liệu xuống...");

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdfLink() {
        if (fileUrl != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, fileUrl);
            startActivity(Intent.createChooser(shareIntent, "Share PDF Link using"));
        } else {
            Toast.makeText(this, "File chưa được tải xuống.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(ViewPDFActivity.this);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String filename = getIntent().getStringExtra("title");
                String fileUrl = getIntent().getStringExtra("pdf");
                downloadFile(fileUrl, filename);
            } else {
                Toast.makeText(this, "Permission denied. Cannot download.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
        dismissProgressDialog();
    }
}
