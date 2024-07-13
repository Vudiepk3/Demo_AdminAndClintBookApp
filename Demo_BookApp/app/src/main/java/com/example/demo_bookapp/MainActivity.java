package com.example.demo_bookapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.demo_bookapp.model.ImageModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout; // Layout của navigation drawer
    private final String[] subjectNames = {
            "Toán Học",
            "Văn Học",
            "Tiếng Anh",
            "Vật Lý",
            "Hoá Học",
            "Sinh Học",
            "Lịch Sử",
            "Địa Lý",
            "Giáo Dục Công Dân"
    }; // Danh sách tên các môn học
    private final int[] cardViewIds = {
            R.id.mathsCard,
            R.id.literatureCard,
            R.id.englishCard,
            R.id.physicsCard,
            R.id.chemistryCard,
            R.id.biologyCard,
            R.id.historyCard,
            R.id.geographyCard,
            R.id.civicEducationCard
    }; // Danh sách các ID của CardView tương ứng với các môn học

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Đặt layout cho hoạt động chính

        loadImageSlide(); // Tải ảnh cho slider
        loadAllDocument(); // Tải tất cả các tài liệu

        drawerLayout = findViewById(R.id.drawer_layout); // Lấy đối tượng DrawerLayout
        Toolbar toolbar = findViewById(R.id.toolbar); // Lấy đối tượng Toolbar
        toolbar.setTitle(""); // Đặt tiêu đề cho toolbar
        setSupportActionBar(toolbar); // Đặt toolbar làm ActionBar
        NavigationView navigationView = findViewById(R.id.nav_view); // Lấy đối tượng NavigationView
        navigationView.setNavigationItemSelectedListener(this); // Đặt listener cho NavigationView
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav); // Tạo toggle cho DrawerLayout
        drawerLayout.addDrawerListener(toggle); // Thêm toggle vào DrawerLayout
        toggle.syncState(); // Đồng bộ trạng thái toggle
    }

    private void loadImageSlide() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false); // Không cho phép hủy dialog khi nhấn ngoài
        builder.setView(R.layout.progress_layout); // Đặt layout cho dialog
        AlertDialog dialog = builder.create(); // Tạo dialog
        dialog.show(); // Hiển thị dialog

        ImageSlider imageSlider = findViewById(R.id.ImageSlide);
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        List<String> linkWebsites = new ArrayList<>(); // Danh sách lưu trữ các link website

        // Tham chiếu cơ sở dữ liệu Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("SlideImage");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                slideModels.clear(); // Xóa danh sách hình ảnh cũ
                linkWebsites.clear(); // Xóa danh sách link website cũ

                // Duyệt qua từng mục trong dữ liệu Firebase
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ImageModel imageModel = itemSnapshot.getValue(ImageModel.class);
                    if (imageModel != null && imageModel.getUrlImage() != null) {
                        slideModels.add(new SlideModel(imageModel.getUrlImage(), ScaleTypes.FIT)); // Thêm hình ảnh vào danh sách slide
                        linkWebsites.add(imageModel.getLinkWeb()); // Thêm link website vào danh sách
                    }
                }

                imageSlider.setImageList(slideModels, ScaleTypes.FIT); // Cập nhật danh sách hình ảnh

                imageSlider.setItemClickListener(i -> {
                    if (i >= 0 && i < linkWebsites.size()) {
                        String linkWebsite = linkWebsites.get(i);
                        if (linkWebsite != null && !linkWebsite.isEmpty() && !linkWebsite.equals("No Link Website")) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkWebsite));startActivity(intent); // Mở trang web bằng Intent
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(MainActivity.this, "Không thể mở đường dẫn web.", Toast.LENGTH_SHORT).show(); // Thông báo nếu không thể mở đường dẫn
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Không có đường dẫn web.", Toast.LENGTH_SHORT).show(); // Thông báo nếu không có đường dẫn web
                        }
                    }
                });
                new Handler().postDelayed(() -> {
                    if (dialog.isShowing()) {
                        dialog.dismiss(); // Đóng dialog sau khi tải xong dữ liệu
                    }
                }, 2000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss(); // Đóng ProgressDialog nếu có lỗi
                Toast.makeText(MainActivity.this, "Lỗi trong việc tải dữ liệu.", Toast.LENGTH_SHORT).show(); // Thông báo lỗi tải dữ liệu
            }
        });
    }

    private void loadAllDocument() {
        for (int i = 0; i < cardViewIds.length; i++) {
            CardView cardView = findViewById(cardViewIds[i]); // Lấy đối tượng CardView tương ứng với môn học
            final int index = i; // Lưu index cho OnClickListener
            cardView.setOnClickListener(v -> navigateToSubject(subjectNames[index])); // Đặt OnClickListener cho CardView để điều hướng đến môn học tương ứng
        }
    }

    private void navigateToSubject(String subjectName) {
        Intent subjectActivity = new Intent(MainActivity.this, ShowDocumentActivity.class); // Tạo Intent để điều hướng đến ShowDocumentActivity
        subjectActivity.putExtra("subjectName", subjectName); // Truyền tên môn học qua Intent
        startActivity(subjectActivity); // Bắt đầu hoạt động mới
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_vote) {
            openLink();
        } else if (itemId == R.id.nav_share) {
            shareApplication();
        } else if (itemId == R.id.nav_feedback) {
            sendFeedback();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }
    private void openLink() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dhcn.MyHAUI&pcampaignid=web_share"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareApplication() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
            intent.putExtra(Intent.EXTRA_TEXT, "This is my text");
            startActivity(Intent.createChooser(intent, "Hãy chia sẻ đến với mọi người"));
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendFeedback() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:vulq2k3@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Xin Chào");
            intent.putExtra(Intent.EXTRA_TEXT, "Hi");
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu); // Inflate menu từ tài nguyên menu_options
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            Toast.makeText(this, "Chức năng đang trong quá trình phát triển", Toast.LENGTH_SHORT).show();
        }
        // Xử lý sự kiện chọn item trong menu
        return super.onOptionsItemSelected(item);
    }

}
