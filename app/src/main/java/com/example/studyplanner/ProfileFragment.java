package com.example.studyplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String PREF_NAME = "StudyPlannerProfile";
    private static final String KEY_NAME = "student_name";
    private static final String KEY_REG_NO = "register_number";
    private static final String KEY_DEPT = "department";
    private static final String KEY_COLLEGE = "college";
    private static final String KEY_EMAIL = "email";

    private TextView tvHeaderName;
    private TextView tvHeaderDept;
    private TextInputEditText etName;
    private TextInputEditText etRegNo;
    private TextInputEditText etDept;
    private TextInputEditText etCollege;
    private TextInputEditText etEmail;
    private MaterialButton btnSave;

    private SharedPreferences preferences;
    private FirebaseHelper firebaseHelper;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        preferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        firebaseHelper = FirebaseHelper.getInstance();

        tvHeaderName = view.findViewById(R.id.tv_profile_header_name);
        tvHeaderDept = view.findViewById(R.id.tv_profile_header_dept);
        etName = view.findViewById(R.id.et_profile_name);
        etRegNo = view.findViewById(R.id.et_profile_reg_no);
        etDept = view.findViewById(R.id.et_profile_dept);
        etCollege = view.findViewById(R.id.et_profile_college);
        etEmail = view.findViewById(R.id.et_profile_email);
        btnSave = view.findViewById(R.id.btn_save_profile);

        loadProfileData();

        btnSave.setOnClickListener(v -> saveProfileData());

        return view;
    }

    private void loadProfileData() {
        // Load local cache first for instant response
        String name = preferences.getString(KEY_NAME, "");
        String regNo = preferences.getString(KEY_REG_NO, "");
        String dept = preferences.getString(KEY_DEPT, "");
        String college = preferences.getString(KEY_COLLEGE, "");
        String email = preferences.getString(KEY_EMAIL, "");

        etName.setText(name);
        etRegNo.setText(regNo);
        etDept.setText(dept);
        etCollege.setText(college);
        etEmail.setText(email);

        updateHeader(name, dept);

        // Fetch latest from Firebase Firestore
        firebaseHelper.getFirestore().collection("profile").document("current_user")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String fName = doc.getString("name");
                        String fReg = doc.getString("reg_no");
                        String fDept = doc.getString("dept");
                        String fCollege = doc.getString("college");
                        String fEmail = doc.getString("email");

                        if (fName != null) etName.setText(fName);
                        if (fReg != null) etRegNo.setText(fReg);
                        if (fDept != null) etDept.setText(fDept);
                        if (fCollege != null) etCollege.setText(fCollege);
                        if (fEmail != null) etEmail.setText(fEmail);

                        updateHeader(fName, fDept);

                        // Update local cache
                        SharedPreferences.Editor editor = preferences.edit();
                        if (fName != null) editor.putString(KEY_NAME, fName);
                        if (fReg != null) editor.putString(KEY_REG_NO, fReg);
                        if (fDept != null) editor.putString(KEY_DEPT, fDept);
                        if (fCollege != null) editor.putString(KEY_COLLEGE, fCollege);
                        if (fEmail != null) editor.putString(KEY_EMAIL, fEmail);
                        editor.apply();
                    }
                });
    }

    private void updateHeader(String name, String dept) {
        if (name != null && !name.trim().isEmpty()) {
            tvHeaderName.setText(name);
        } else {
            tvHeaderName.setText("Student Profile");
        }

        if (dept != null && !dept.trim().isEmpty()) {
            tvHeaderDept.setText(dept);
        } else {
            tvHeaderDept.setText("Keep your academic details updated");
        }
    }

    private void saveProfileData() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String regNo = etRegNo.getText() != null ? etRegNo.getText().toString().trim() : "";
        String dept = etDept.getText() != null ? etDept.getText().toString().trim() : "";
        String college = etCollege.getText() != null ? etCollege.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        // 1. Save to SharedPreferences for quick offline access
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_REG_NO, regNo);
        editor.putString(KEY_DEPT, dept);
        editor.putString(KEY_COLLEGE, college);
        editor.putString(KEY_EMAIL, email);
        editor.apply();

        updateHeader(name, dept);

        // 2. Save to Firebase Firestore
        Map<String, Object> profileMap = new HashMap<>();
        profileMap.put("name", name);
        profileMap.put("reg_no", regNo);
        profileMap.put("dept", dept);
        profileMap.put("college", college);
        profileMap.put("email", email);

        firebaseHelper.getFirestore().collection("profile").document("current_user")
                .set(profileMap)
                .addOnSuccessListener(unused -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Profile saved to Firebase!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Saved locally (Firebase sync error: " + e.getMessage() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
