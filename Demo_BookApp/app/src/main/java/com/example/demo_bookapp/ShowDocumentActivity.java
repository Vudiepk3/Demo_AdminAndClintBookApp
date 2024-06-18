package com.example.demo_bookapp;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo_bookapp.adapter.DocumentAdapter;
import com.example.demo_bookapp.model.DocumentModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShowDocumentActivity extends AppCompatActivity {

    private List<DocumentModel> mList = new ArrayList<>();
    private DocumentAdapter documentAdapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private LinearLayout linearLayout;
    private TextView txtNumberDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_document);
        loadDocument();

    }
    private void loadDocument() {
        String subjectName = getIntent().getStringExtra("subjectName");
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        txtNumberDocument = (TextView) findViewById(R.id.txtNumberDocument);

        DatabaseReference documentsRef = FirebaseDatabase.getInstance().getReference().child("documents");
        Query query = documentsRef.orderByChild("subjectName").equalTo(subjectName);

        FirebaseRecyclerOptions<DocumentModel> options =
                new FirebaseRecyclerOptions.Builder<DocumentModel>()
                        .setQuery(query, DocumentModel.class)
                        .build();

        documentAdapter = new DocumentAdapter(options);
        recyclerView.setAdapter(documentAdapter);
        documentAdapter.startListening();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                txtNumberDocument.setText("Số Lượng Đề Thi Hiện Có:" + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors here
            }
        });
    }
    private void setSearchView(){
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

    }
    private void filterList(String query) {
        if (query != null) {
            List<DocumentModel> filteredList = new ArrayList<>();
            for (DocumentModel item : mList) {
                if (item.getTitle().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                    filteredList.add(item);
                }
            }
            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No Data found", Toast.LENGTH_SHORT).show();
            } else {
                documentAdapter.setFilteredList(filteredList);
            }
        }
    }

}
