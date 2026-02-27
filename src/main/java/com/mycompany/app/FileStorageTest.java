package com.mycompany.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Lightweight integration tests for FileStorage.
 */
public class FileStorageTest {

    public static void main(String[] args) throws Exception {
        testSaveLoadTypedRoundTrip();
        testInvalidRowsSkipped();
        testBackupCreation();
        testQuotedCsvHandling();
        System.out.println("All FileStorage tests passed.");
    }

    private static void testSaveLoadTypedRoundTrip() throws Exception {
        Path tempDir = Files.createTempDirectory("workout-storage-roundtrip-");
        String filePath = tempDir.resolve("workouts_roundtrip.csv").toString();

        FileStorage storage = new FileStorage(filePath);
        storage.setAutoBackupEnabled(false);

        List<Workout> workouts = List.of(
                new Workout("2/21/2026", "Bench Press", 135.0, 10, 3, "Strong session", true),
                new Workout("2/22/2026", "Pull Ups", 0.0, 12, 3, "Bodyweight", false)
        );

        assertTrue(storage.saveWorkoutObjects(workouts, filePath), "Typed save should succeed");

        List<Workout> loaded = storage.loadWorkoutObjects(filePath);
        assertEquals(2, loaded.size(), "Round-trip should preserve workout count");
        assertEquals("Bench Press", loaded.get(0).getExercise(), "Exercise should match");
        assertEquals(3, loaded.get(0).getSets(), "Sets should match");
    }

    private static void testInvalidRowsSkipped() throws Exception {
        Path tempDir = Files.createTempDirectory("workout-storage-invalid-");
        Path csv = tempDir.resolve("workouts_invalid.csv");

        List<String> lines = List.of(
                "date,exercise,weight,reps,sets,note,completed",
                "2/21/2026,Squat,185.0,8,4,Valid,true",
                "2/21/2026,BadNegative,-10,8,3,Invalid,false",
                "bad-date,BadDate,100,10,3,Invalid,true"
        );
        Files.write(csv, lines);

        FileStorage storage = new FileStorage(csv.toString());
        FileStorage.OperationResult<List<String>> result = storage.loadWorkoutsWithResult(csv.toString());

        assertEquals(3, result.getProcessedCount(), "Should process 3 data rows");
        assertEquals(1, result.getSuccessCount(), "Should keep only valid rows");
        assertEquals(2, result.getSkippedCount(), "Should skip invalid rows");
        assertTrue(result.hasErrors(), "Result should include validation errors");
    }

    private static void testBackupCreation() throws Exception {
        Path tempDir = Files.createTempDirectory("workout-storage-backup-");
        String uniqueName = "workouts_backup_" + System.nanoTime() + ".csv";
        String filePath = tempDir.resolve(uniqueName).toString();

        FileStorage storage = new FileStorage(filePath);
        storage.setAutoBackupEnabled(true);

        List<String> data1 = List.of("2/21/2026,Bench Press,135.0,10,3,First save,true");
        List<String> data2 = List.of("2/22/2026,Bench Press,140.0,8,3,Second save,false");

        storage.saveWorkouts(data1, filePath);
        storage.saveWorkouts(data2, filePath);

        String backupPrefix = uniqueName.replace(".csv", "_backup_");
        long backupCount = Files.list(Path.of("data", "backups"))
                .filter(path -> path.getFileName().toString().startsWith(backupPrefix))
                .count();

        assertTrue(backupCount >= 1, "Second save should create at least one backup");
    }

    private static void testQuotedCsvHandling() throws Exception {
        Path tempDir = Files.createTempDirectory("workout-storage-csv-");
        String filePath = tempDir.resolve("workouts_quoted.csv").toString();

        FileStorage storage = new FileStorage(filePath);
        storage.setAutoBackupEnabled(false);

        List<Workout> workouts = List.of(
                new Workout("2/24/2026", "Incline Bench", 95.0, 12, 3, "Felt \"great\", strong form", true)
        );

        storage.saveWorkoutObjects(workouts, filePath);
        List<Workout> loaded = storage.loadWorkoutObjects(filePath);

        assertEquals(1, loaded.size(), "Quoted CSV should round-trip with one row");
        assertEquals("Felt \"great\", strong form", loaded.get(0).getNote(), "Quoted/comma notes should be preserved");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if ((expected == null && actual != null) || (expected != null && !expected.equals(actual))) {
            throw new AssertionError(message + " Expected=" + expected + " Actual=" + actual);
        }
    }
}
