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

    TextView detailNameImage,detailLinkWeb,detailNoteImage;
    ImageView detailImage;
    FloatingActionButton deleteButton, editButton;
    String key = "";
    String imageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailNameImage = findViewById(R.id.detailNameImage);
        detailLinkWeb = findViewById(R.id.detailLinkWeb);
        detailNoteImage = findViewById(R.id.detailNoteImage);
        detailImage = findViewById(R.id.detailUrlImage);

        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            detailNameImage.setText(bundle.getString("NameImage"));
            detailLinkWeb.setText(bundle.getString("LinkWeb"));
            detailNoteImage.setText(bundle.getString("NoteImage"));
            key = bundle.getString("Key");
            imageUrl = bundle.getString("UrlImage");
            Glide.with(this).load(bundle.getString("UrlImage")).into(detailImage);
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                builder.setTitle("Confirm Deletion");
                builder.setMessage("Bạn thực sự muốn xóa ảnh?");
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SlideImage");
                        FirebaseStorage storage = FirebaseStorage.getInstance();

                        StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                reference.child(key).removeValue();
                                Toast.makeText(DetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), ManageImageActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DetailActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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