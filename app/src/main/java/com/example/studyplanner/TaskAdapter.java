package com.example.studyplanner;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onToggleTaskComplete(Task task, boolean isCompleted);
        void onEditTask(Task task);
        void onDeleteTask(Task task);
    }

    private final Context context;
    private List<Task> taskList;
    private final OnTaskActionListener listener;

    public TaskAdapter(Context context, List<Task> taskList, OnTaskActionListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateList(List<Task> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTitle.setText(task.getTitle());

        // Description
        String desc = task.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            holder.tvDesc.setText(desc);
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Subject
        String subject = task.getSubject();
        if (subject != null && !subject.trim().isEmpty()) {
            holder.tvSubject.setText(subject);
            holder.tvSubject.setVisibility(View.VISIBLE);
        } else {
            holder.tvSubject.setVisibility(View.GONE);
        }

        // Due date
        String dueDate = task.getDueDate();
        if (dueDate != null && !dueDate.trim().isEmpty()) {
            holder.tvDueDate.setText(dueDate);
        } else {
            holder.tvDueDate.setText("No due date");
        }

        // Priority badge styling
        String priority = task.getPriority();
        if (priority == null || priority.trim().isEmpty()) {
            priority = "Medium";
        }
        holder.tvPriority.setText(priority);

        if ("High".equalsIgnoreCase(priority)) {
            holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_high));
            holder.tvPriority.setBackgroundResource(R.drawable.badge_priority_high);
        } else if ("Low".equalsIgnoreCase(priority)) {
            holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_low));
            holder.tvPriority.setBackgroundResource(R.drawable.badge_priority_low);
        } else {
            holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_medium));
            holder.tvPriority.setBackgroundResource(R.drawable.badge_priority_medium);
        }

        // Completion state & strike-through
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted());
        applyCompletedStyle(holder, task.isCompleted());

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            applyCompletedStyle(holder, isChecked);
            if (listener != null) {
                listener.onToggleTaskComplete(task, isChecked);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditTask(task);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteTask(task);
            }
        });
    }

    private void applyCompletedStyle(TaskViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        TextView tvTitle, tvDesc, tvSubject, tvDueDate, tvPriority;
        ImageButton btnEdit, btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cb_task_completed);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDesc = itemView.findViewById(R.id.tv_task_description);
            tvSubject = itemView.findViewById(R.id.tv_task_subject);
            tvDueDate = itemView.findViewById(R.id.tv_task_due_date);
            tvPriority = itemView.findViewById(R.id.tv_task_priority);
            btnEdit = itemView.findViewById(R.id.btn_edit_task);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);
        }
    }
}

