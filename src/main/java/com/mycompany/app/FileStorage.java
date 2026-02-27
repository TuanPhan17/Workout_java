package com.mycompany.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Handles workout persistence with CSV save/load, backup support, validation,
 * and structured operation results.
 */
public class FileStorage {

    private static final String DEFAULT_FILENAME = "workouts.csv";
    private static final String DATA_DIRECTORY = "data";
    private static final String BACKUP_DIRECTORY = "data/backups";
    private static final String DELIMITER = ",";
    private static final String CSV_HEADER = "date,exercise,weight,reps,sets,note,completed";
    private static final int MAX_BACKUPS = 5;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private String currentFilePath;
    private boolean autoBackupEnabled;
    private int operationCount;
    private final ReentrantReadWriteLock fileLock = new ReentrantReadWriteLock();

    public static class OperationResult<T> {
        private final T data;
        private final int processedCount;
        private final int successCount;
        private final int skippedCount;
        private final List<String> errors;

        public OperationResult(T data, int processedCount, int successCount, int skippedCount, List<String> errors) {
            this.data = data;
            this.processedCount = processedCount;
            this.successCount = successCount;
            this.skippedCount = skippedCount;
            this.errors = new ArrayList<>(errors);
        }

        public T getData() {
            return data;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    public FileStorage() {
        this.currentFilePath = DATA_DIRECTORY + File.separator + DEFAULT_FILENAME;
        this.autoBackupEnabled = true;
        this.operationCount = 0;
        initializeDirectories();
    }

    public FileStorage(String customFilePath) {
        this.currentFilePath = customFilePath;
        this.autoBackupEnabled = true;
        this.operationCount = 0;
        initializeDirectories();
    }

    public boolean saveWorkoutObjects(List<Workout> workouts) throws FileStorageException {
        return saveWorkoutObjects(workouts, currentFilePath);
    }

    public boolean saveWorkoutObjects(List<Workout> workouts, String filePath) throws FileStorageException {
        if (workouts == null) {
            throw new FileStorageException("Cannot save null workout list");
        }

        List<String> lines = new ArrayList<>();
        for (Workout workout : workouts) {
            if (workout != null) {
                lines.add(workout.toCsvLine());
            }
        }

        return saveWorkouts(lines, filePath);
    }

    public List<Workout> loadWorkoutObjects() throws FileStorageException {
        return loadWorkoutObjects(currentFilePath);
    }

    public List<Workout> loadWorkoutObjects(String filePath) throws FileStorageException {
        OperationResult<List<String>> result = loadWorkoutsWithResult(filePath);
        List<Workout> workouts = new ArrayList<>();

        for (String line : result.getData()) {
            workouts.add(Workout.fromCsvLine(line));
        }

        return workouts;
    }

    public boolean saveWorkouts(List<String> workoutData) throws FileStorageException {
        return saveWorkouts(workoutData, currentFilePath);
    }

    public boolean saveWorkouts(List<String> workoutData, String filePath) throws FileStorageException {
        OperationResult<Void> result = saveWorkoutsWithResult(workoutData, filePath);
        return !result.hasErrors();
    }

    public OperationResult<Void> saveWorkoutsWithResult(List<String> workoutData, String filePath)
            throws FileStorageException {
        if (workoutData == null) {
            throw new FileStorageException("Cannot save null workout data");
        }

        fileLock.writeLock().lock();
        try {
            int processedCount = workoutData.size();
            int successCount = 0;
            int skippedCount = 0;
            List<String> errors = new ArrayList<>();

            if (autoBackupEnabled && fileExists(filePath)) {
                createBackup(filePath);
            }

            String tempFilePath = filePath + ".tmp";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFilePath))) {
                writer.write(CSV_HEADER);
                writer.newLine();

                for (int i = 0; i < workoutData.size(); i++) {
                    String line = workoutData.get(i);
                    if (!isValidWorkoutEntry(line)) {
                        skippedCount++;
                        errors.add("Skipped invalid entry at index " + i + ": " + line);
                        continue;
                    }

                    List<String> fields = parseCsvLine(line);
                    fields.set(1, normalizeExerciseName(fields.get(1)));
                    writer.write(toCsvLine(fields));
                    writer.newLine();
                    successCount++;
                }

                writer.flush();
            } catch (IOException e) {
                deleteFile(tempFilePath);
                throw new FileStorageException("Failed to write workout data: " + e.getMessage(), e);
            }

            try {
                Path source = Paths.get(tempFilePath);
                Path target = Paths.get(filePath);
                Files.deleteIfExists(target);
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);

                operationCount++;
                logOperation("Saved " + successCount + " workouts to " + filePath + " (skipped " + skippedCount + ")");
            } catch (IOException e) {
                throw new FileStorageException("Failed to finalize save operation: " + e.getMessage(), e);
            }

