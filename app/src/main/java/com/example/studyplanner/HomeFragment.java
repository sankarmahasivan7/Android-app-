package com.example.studyplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements TaskAdapter.OnTaskActionListener {

    private TextView tvGreeting;
    private TextView tvStudentName;
    private TextView tvTodayDate;
    private TextView tvProgressPercent;
    private TextView tvProgressSummary;
    private ProgressBar pbOverallProgress;
    private TextView tvStatTotalSubjects;
    private TextView tvStatPendingTasks;
    private TextView tvStatCompletedTasks;
    private TextView tvNoTasksToday;
    private RecyclerView rvTodayTasks;

    private FirebaseHelper firebaseHelper;
    private TaskAdapter taskAdapter;
    private final List<Task> todayTasks = new ArrayList<>();

    private ListenerRegistration subjectsListener;
    private ListenerRegistration tasksListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseHelper = FirebaseHelper.getInstance();

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvStudentName = view.findViewById(R.id.tv_student_name);
        tvTodayDate = view.findViewById(R.id.tv_today_date);
        tvProgressPercent = view.findViewById(R.id.tv_home_progress_percent);
        tvProgressSummary = view.findViewById(R.id.tv_home_progress_summary);
        pbOverallProgress = view.findViewById(R.id.pb_home_overall_progress);
        tvStatTotalSubjects = view.findViewById(R.id.tv_stat_total_subjects);
        tvStatPendingTasks = view.findViewById(R.id.tv_stat_pending_tasks);
        tvStatCompletedTasks = view.findViewById(R.id.tv_stat_completed_tasks);
        tvNoTasksToday = view.findViewById(R.id.tv_home_no_tasks);
        rvTodayTasks = view.findViewById(R.id.rv_today_tasks);

        rvTodayTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTodayTasks.setNestedScrollingEnabled(false);
        taskAdapter = new TaskAdapter(requireContext(), todayTasks, this);
        rvTodayTasks.setAdapter(taskAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadHeaderData();
        startListeningToFirebase();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (subjectsListener != null) {
            subjectsListener.remove();
            subjectsListener = null;
        }
        if (tasksListener != null) {
            tasksListener.remove();
            tasksListener = null;
        }
    }

    private void loadHeaderData() {
        if (!isAdded()) return;

        // 1. Time-based Greeting
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            tvGreeting.setText("Good Morning,");
        } else if (hour < 17) {
            tvGreeting.setText("Good Afternoon,");
        } else {
            tvGreeting.setText("Good Evening,");
        }

        // 2. Student Name from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("StudyPlannerProfile", Context.MODE_PRIVATE);
        String name = prefs.getString("student_name", "Student");
        if (name == null || name.trim().isEmpty()) {
            name = "Student";
        }
        tvStudentName.setText(name);

        // 3. Today's Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
        String displayDate = dateFormat.format(new Date());
        tvTodayDate.setText(displayDate);
    }

    private void startListeningToFirebase() {
        // Listen to Subjects count
        subjectsListener = firebaseHelper.listenToSubjects((snapshots, error) -> {
            if (error != null) return;
            int count = (snapshots != null) ? snapshots.size() : 0;
            tvStatTotalSubjects.setText(String.valueOf(count));
        });

        // Listen to Tasks
        SimpleDateFormat queryFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDateString = queryFormat.format(new Date());

        tasksListener = firebaseHelper.listenToTasks((snapshots, error) -> {
            if (error != null) return;

            int totalTasks = 0;
            int completedTasks = 0;
            int pendingTasks = 0;

            todayTasks.clear();

            if (snapshots != null) {
                totalTasks = snapshots.size();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Task task = doc.toObject(Task.class);
                    if (task != null) {
                        task.setId(doc.getId());
                        if (task.isCompleted()) {
                            completedTasks++;
                        } else {
                            pendingTasks++;
                        }

                        if (todayDateString.equals(task.getDueDate())) {
                            todayTasks.add(task);
                        }
                    }
                }
            }

            tvStatPendingTasks.setText(String.valueOf(pendingTasks));
            tvStatCompletedTasks.setText(String.valueOf(completedTasks));

            int progressPercent = 0;
            if (totalTasks > 0) {
                progressPercent = (int) Math.round(((double) completedTasks / totalTasks) * 100);
            }
            pbOverallProgress.setProgress(progressPercent);
            tvProgressPercent.setText(progressPercent + "%");
            tvProgressSummary.setText(completedTasks + " of " + totalTasks + " tasks completed");

            if (todayTasks.isEmpty()) {
                tvNoTasksToday.setVisibility(View.VISIBLE);
                rvTodayTasks.setVisibility(View.GONE);
            } else {
                tvNoTasksToday.setVisibility(View.GONE);
                rvTodayTasks.setVisibility(View.VISIBLE);
            }
            taskAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onToggleTaskComplete(Task task, boolean isCompleted) {
        firebaseHelper.setTaskCompleted(task.getId(), isCompleted, null, null);
    }

    @Override
    public void onEditTask(Task task) {
        Toast.makeText(requireContext(), "You can edit this task in the Tasks section", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteTask(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_task_msg)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    firebaseHelper.deleteTask(task.getId(),
                            unused -> Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(requireContext(), "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
