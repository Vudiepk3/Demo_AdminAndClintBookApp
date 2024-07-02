package com.example.demo_adminbookapp.adapter;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demo_adminbookapp.R;
import com.example.demo_adminbookapp.documentsactivity.ViewPDFActivity;
import com.example.demo_adminbookapp.model.DocumentModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.List;

public class DocumentAdapter extends FirebaseRecyclerAdapter<DocumentModel, DocumentAdapter.CategoryViewHolder> {

    private DatabaseReference databaseReference;
    private List<DocumentModel> filteredList;
    private List<DocumentModel> documentList;
    @Override
    protected void onBindViewHolder(@NonNull final CategoryViewHolder holder, int position, @NonNull final DocumentModel model) {
        holder.txtTitle.setText(model.getTitle());
        holder.txtSubject.setText(model.getSubjectName());
        holder.readBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.readBook.getContext(), ViewPDFActivity.class);
                intent.putExtra("title", model.getTitle());
                intent.putExtra("pdf", model.getLinkDocument());
                holder.readBook.getContext().startActivity(intent);
            }
        });
        holder.deleteBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy key của node cần xóa
                String nodeKey = getRef(holder.getAdapterPosition()).getKey();

                // Lấy tên file từ model
                String fileName = model.getTitle();

                // Tạo dialog xác nhận
                new AlertDialog.Builder(holder.deleteBook.getContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa file " + fileName + "?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // Xóa node từ Firebase Realtime Database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("documents");
                            databaseReference.child(nodeKey).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        // Xóa thành công
                                        Toast.makeText(holder.readBook.getContext(), "Đã xóa file"+fileName, Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Xóa thất bại
                                        Toast.makeText(holder.readBook.getContext(), "Lỗi khi xóa file", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        holder.shareBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the PDF link from your model
                String pdfLink = model.getLinkDocument(); // Assuming this now returns a URL string

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain"); // Set MIME type for text
                shareIntent.putExtra(Intent.EXTRA_TEXT, pdfLink); // Put the link in the intent

                // Optional: Set a title for the share dialog
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, model.getTitle());

                // Start the chooser to allow the user to select a sharing app
                holder.readBook.getContext().startActivity(Intent.createChooser(shareIntent, "Share PDF Link"));
            }
        });

    }

    @NonNull
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new CategoryViewHolder(view);
    }

    public void searchDataList(List<DocumentModel> searchList) {
        documentList = searchList;
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
        ImageView deleteBook,readBook,shareBook;
        TextView txtSubject,txtTitle;
        // ... other views

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            readBook= itemView.findViewById(R.id.imgRead);
            deleteBook = itemView.findViewById(R.id.imgDelete);
            shareBook = itemView.findViewById(R.id.imgShare);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            // ... initialize other views
        }
    }
}