            return new OperationResult<>(null, processedCount, successCount, skippedCount, errors);
        } finally {
            fileLock.writeLock().unlock();
        }
    }

    public List<String> loadWorkouts() throws FileStorageException {
        return loadWorkouts(currentFilePath);
    }

    public List<String> loadWorkouts(String filePath) throws FileStorageException {
        return loadWorkoutsWithResult(filePath).getData();
    }

    public OperationResult<List<String>> loadWorkoutsWithResult(String filePath) throws FileStorageException {
        fileLock.readLock().lock();
        try {
            List<String> workouts = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int processedCount = 0;
            int skippedCount = 0;

            if (!fileExists(filePath)) {
                return new OperationResult<>(workouts, 0, 0, 0, errors);
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (lineNumber == 1 && line.equals(CSV_HEADER)) {
                        continue;
                    }
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    processedCount++;
                    if (isValidWorkoutEntry(line)) {
                        workouts.add(toCsvLine(parseCsvLine(line)));
                    } else {
                        skippedCount++;
                        errors.add("Invalid entry at line " + lineNumber + ": " + line);
                    }
                }

                operationCount++;
                logOperation("Loaded " + workouts.size() + " workouts from " + filePath + " (skipped " + skippedCount + ")");
            } catch (FileNotFoundException e) {
                return new OperationResult<>(workouts, 0, 0, 0, errors);
            } catch (IOException e) {
                throw new FileStorageException("Failed to read workout data: " + e.getMessage(), e);
            }

            return new OperationResult<>(workouts, processedCount, workouts.size(), skippedCount, errors);
        } finally {
            fileLock.readLock().unlock();
        }
    }

    public boolean createBackup(String filePath) {
        if (!fileExists(filePath)) {
            return false;
        }

        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String originalName = Paths.get(filePath).getFileName().toString();
            String backupName = originalName.replace(".csv", "_backup_" + timestamp + ".csv");
            String backupPath = BACKUP_DIRECTORY + File.separator + backupName;
            Files.copy(Paths.get(filePath), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
            rotateBackups();
            return true;
        } catch (IOException e) {
            System.err.println("[FileStorage] Backup failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isValidWorkoutEntry(String entry) {
        if (entry == null || entry.trim().isEmpty()) {
            return false;
        }

        List<String> parts = parseCsvLine(entry);
        if (parts.size() < 7) {
            return false;
        }

        String date = parts.get(0).trim();
        String exercise = normalizeExerciseName(parts.get(1));
        if (date.isEmpty() || !isValidDate(date) || exercise.isEmpty()) {
            return false;
        }

        try {
            double weight = Double.parseDouble(parts.get(2).trim());
            int reps = Integer.parseInt(parts.get(3).trim());
            int sets = Integer.parseInt(parts.get(4).trim());
            if (weight < 0 || reps < 0 || sets < 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        String completed = parts.get(6).trim().toLowerCase();
        return "true".equals(completed) || "false".equals(completed);
    }

    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            return false;
        }
    }

    public void setAutoBackupEnabled(boolean enabled) {
        this.autoBackupEnabled = enabled;
    }

    public String getCurrentFilePath() {
        return currentFilePath;
    }

    public void setCurrentFilePath(String filePath) {
        this.currentFilePath = filePath;
    }

    public int getOperationCount() {
        return operationCount;
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
            Files.createDirectories(Paths.get(BACKUP_DIRECTORY));
        } catch (IOException e) {
            System.err.println("[FileStorage] Could not create storage directories: " + e.getMessage());
        }
    }

    private void rotateBackups() {
        try {
            File backupDir = new File(BACKUP_DIRECTORY);
            File[] backups = backupDir.listFiles((dir, name) -> name.contains("_backup_"));
            if (backups != null && backups.length > MAX_BACKUPS) {
                java.util.Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
                int toDelete = backups.length - MAX_BACKUPS;
                for (int i = 0; i < toDelete; i++) {
                    backups[i].delete();
                }
            }
        } catch (Exception e) {
            System.err.println("[FileStorage] Backup rotation failed: " + e.getMessage());
        }
    }

    private boolean isValidDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException ignored) {
            try {
                DateTimeFormatter us = DateTimeFormatter.ofPattern("M/d/yy");
                LocalDate.parse(value, us);
                return true;
            } catch (DateTimeParseException ignoredAgain) {
                try {
                    DateTimeFormatter us4 = DateTimeFormatter.ofPattern("M/d/yyyy");
                    LocalDate.parse(value, us4);
                    return true;
                } catch (DateTimeParseException e) {
                    return false;
                }
            }
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        fields.add(current.toString().trim());
        return fields;
    }

    private String toCsvLine(List<String> fields) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                csv.append(DELIMITER);
            }
            csv.append(escapeCsvField(fields.get(i)));
        }
        return csv.toString();
    }

    private String escapeCsvField(String value) {
        String safe = value == null ? "" : value.replace("\r", " ").replace("\n", " ").trim();
        boolean requiresQuotes = safe.contains(DELIMITER) || safe.contains("\"");
        safe = safe.replace("\"", "\"\"");
        return requiresQuotes ? "\"" + safe + "\"" : safe;
    }

    private String normalizeExerciseName(String exercise) {
        if (exercise == null) {
            return "";
        }
        return exercise.trim().replaceAll("\\s+", " ");
    }

    private void logOperation(String message) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[FileStorage " + ts + "] " + message);
    }

    public static class FileStorageException extends Exception {
        public FileStorageException(String message) {
            super(message);
        }

        public FileStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
