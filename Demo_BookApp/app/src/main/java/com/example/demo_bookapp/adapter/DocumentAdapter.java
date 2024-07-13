package com.example.demo_bookapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.demo_bookapp.R;
import com.example.demo_bookapp.ViewPDFActivity;
import com.example.demo_bookapp.model.DocumentModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

// Lớp Adapter để hiển thị danh sách các tài liệu trong RecyclerView
public class DocumentAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final Context context; // Context để bắt đầu các hoạt động
    private List<DocumentModel> dataList; // Danh sách dữ liệu tài liệu để hiển thị

    // Hàm khởi tạo để khởi tạo adapter với context và danh sách dữ liệu
    public DocumentAdapter(Context context, List<DocumentModel> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    // Phương thức tạo và gắn layout cho mỗi item trong RecyclerView
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new MyViewHolder(view);
    }

    // Gắn dữ liệu vào các view của ViewHolder
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Đặt tiêu đề, môn học và năm cho tài liệu hiện tại
        holder.recTitle.setText(dataList.get(position).getTitle());
        holder.recSubject.setText(dataList.get(position).getSubjectName());
        int year = Calendar.getInstance().get(Calendar.YEAR);
        holder.recYear.setText(year);

        // Đặt OnClickListener cho CardView để mở trình xem PDF khi nhấn vào
        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewPDFActivity.class);
                // Truyền tiêu đề và liên kết đến tài liệu PDF cho hoạt động xem PDF
                intent.putExtra("title", dataList.get(holder.getAdapterPosition()).getTitle());
                intent.putExtra("pdf", dataList.get(holder.getAdapterPosition()).getLinkDocument());
                context.startActivity(intent);
            }
        });
    }

    // Trả về tổng số item trong danh sách dữ liệu
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // Cập nhật danh sách dữ liệu với danh sách mới và thông báo cho adapter để làm mới
    @SuppressLint("NotifyDataSetChanged")
    public void searchDataList(ArrayList<DocumentModel> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
}

// Lớp ViewHolder để giữ và tái sử dụng các view cho mỗi item tài liệu
class MyViewHolder extends RecyclerView.ViewHolder{

    TextView recTitle, recSubject, recYear; // TextView để hiển thị chi tiết tài liệu
    CardView recCard; // CardView cho mỗi item tài liệu

    // Hàm khởi tạo để khởi tạo các view
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        recCard = itemView.findViewById(R.id.recCard);
        recTitle = itemView.findViewById(R.id.recTitle);
        recSubject = itemView.findViewById(R.id.recSubject);
        recYear = itemView.findViewById(R.id.recYear);
    }
}
