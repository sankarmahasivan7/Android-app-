package com.example.studyplanner;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class SubjectsFragment extends Fragment implements SubjectAdapter.OnSubjectActionListener {

    private RecyclerView rvSubjects;
    private View layoutEmpty;
    private FloatingActionButton fabAddSubject;

    private FirebaseHelper firebaseHelper;
    private SubjectAdapter adapter;
    private final List<Subject> subjects = new ArrayList<>();
    private ListenerRegistration subjectsListener;

    public SubjectsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subjects, container, false);

        firebaseHelper = FirebaseHelper.getInstance();

        rvSubjects = view.findViewById(R.id.rv_subjects);
        layoutEmpty = view.findViewById(R.id.layout_empty_subjects);
        fabAddSubject = view.findViewById(R.id.fab_add_subject);

        rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SubjectAdapter(requireContext(), subjects, this);
        rvSubjects.setAdapter(adapter);

        fabAddSubject.setOnClickListener(v -> showAddEditSubjectDialog(null));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startListeningToSubjects();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (subjectsListener != null) {
            subjectsListener.remove();
            subjectsListener = null;
        }
    }

    private void startListeningToSubjects() {
        if (subjectsListener != null) {
            subjectsListener.remove();
        }

        subjectsListener = firebaseHelper.listenToSubjects((snapshots, error) -> {
            if (error != null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            subjects.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Subject subject = doc.toObject(Subject.class);
                    if (subject != null) {
                        subject.setId(doc.getId());
                        subjects.add(subject);
                    }
                }
            }

            if (subjects.isEmpty()) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvSubjects.setVisibility(View.GONE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rvSubjects.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void showAddEditSubjectDialog(@Nullable Subject subjectToEdit) {
        boolean isEdit = (subjectToEdit != null);

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_subject, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_dialog_subject_name);
        TextInputEditText etCode = dialogView.findViewById(R.id.et_dialog_subject_code);
        TextInputEditText etFaculty = dialogView.findViewById(R.id.et_dialog_faculty_name);
        TextInputEditText etTargetHours = dialogView.findViewById(R.id.et_dialog_target_hours);

        if (isEdit) {
            etName.setText(subjectToEdit.getName());
            etCode.setText(subjectToEdit.getCode());
            etFaculty.setText(subjectToEdit.getFaculty());
            etTargetHours.setText(String.valueOf((int) subjectToEdit.getTargetHours()));
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? R.string.edit_subject : R.string.add_subject)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
                String faculty = etFaculty.getText() != null ? etFaculty.getText().toString().trim() : "";
                String hoursStr = etTargetHours.getText() != null ? etTargetHours.getText().toString().trim() : "";

                if (TextUtils.isEmpty(name)) {
                    etName.setError(getString(R.string.field_required));
                    etName.requestFocus();
                    return;
                }

                double hours = 0.0;
                if (!TextUtils.isEmpty(hoursStr)) {
                    try {
                        hours = Double.parseDouble(hoursStr);
                    } catch (NumberFormatException e) {
                        hours = 0.0;
                    }
                }

                if (isEdit) {
                    subjectToEdit.setName(name);
                    subjectToEdit.setCode(code);
                    subjectToEdit.setFaculty(faculty);
                    subjectToEdit.setTargetHours(hours);
                    firebaseHelper.updateSubject(subjectToEdit,
                            unused -> Toast.makeText(requireContext(), "Subject updated in Firebase", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(requireContext(), "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Subject newSubject = new Subject(name, code, faculty, hours);
                    firebaseHelper.addSubject(newSubject,
                            docRef -> Toast.makeText(requireContext(), "Subject added to Firebase", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(requireContext(), "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    public void onEditSubject(Subject subject) {
        showAddEditSubjectDialog(subject);
    }

    @Override
    public void onDeleteSubject(Subject subject) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_subject_msg)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    firebaseHelper.deleteSubject(subject.getId(),
                            unused -> Toast.makeText(requireContext(), "Subject deleted from Firebase", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(requireContext(), "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
