package com.example.demo_bookapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo_bookapp.ViewPDFActivity;
import com.example.demo_bookapp.model.DocumentModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.example.demo_bookapp.R;

import java.util.List;

public class DocumentAdapter extends FirebaseRecyclerAdapter<DocumentModel, DocumentAdapter.CategoryViewHolder> {

    private DatabaseReference databaseReference;
    private List<DocumentModel> filteredList;

    @Override
    protected void onBindViewHolder(@NonNull final CategoryViewHolder holder, int position, @NonNull final DocumentModel model) {
        holder.txtTitle.setText(model.getTitle());
        holder.txtSubject.setText(model.getSubjectName());
        holder.txtYear.setText("2023");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), ViewPDFActivity.class);
                intent.putExtra("title", model.getTitle());
                intent.putExtra("pdf", model.getLinkDocument());
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }
    @NonNull
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new CategoryViewHolder(view);
    }

    public void setFilteredList(List<DocumentModel> filteredList) {
        this.filteredList = filteredList;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return filteredList == null ? super.getItemCount() : filteredList.size();
    }

    // ... (Các phương thức khác của adapter)

    public void filter(String text) {
        Query query = getRef(0).orderByChild("title").startAt(text).endAt(text + "\uf8ff");
        FirebaseRecyclerOptions<DocumentModel> filteredOptions =
                new FirebaseRecyclerOptions.Builder<DocumentModel>()
                        .setQuery(query, DocumentModel.class)
                        .build();
        updateOptions(filteredOptions);
    }
    public DocumentAdapter(@NonNull FirebaseRecyclerOptions<DocumentModel> options) {
        super(options);
        this.databaseReference = FirebaseDatabase.getInstance().getReference("documents");
    }


    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubject,txtTitle,txtYear;
        // ... other views

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtYear = itemView.findViewById(R.id.txtYear);

        }
    }
}
