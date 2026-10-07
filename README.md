# StudyPlanner - Student Study Planner Android App

StudyPlanner is a native Android application designed to help students organize their academic life, track subjects, manage assignments/tasks, and monitor study progress in real-time.

## Features

- **Dashboard (Home)**:
  - Personalized time-based greeting and today's date.
  - Live statistics: Total Subjects, Pending Tasks, Completed Tasks.
  - Overall progress indicator.
  - Dedicated "Today's Tasks" list with direct completion toggles.

- **Subjects Management**:
  - Add, edit, view, and delete course subjects.
  - Track course codes, faculty names, and target study hours.

- **Task Management**:
  - Create and manage tasks with title, description, subject tag, due date, and priority level (Low, Medium, High).
  - Built-in DatePicker dialog for easy due date selection.
  - Filter chips: All, Pending, Completed, High Priority.
  - Instant checkbox toggle with completion styling.

- **Academic Progress & Analytics**:
  - Real-time overall completion rate calculation.
  - Subject-wise progress breakdown and study targets.

- **Student Profile**:
  - Student details (Name, Register Number, Department, College, Email).
  - Dual persistence (Cloud Firestore sync + SharedPreferences for instant offline display).

## Technology Stack

- **Platform**: Android Native
- **Language**: Java
- **UI Design**: XML layouts with Material Components (Material Cards, Chips, Buttons, TextInputs)
- **Database**: Firebase Cloud Firestore (Real-time synchronization)
- **Architecture**: Single Activity (`MainActivity`) with `BottomNavigationView` and Fragments

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/sankarmahasivan7/Android-app-.git
   ```
2. Open the project in **Android Studio**.
3. Connect your Firebase project:
   - Add your Android app package `com.example.studyplanner` in [Firebase Console](https://console.firebase.google.com/).
   - Download `google-services.json` and replace the template at `app/google-services.json`.
   - Enable **Cloud Firestore** in test mode.
4. Sync Gradle and Run the application on an emulator or Android device.
