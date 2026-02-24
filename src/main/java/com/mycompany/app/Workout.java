package com.mycompany.app; // Package for the workout app

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
}