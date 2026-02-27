# Workout Log (Java)

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-blue?logo=apachemaven&logoColor=white)
![Persistence](https://img.shields.io/badge/Storage-CSV%20%2B%20Backups-success)
![Status](https://img.shields.io/badge/Tests-Local%20Passing-brightgreen)

A console-based **Workout Log** application for tracking workouts, saving progress to disk, and loading history on startup.

---

## Features

- Add and view workout entries from a CLI menu
- Persist workout data to CSV on exit
- Load existing workout history on startup
- Validation for invalid entries (negative numbers, invalid date format)
- CSV-safe parsing/escaping (supports commas and quotes in notes)
- Automatic backup creation/rotation
- Thread-safe read/write file operations (`ReentrantReadWriteLock`)
- Lightweight integration tests for storage behavior

---

## Tech Stack

- **Language:** Java
- **Build config:** Maven (`pom.xml`)
- **Data format:** CSV
- **Storage location:** `data/workouts.csv`

---

## Project Structure

```text
Workout_java/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── mycompany/
                    └── app/
                        ├── WorkoutApp.java
                        ├── WorkoutLog.java
                        ├── Workout.java
                        ├── FileStorage.java
                        └── FileStorageTest.java
```

---

## Core Classes

- **WorkoutApp**: Main menu and application flow
- **WorkoutLog**: In-memory list manager for workouts
- **Workout**: Workout data model + CSV conversion helpers
- **FileStorage**: Save/load, validation, backups, locking, operation results
- **FileStorageTest**: Integration-style tests for storage reliability

---

## Getting Started

### 1) Clone

```bash
git clone https://github.com/TuanPhan17/Workout_java.git
cd Workout_java
```

### 2) Compile

```bash
javac -d target/classes src/main/java/com/mycompany/app/*.java
```

### 3) Run App

```bash
java -cp target/classes com.mycompany.app.WorkoutApp
```

### 4) Run Storage Tests

```bash
java -cp target/classes com.mycompany.app.FileStorageTest
```

---

## Data Format

CSV header:

```csv
date,exercise,weight,reps,sets,note,completed
```

Example entry:

```csv
2/21/2026,Bench Press,135.0,10,3,"Felt ""great"", strong form",true
```

---

## Team Workflow Notes

- Keep code in standard Java project structure (`src/main/java/...`)
- Add method-level comments for major logic blocks
- Test after integration changes before pushing
- Keep runtime-generated folders (like `data/`) out of commits unless explicitly needed

---

## Future Improvements

- Add unit tests with JUnit in Maven lifecycle
- Add CI workflow (GitHub Actions) for automatic compile/test checks
- Improve CLI UX (input retries, clearer validation prompts)
- Add filter/search features for workout history

---

## License

This project is for educational use in **CS132** team coursework.
