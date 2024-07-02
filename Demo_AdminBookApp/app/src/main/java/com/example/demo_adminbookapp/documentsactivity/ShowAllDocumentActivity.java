package com.example.demo_adminbookapp.documentsactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.demo_adminbookapp.ManageImageActivity;
import com.example.demo_adminbookapp.R;
import com.example.demo_adminbookapp.adapter.DocumentAdapter;
import com.example.demo_adminbookapp.model.DocumentModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowAllDocumentActivity extends AppCompatActivity {
    private List<DocumentModel> mList = new ArrayList<>();
    private DocumentAdapter documentAdapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private LinearLayout linearLayout;
    private TextView txtNumberDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_file);
        loadDocument();

    }
    private void loadDocument(){
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<DocumentModel> options =
                new FirebaseRecyclerOptions.Builder<DocumentModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("documents"), DocumentModel.class)
                        .build();

        documentAdapter = new DocumentAdapter(options);
        recyclerView.setAdapter(documentAdapter);
        documentAdapter.startListening();
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowAllDocumentActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();
        txtNumberDocument=(TextView) findViewById(R.id.txtNumberDocument);
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference().child("documents");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                txtNumberDocument.setText("Số Đề Thi Hiện Có: " + count);
                dialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });

    }




}
