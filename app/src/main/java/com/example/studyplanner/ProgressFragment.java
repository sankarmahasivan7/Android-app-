package com.example.studyplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressFragment extends Fragment {

    private TextView tvOverallPercent;
    private ProgressBar pbOverall;
    private TextView tvTotalTasks;
    private TextView tvCompletedTasks;
    private TextView tvPendingTasks;
    private TextView tvEmptySubjects;
    private RecyclerView rvSubjectProgress;

    private FirebaseHelper firebaseHelper;
    private SubjectProgressAdapter adapter;
    private final List<Subject> subjectsList = new ArrayList<>();
    private final List<Task> tasksList = new ArrayList<>();
    private final List<SubjectProgressAdapter.SubjectProgressItem> progressItems = new ArrayList<>();

    private ListenerRegistration subjectsListener;
    private ListenerRegistration tasksListener;

    public ProgressFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        firebaseHelper = FirebaseHelper.getInstance();

        tvOverallPercent = view.findViewById(R.id.tv_progress_overall_percent);
        pbOverall = view.findViewById(R.id.pb_progress_overall);
        tvTotalTasks = view.findViewById(R.id.tv_progress_total_tasks);
        tvCompletedTasks = view.findViewById(R.id.tv_progress_completed_tasks);
        tvPendingTasks = view.findViewById(R.id.tv_progress_pending_tasks);
        tvEmptySubjects = view.findViewById(R.id.tv_progress_empty_subjects);
        rvSubjectProgress = view.findViewById(R.id.rv_subject_progress);

        rvSubjectProgress.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSubjectProgress.setNestedScrollingEnabled(false);
        adapter = new SubjectProgressAdapter(requireContext(), progressItems);
        rvSubjectProgress.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
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

    private void startListeningToFirebase() {
        subjectsListener = firebaseHelper.listenToSubjects((snapshots, error) -> {
            if (error != null) return;
            subjectsList.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Subject subject = doc.toObject(Subject.class);
                    if (subject != null) {
                        subject.setId(doc.getId());
                        subjectsList.add(subject);
                    }
                }
            }
            recomputeAnalytics();
        });

        tasksListener = firebaseHelper.listenToTasks((snapshots, error) -> {
            if (error != null) return;
            tasksList.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Task task = doc.toObject(Task.class);
                    if (task != null) {
                        task.setId(doc.getId());
                        tasksList.add(task);
                    }
                }
            }
            recomputeAnalytics();
        });
    }

    private void recomputeAnalytics() {
        if (!isAdded()) return;

        // 1. Overall task stats
        int totalTasks = tasksList.size();
        int completedTasks = 0;
        int pendingTasks = 0;

        Map<String, Integer> subjectTotalCount = new HashMap<>();
        Map<String, Integer> subjectCompletedCount = new HashMap<>();

        for (Task task : tasksList) {
            if (task.isCompleted()) {
                completedTasks++;
            } else {
                pendingTasks++;
            }

            String subName = task.getSubject();
            if (subName != null) {
                subjectTotalCount.put(subName, subjectTotalCount.getOrDefault(subName, 0) + 1);
                if (task.isCompleted()) {
                    subjectCompletedCount.put(subName, subjectCompletedCount.getOrDefault(subName, 0) + 1);
                }
            }
        }

        int overallPercent = 0;
        if (totalTasks > 0) {
            overallPercent = (int) Math.round(((double) completedTasks / totalTasks) * 100);
        }

        tvOverallPercent.setText(overallPercent + "%");
        pbOverall.setProgress(overallPercent);
        tvTotalTasks.setText(String.valueOf(totalTasks));
        tvCompletedTasks.setText(String.valueOf(completedTasks));
        tvPendingTasks.setText(String.valueOf(pendingTasks));

        // 2. Subject-wise analytics
        progressItems.clear();
        if (subjectsList.isEmpty()) {
            tvEmptySubjects.setVisibility(View.VISIBLE);
            rvSubjectProgress.setVisibility(View.GONE);
        } else {
            tvEmptySubjects.setVisibility(View.GONE);
            rvSubjectProgress.setVisibility(View.VISIBLE);

            for (Subject subject : subjectsList) {
                int totalForSub = subjectTotalCount.getOrDefault(subject.getName(), 0);
                int compForSub = subjectCompletedCount.getOrDefault(subject.getName(), 0);
                progressItems.add(new SubjectProgressAdapter.SubjectProgressItem(subject, totalForSub, compForSub));
            }
        }
        adapter.notifyDataSetChanged();
    }
}
