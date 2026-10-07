package com.example.studyplanner;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment implements TaskAdapter.OnTaskActionListener {

    private RecyclerView rvTasks;
    private View layoutEmpty;
    private FloatingActionButton fabAddTask;
    private ChipGroup chipGroupFilters;

    private FirebaseHelper firebaseHelper;
    private TaskAdapter adapter;
    private final List<Task> allTasks = new ArrayList<>();
    private final List<Task> filteredTasks = new ArrayList<>();
    private final List<String> availableSubjects = new ArrayList<>();

    private String currentFilter = "All";
    private ListenerRegistration tasksListener;
    private ListenerRegistration subjectsListener;

    public TasksFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        firebaseHelper = FirebaseHelper.getInstance();

        rvTasks = view.findViewById(R.id.rv_tasks);
        layoutEmpty = view.findViewById(R.id.layout_empty_tasks);
        fabAddTask = view.findViewById(R.id.fab_add_task);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);

        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(requireContext(), filteredTasks, this);
        rvTasks.setAdapter(adapter);

        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_filter_pending) {
                currentFilter = "Pending";
            } else if (checkedId == R.id.chip_filter_completed) {
                currentFilter = "Completed";
            } else if (checkedId == R.id.chip_filter_high) {
                currentFilter = "High Priority";
            } else {
                currentFilter = "All";
            }
            applyFilter();
        });

        fabAddTask.setOnClickListener(v -> showAddEditTaskDialog(null));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startListeningToData();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (tasksListener != null) {
            tasksListener.remove();
            tasksListener = null;
        }
        if (subjectsListener != null) {
            subjectsListener.remove();
            subjectsListener = null;
        }
    }

    private void startListeningToData() {
        // Listen to available subjects for task assignment
        subjectsListener = firebaseHelper.listenToSubjects((snapshots, error) -> {
            availableSubjects.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Subject subject = doc.toObject(Subject.class);
                    if (subject != null && subject.getName() != null) {
                        availableSubjects.add(subject.getName());
                    }
                }
            }
            if (availableSubjects.isEmpty()) {
                availableSubjects.add("General Study");
            }
        });

        // Listen to all tasks
        tasksListener = firebaseHelper.listenToTasks((snapshots, error) -> {
            if (error != null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            allTasks.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Task task = doc.toObject(Task.class);
                    if (task != null) {
                        task.setId(doc.getId());
                        allTasks.add(task);
                    }
                }
            }
            applyFilter();
        });
    }

    private void applyFilter() {
        filteredTasks.clear();
        for (Task task : allTasks) {
            if ("Pending".equalsIgnoreCase(currentFilter)) {
                if (!task.isCompleted()) filteredTasks.add(task);
            } else if ("Completed".equalsIgnoreCase(currentFilter)) {
                if (task.isCompleted()) filteredTasks.add(task);
            } else if ("High Priority".equalsIgnoreCase(currentFilter)) {
                if ("High".equalsIgnoreCase(task.getPriority())) filteredTasks.add(task);
            } else {
                filteredTasks.add(task);
            }
        }

        if (filteredTasks.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddEditTaskDialog(@Nullable Task taskToEdit) {
        boolean isEdit = (taskToEdit != null);

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null);
        TextInputEditText etTitle = dialogView.findViewById(R.id.et_dialog_task_title);
        TextInputEditText etDesc = dialogView.findViewById(R.id.et_dialog_task_description);
        Spinner spSubject = dialogView.findViewById(R.id.spinner_dialog_task_subject);
        TextInputEditText etDueDate = dialogView.findViewById(R.id.et_dialog_task_due_date);
        Spinner spPriority = dialogView.findViewById(R.id.spinner_dialog_task_priority);

        // Subject Spinner
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, availableSubjects);
        spSubject.setAdapter(subjectAdapter);

        // Priority Spinner
        String[] priorities = new String[]{"Low", "Medium", "High"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, priorities);
        spPriority.setAdapter(priorityAdapter);
        spPriority.setSelection(1); // default Medium

        final Calendar calendar = Calendar.getInstance();
        etDueDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, y, m, d) -> {
                calendar.set(Calendar.YEAR, y);
                calendar.set(Calendar.MONTH, m);
                calendar.set(Calendar.DAY_OF_MONTH, d);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                etDueDate.setText(sdf.format(calendar.getTime()));
            }, year, month, day);
            datePicker.show();
        });

        if (isEdit) {
            etTitle.setText(taskToEdit.getTitle());
            etDesc.setText(taskToEdit.getDescription());
            etDueDate.setText(taskToEdit.getDueDate());

            int subIndex = availableSubjects.indexOf(taskToEdit.getSubject());
            if (subIndex >= 0) {
                spSubject.setSelection(subIndex);
            }

            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i].equalsIgnoreCase(taskToEdit.getPriority())) {
                    spPriority.setSelection(i);
                    break;
                }
            }
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etDueDate.setText(sdf.format(calendar.getTime()));
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? R.string.edit_task : R.string.add_task)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
                String dueDate = etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "";
                String subject = spSubject.getSelectedItem() != null ? spSubject.getSelectedItem().toString() : "General";
                String priority = spPriority.getSelectedItem() != null ? spPriority.getSelectedItem().toString() : "Medium";

                if (TextUtils.isEmpty(title)) {
                    etTitle.setError(getString(R.string.field_required));
                    etTitle.requestFocus();
                    return;
                }

                if (isEdit) {
                    taskToEdit.setTitle(title);
                    taskToEdit.setDescription(desc);
                    taskToEdit.setSubject(subject);
                    taskToEdit.setDueDate(dueDate);
                    taskToEdit.setPriority(priority);
                    firebaseHelper.updateTask(taskToEdit,
                            unused -> Toast.makeText(requireContext(), "Task updated in Firebase", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(requireContext(), "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Task newTask = new Task(title, desc, subject, dueDate, priority, false);
                    firebaseHelper.addTask(newTask,
                            docRef -> Toast.makeText(requireContext(), "Task added to Firebase", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(requireContext(), "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    public void onToggleTaskComplete(Task task, boolean isCompleted) {
        firebaseHelper.setTaskCompleted(task.getId(), isCompleted,
                unused -> {},
                e -> Toast.makeText(getContext(), "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onEditTask(Task task) {
        showAddEditTaskDialog(task);
    }

    @Override
    public void onDeleteTask(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_task_msg)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    firebaseHelper.deleteTask(task.getId(),
                            unused -> Toast.makeText(requireContext(), "Task deleted from Firebase", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(requireContext(), "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
