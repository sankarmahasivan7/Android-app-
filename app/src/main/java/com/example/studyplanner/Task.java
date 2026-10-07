package com.example.studyplanner;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class Task implements Serializable {
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String subject;
    private String dueDate;
    private String priority;
    private boolean completed;

    public Task() {
        // Required for Firebase Firestore deserialization
    }

    public Task(String title, String description, String subject, String dueDate, String priority, boolean completed) {
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
    }

    public Task(String id, String title, String description, String subject, String dueDate, String priority, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
