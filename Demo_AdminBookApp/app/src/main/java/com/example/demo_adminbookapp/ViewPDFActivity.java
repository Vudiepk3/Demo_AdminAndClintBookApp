package com.example.demo_adminbookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.net.URLEncoder;

public class ViewPDFActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    WebView pdfview;
    FloatingActionButton btnDownload;
    ProgressBar progressBar;
    private long downloadId;
    private BroadcastReceiver onDownloadComplete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpdf);

        pdfview = findViewById(R.id.viewPdf);
        btnDownload = findViewById(R.id.btnDownload);
        progressBar = findViewById(R.id.progressBar);

        initializeWebView();
        setupDownloadButton();
        registerDownloadReceiver();
    }
    private void initializeWebView() {
        pdfview.getSettings().setJavaScriptEnabled(true);

        String filename = getIntent().getStringExtra("title");
        String fileurl = getIntent().getStringExtra("pdf");

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle(filename);
        pd.setMessage("Opening....!!!");

        pdfview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                pd.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pd.dismiss();
            }
        });

        pdfview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("WebView", consoleMessage.message());
                return true;
            }
        });

        loadPdfFile(fileurl);
    }
    private void loadPdfFile(String fileurl) {
        try {
            String encodedUrl = URLEncoder.encode(fileurl, "UTF-8");
            pdfview.loadUrl("http://docs.google.com/gview?embedded=true&url=" + encodedUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show();
        }
    }
    private void setupDownloadButton() {
        btnDownload.setOnClickListener(v -> {
            String filename = getIntent().getStringExtra("title");
            String fileurl = getIntent().getStringExtra("pdf");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadFile(this, fileurl, filename);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        });
    }
    private void registerDownloadReceiver() {
        onDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    progressBar.setVisibility(android.view.View.GONE); // Ẩn progressBar

                    // Lấy đường dẫn đến file đã tải xuống
                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    Uri downloadUri = downloadManager.getUriForDownloadedFile(downloadId);
                    if (downloadUri != null) {
                        String filePath = downloadUri.getPath();

                        // Hiển thị Snackbar "Download completed" trước
                        Snackbar.make(findViewById(android.R.id.content), "Download completed", Snackbar.LENGTH_LONG)
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        super.onDismissed(snackbar, event);
                                        // Sau khi Snackbar biến mất, hiển thị Toast với đường dẫn đến file
                                        Toast.makeText(ViewPDFActivity.this, "File saved at: " + filePath, Toast.LENGTH_LONG).show();
                                    }
                                })
                                .show();
                    }
                }
            }
        };

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    private void downloadFile(Context context, String pdfLink, String fileName) {
        try {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(pdfLink);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(fileName)
                    .setMimeType("application/pdf")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            File.separator + fileName
                    );

            downloadId = downloadManager.enqueue(request); // Lưu ID tải xuống

            progressBar.setVisibility(android.view.View.VISIBLE); // Hiển thị progressBar
            Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error downloading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, bắt đầu tải xuống file
                String filename = getIntent().getStringExtra("title");
                String fileurl = getIntent().getStringExtra("pdf");
                downloadFile(this, fileurl, filename);
            } else {
                // Quyền bị từ chối, hiển thị thông báo cho người dùng
                Toast.makeText(this, "Permission denied. Cannot download file.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }
}