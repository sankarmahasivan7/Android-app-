package com.example.studyplanner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    public static final String COLLECTION_SUBJECTS = "subjects";
    public static final String COLLECTION_TASKS = "tasks";

    private static FirebaseHelper instance;
    private final FirebaseFirestore firestore;

    private FirebaseHelper() {
        firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public CollectionReference getSubjectsCollection() {
        return firestore.collection(COLLECTION_SUBJECTS);
    }

    public CollectionReference getTasksCollection() {
        return firestore.collection(COLLECTION_TASKS);
    }

    // ==========================================
    // SUBJECTS FIRESTORE CRUD
    // ==========================================

    public void addSubject(Subject subject, OnSuccessListener<DocumentReference> successListener, OnFailureListener failureListener) {
        getSubjectsCollection()
                .add(subject)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void updateSubject(Subject subject, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        if (subject.getId() == null) return;
        getSubjectsCollection()
                .document(subject.getId())
                .set(subject)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void deleteSubject(String subjectId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        if (subjectId == null) return;
        getSubjectsCollection()
                .document(subjectId)
                .delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public ListenerRegistration listenToSubjects(EventListener<QuerySnapshot> listener) {
        return getSubjectsCollection()
                .orderBy("name")
                .addSnapshotListener(listener);
    }

    // ==========================================
    // TASKS FIRESTORE CRUD
    // ==========================================

    public void addTask(Task task, OnSuccessListener<DocumentReference> successListener, OnFailureListener failureListener) {
        getTasksCollection()
                .add(task)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void updateTask(Task task, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        if (task.getId() == null) return;
        getTasksCollection()
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void deleteTask(String taskId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        if (taskId == null) return;
        getTasksCollection()
                .document(taskId)
                .delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void setTaskCompleted(String taskId, boolean completed, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        if (taskId == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("completed", completed);
        getTasksCollection()
                .document(taskId)
                .update(updates)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public ListenerRegistration listenToTasks(EventListener<QuerySnapshot> listener) {
        return getTasksCollection()
                .orderBy("completed")
                .orderBy("dueDate")
                .addSnapshotListener(listener);
    }
}

