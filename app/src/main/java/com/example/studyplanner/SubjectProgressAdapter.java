package com.example.studyplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubjectProgressAdapter extends RecyclerView.Adapter<SubjectProgressAdapter.ViewHolder> {

    public static class SubjectProgressItem {
        public Subject subject;
        public int totalTasks;
        public int completedTasks;

        public SubjectProgressItem(Subject subject, int totalTasks, int completedTasks) {
            this.subject = subject;
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
        }

        public int getPercentage() {
            if (totalTasks == 0) return 0;
            return (int) Math.round(((double) completedTasks / totalTasks) * 100);
        }
    }

    private final Context context;
    private List<SubjectProgressItem> items;

    public SubjectProgressAdapter(Context context, List<SubjectProgressItem> items) {
        this.context = context;
        this.items = items;
    }

    public void updateList(List<SubjectProgressItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubjectProgressItem item = items.get(position);

        holder.tvName.setText(item.subject.getName());
        String code = item.subject.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "SUB";
        }
        holder.tvCode.setText(code);

        int percent = item.getPercentage();
        holder.tvCount.setText(item.completedTasks + " of " + item.totalTasks + " tasks done");
        holder.tvPercent.setText(percent + "%");
        holder.progressBar.setProgress(percent);

        holder.tvTargetHours.setText("Target: " + (int) item.subject.getTargetHours() + " hrs");
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvCount, tvPercent, tvTargetHours;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_progress_subject_name);
            tvCode = itemView.findViewById(R.id.tv_progress_subject_code);
            tvCount = itemView.findViewById(R.id.tv_progress_task_count);
            tvPercent = itemView.findViewById(R.id.tv_progress_percent);
            progressBar = itemView.findViewById(R.id.pb_subject_progress);
            tvTargetHours = itemView.findViewById(R.id.tv_progress_target_hours);
        }
    }
}

