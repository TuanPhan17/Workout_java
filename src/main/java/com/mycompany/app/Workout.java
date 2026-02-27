package com.mycompany.app; // Package for the workout app

import java.util.ArrayList;
import java.util.List;

public class Workout { // Represents one workout entry

    private String date; // Workout date
    private String exercise; // Exercise name
    private double weight; // Weight used
    private int reps; // Number of reps
    private int sets; // Number of sets
    private String note; // Extra notes
    private boolean completed; // Whether workout is done

    // Constructor sets all workout details
    public Workout(String date, String exercise, double weight, int reps, int sets, String note, boolean completed) {
        this.date = date; // Set date
        this.exercise = exercise; // Set exercise
        this.weight = weight; // Set weight
        this.reps = reps; // Set reps
        this.sets = sets; // Set sets
        this.note = note; // Set note
        this.completed = completed; // Set completed status
    }

    public String getDate() { // Get date
        return date;
    }

    public String getExercise() { // Get exercise
        return exercise;
    }

    public double getWeight() { // Get weight
        return weight;
    }

    public int getReps() { // Get reps
        return reps;
    }

    public int getSets() { // Get sets
        return sets;
    }

    public String getNote() { // Get note
        return note;
    }

    public boolean isCompleted() { // Check if completed
        return completed;
    }

    public void setCompleted(boolean completed) { // Update completed status
        this.completed = completed;
    }

    public String toString() { // Print workout nicely
        return "Date: " + date +
                ", Exercise: " + exercise +
                ", Weight: " + weight +
                ", Reps: " + reps +
                ", Sets: " + sets +
                ", Note: " + note +
                ", Completed: " + completed;
    }

    // Convert Workout object into a CSV-safe line for file persistence.
    public String toCsvLine() {
        List<String> fields = new ArrayList<>();
        fields.add(date);
        fields.add(exercise);
        fields.add(String.valueOf(weight));
        fields.add(String.valueOf(reps));
        fields.add(String.valueOf(sets));
        fields.add(note);
        fields.add(String.valueOf(completed));
        return toCsv(fields);
    }

    // Build a Workout object from one CSV line loaded from storage.
    public static Workout fromCsvLine(String line) {
        List<String> fields = parseCsv(line);
        if (fields.size() < 7) {
            throw new IllegalArgumentException("Invalid workout CSV line: " + line);
        }

        String date = fields.get(0).trim();
        String exercise = fields.get(1).trim();
        double weight = Double.parseDouble(fields.get(2).trim());
        int reps = Integer.parseInt(fields.get(3).trim());
        int sets = Integer.parseInt(fields.get(4).trim());
        String note = fields.get(5).trim();
        boolean completed = Boolean.parseBoolean(fields.get(6).trim());

        return new Workout(date, exercise, weight, reps, sets, note, completed);
    }

    private static String toCsv(List<String> fields) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            out.append(escapeCsv(fields.get(i)));
        }
        return out.toString();
    }

    private static String escapeCsv(String value) {
        String safe = value == null ? "" : value.replace("\r", " ").replace("\n", " ").trim();
        boolean needsQuotes = safe.contains(",") || safe.contains("\"");
        safe = safe.replace("\"", "\"\"");
        return needsQuotes ? "\"" + safe + "\"" : safe;
    }

    private static List<String> parseCsv(String line) {
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
}