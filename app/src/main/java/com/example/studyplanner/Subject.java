package com.example.studyplanner;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class Subject implements Serializable {
    @DocumentId
    private String id;
    private String name;
    private String code;
    private String faculty;
    private double targetHours;

    public Subject() {
        // Required for Firebase Firestore deserialization
    }

    public Subject(String name, String code, String faculty, double targetHours) {
        this.name = name;
        this.code = code;
        this.faculty = faculty;
        this.targetHours = targetHours;
    }

    public Subject(String id, String name, String code, String faculty, double targetHours) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.faculty = faculty;
        this.targetHours = targetHours;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public double getTargetHours() {
        return targetHours;
    }

    public void setTargetHours(double targetHours) {
        this.targetHours = targetHours;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
