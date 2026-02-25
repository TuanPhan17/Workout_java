package com.mycompany.app;

import java.util.ArrayList
import java.util.Lists;

// WorkoutLog manages a list of workout entries
public class WorkoutLog {

    private List<Workout> workoutList; // List to store all workout

    // Constructor initializes the workout list
    public Workoutlog() {
        workoutList = new ArrayList<>();
    }

    // Add a new workout to the log
    public void addWorkout (Workout workout) {
        workoutList.add(workout);
    }

    // Remove a workout by its index
    public void removeWorkout(int workoutIndex) {
        if (workoutIndex >= 0 && workoutIndex < workoutList.size()) {
            workoutList.remove.(workoutIndex);
        } else {
            System.out.println("Invalid index, No workout removed.");
        }
    }

    // Get a workout by its index
    public Workout getWorkout(int workoutIndex) {
        if (workoutIndex >= && workoutIndex < workoutList.size()) {
            return workoutList.get(workoutIndex);
        } else {
            System.out.println("Invalid index. Returning null.");
            return null;
        }
    }

    // List all workout
    public void listAllWorkouts() {
        if (workoutList.isEmpty()) {
            System.out.println("No workouts in the log.");
        } else {
            for (int workoutIndex = 0; workoutIndex < workoutList.size(); workoutIndex++) {
                System.out.println("[" + workoutIndex + "]" + workoutList.get(workoutIndex));
            }
        }
    }

    // Mark a workout as completed
    public void markCompleted(int index) {
        if (workoutIndex >= 0 && workoutIndex < workoutList.size()) {
            workoutList.get(workoutIndex).setCompleted(true);
            System.out.println("Workout marked as completed.");
        } else {
            System.out.println("Invalid index. Cannot mark completed.");
        }
    }

    // Get the total number of workouts
    public int getTotalWorkouts() {
        return workoutList.size();
    }
}