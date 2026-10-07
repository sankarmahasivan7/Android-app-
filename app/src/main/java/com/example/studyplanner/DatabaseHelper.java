package com.example.studyplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StudyPlanner.db";
    private static final int DATABASE_VERSION = 1;

    // Subjects Table
    public static final String TABLE_SUBJECTS = "subjects";
    public static final String COL_SUBJECT_ID = "id";
    public static final String COL_SUBJECT_NAME = "name";
    public static final String COL_SUBJECT_CODE = "code";
    public static final String COL_SUBJECT_FACULTY = "faculty";
    public static final String COL_SUBJECT_TARGET_HOURS = "target_hours";

    // Tasks Table
    public static final String TABLE_TASKS = "tasks";
    public static final String COL_TASK_ID = "id";
    public static final String COL_TASK_TITLE = "title";
    public static final String COL_TASK_DESCRIPTION = "description";
    public static final String COL_TASK_SUBJECT = "subject";
    public static final String COL_TASK_DUE_DATE = "due_date";
    public static final String COL_TASK_PRIORITY = "priority";
    public static final String COL_TASK_COMPLETED = "completed";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSubjectsTable = "CREATE TABLE " + TABLE_SUBJECTS + " ("
                + COL_SUBJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SUBJECT_NAME + " TEXT NOT NULL, "
                + COL_SUBJECT_CODE + " TEXT, "
                + COL_SUBJECT_FACULTY + " TEXT, "
                + COL_SUBJECT_TARGET_HOURS + " REAL"
                + ");";

        String createTasksTable = "CREATE TABLE " + TABLE_TASKS + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_TITLE + " TEXT NOT NULL, "
                + COL_TASK_DESCRIPTION + " TEXT, "
                + COL_TASK_SUBJECT + " TEXT, "
                + COL_TASK_DUE_DATE + " TEXT, "
                + COL_TASK_PRIORITY + " TEXT, "
                + COL_TASK_COMPLETED + " INTEGER DEFAULT 0"
                + ");";

        db.execSQL(createSubjectsTable);
        db.execSQL(createTasksTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS);
        onCreate(db);
    }

    // ==========================================
    // SUBJECTS CRUD
    // ==========================================

    public long insertSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SUBJECT_NAME, subject.getName());
        cv.put(COL_SUBJECT_CODE, subject.getCode());
        cv.put(COL_SUBJECT_FACULTY, subject.getFaculty());
        cv.put(COL_SUBJECT_TARGET_HOURS, subject.getTargetHours());
        return db.insert(TABLE_SUBJECTS, null, cv);
    }

    public List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUBJECTS, null, null, null, null, null, COL_SUBJECT_NAME + " ASC");

        if (cursor != null) {
            try {
                int idIdx = cursor.getColumnIndexOrThrow(COL_SUBJECT_ID);
                int nameIdx = cursor.getColumnIndexOrThrow(COL_SUBJECT_NAME);
                int codeIdx = cursor.getColumnIndexOrThrow(COL_SUBJECT_CODE);
                int facultyIdx = cursor.getColumnIndexOrThrow(COL_SUBJECT_FACULTY);
                int hoursIdx = cursor.getColumnIndexOrThrow(COL_SUBJECT_TARGET_HOURS);

                while (cursor.moveToNext()) {
                    Subject subject = new Subject(
                            String.valueOf(cursor.getLong(idIdx)),
                            cursor.getString(nameIdx),
                            cursor.getString(codeIdx),
                            cursor.getString(facultyIdx),
                            cursor.getDouble(hoursIdx)
                    );
                    subjects.add(subject);
                }
            } finally {
                cursor.close();
            }
        }
        return subjects;
    }

    public Subject getSubjectById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Subject subject = null;
        Cursor cursor = db.query(TABLE_SUBJECTS, null, COL_SUBJECT_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    subject = new Subject(
                            String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(COL_SUBJECT_ID))),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBJECT_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBJECT_CODE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBJECT_FACULTY)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SUBJECT_TARGET_HOURS))
                    );
                }
            } finally {
                cursor.close();
            }
        }
        return subject;
    }

    public Subject getSubjectById(long id) {
        return getSubjectById(String.valueOf(id));
    }

    public int updateSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SUBJECT_NAME, subject.getName());
        cv.put(COL_SUBJECT_CODE, subject.getCode());
        cv.put(COL_SUBJECT_FACULTY, subject.getFaculty());
        cv.put(COL_SUBJECT_TARGET_HOURS, subject.getTargetHours());

        return db.update(TABLE_SUBJECTS, cv, COL_SUBJECT_ID + " = ?",
                new String[]{String.valueOf(subject.getId())});
    }

    public int deleteSubject(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SUBJECTS, COL_SUBJECT_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int deleteSubject(long id) {
        return deleteSubject(String.valueOf(id));
    }

    public int getSubjectCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SUBJECTS, null);
        int count = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return count;
    }

    public List<String> getAllSubjectNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUBJECTS, new String[]{COL_SUBJECT_NAME},
                null, null, null, null, COL_SUBJECT_NAME + " ASC");
        if (cursor != null) {
            try {
                int nameIdx = cursor.getColumnIndexOrThrow(COL_SUBJECT_NAME);
                while (cursor.moveToNext()) {
                    names.add(cursor.getString(nameIdx));
                }
            } finally {
                cursor.close();
            }
        }
        return names;
    }

    // ==========================================
    // TASKS CRUD
    // ==========================================

    public long insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, task.getTitle());
        cv.put(COL_TASK_DESCRIPTION, task.getDescription());
        cv.put(COL_TASK_SUBJECT, task.getSubject());
        cv.put(COL_TASK_DUE_DATE, task.getDueDate());
        cv.put(COL_TASK_PRIORITY, task.getPriority());
        cv.put(COL_TASK_COMPLETED, task.isCompleted() ? 1 : 0);
        return db.insert(TABLE_TASKS, null, cv);
    }

    public List<Task> getAllTasks() {
        return getTasksBySelection(null, null);
    }

    public List<Task> getTasksByFilter(String filter) {
        if ("Pending".equalsIgnoreCase(filter)) {
            return getTasksBySelection(COL_TASK_COMPLETED + " = 0", null);
        } else if ("Completed".equalsIgnoreCase(filter)) {
            return getTasksBySelection(COL_TASK_COMPLETED + " = 1", null);
        } else if ("High Priority".equalsIgnoreCase(filter)) {
            return getTasksBySelection(COL_TASK_PRIORITY + " = ?", new String[]{"High"});
        } else {
            return getAllTasks();
        }
    }

    public List<Task> getTasksForToday(String todayDate) {
        return getTasksBySelection(COL_TASK_DUE_DATE + " = ?", new String[]{todayDate});
    }

    private List<Task> getTasksBySelection(String selection, String[] selectionArgs) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs,
                null, null, COL_TASK_COMPLETED + " ASC, " + COL_TASK_DUE_DATE + " ASC");

        if (cursor != null) {
            try {
                int idIdx = cursor.getColumnIndexOrThrow(COL_TASK_ID);
                int titleIdx = cursor.getColumnIndexOrThrow(COL_TASK_TITLE);
                int descIdx = cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION);
                int subjectIdx = cursor.getColumnIndexOrThrow(COL_TASK_SUBJECT);
                int dateIdx = cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE);
                int priorityIdx = cursor.getColumnIndexOrThrow(COL_TASK_PRIORITY);
                int compIdx = cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED);

                while (cursor.moveToNext()) {
                    Task task = new Task(
                            String.valueOf(cursor.getLong(idIdx)),
                            cursor.getString(titleIdx),
                            cursor.getString(descIdx),
                            cursor.getString(subjectIdx),
                            cursor.getString(dateIdx),
                            cursor.getString(priorityIdx),
                            cursor.getInt(compIdx) == 1
                    );
                    tasks.add(task);
                }
            } finally {
                cursor.close();
            }
        }
        return tasks;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, task.getTitle());
        cv.put(COL_TASK_DESCRIPTION, task.getDescription());
        cv.put(COL_TASK_SUBJECT, task.getSubject());
        cv.put(COL_TASK_DUE_DATE, task.getDueDate());
        cv.put(COL_TASK_PRIORITY, task.getPriority());
        cv.put(COL_TASK_COMPLETED, task.isCompleted() ? 1 : 0);

        return db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
    }

    public int deleteTask(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TASKS, COL_TASK_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int deleteTask(long id) {
        return deleteTask(String.valueOf(id));
    }

    public int setTaskCompleted(String id, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_COMPLETED, completed ? 1 : 0);
        return db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int setTaskCompleted(long id, boolean completed) {
        return setTaskCompleted(String.valueOf(id), completed);
    }

    public int getTotalTaskCount() {
        return getCountQuery("SELECT COUNT(*) FROM " + TABLE_TASKS, null);
    }

    public int getCompletedTaskCount() {
        return getCountQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_COMPLETED + " = 1", null);
    }

    public int getPendingTaskCount() {
        return getCountQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_COMPLETED + " = 0", null);
    }

    public int getTotalTaskCountBySubject(String subject) {
        return getCountQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_SUBJECT + " = ?",
                new String[]{subject});
    }

    public int getCompletedTaskCountBySubject(String subject) {
        return getCountQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_SUBJECT + " = ? AND " + COL_TASK_COMPLETED + " = 1",
                new String[]{subject});
    }

    private int getCountQuery(String query, String[] args) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, args);
        int count = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return count;
    }
}

