package com.example.studyplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    public interface OnSubjectActionListener {
        void onEditSubject(Subject subject);
        void onDeleteSubject(Subject subject);
    }

    private final Context context;
    private List<Subject> subjectList;
    private final OnSubjectActionListener listener;

    public SubjectAdapter(Context context, List<Subject> subjectList, OnSubjectActionListener listener) {
        this.context = context;
        this.subjectList = subjectList;
        this.listener = listener;
    }

    public void updateList(List<Subject> newList) {
        this.subjectList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);

        String code = subject.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "SUB";
        }
        holder.tvCode.setText(code);
        holder.tvName.setText(subject.getName());

        String faculty = subject.getFaculty();
        if (faculty == null || faculty.trim().isEmpty()) {
            holder.tvFaculty.setText("Faculty: Not specified");
        } else {
            holder.tvFaculty.setText("Faculty: " + faculty);
        }

        holder.tvTargetHours.setText("Target: " + (int) subject.getTargetHours() + " hrs");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditSubject(subject);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteSubject(subject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectList != null ? subjectList.size() : 0;
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvName, tvFaculty, tvTargetHours;
        ImageButton btnEdit, btnDelete;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_subject_code);
            tvName = itemView.findViewById(R.id.tv_subject_name);
            tvFaculty = itemView.findViewById(R.id.tv_faculty_name);
            tvTargetHours = itemView.findViewById(R.id.tv_target_hours);
            btnEdit = itemView.findViewById(R.id.btn_edit_subject);
            btnDelete = itemView.findViewById(R.id.btn_delete_subject);
        }
    }
}

