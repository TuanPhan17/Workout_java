package com.mycompany.app;

import java.util.ArrayList;
import java.util.List;

// WorkoutLog manages a list of workout entries
public class WorkoutLog {

    private List<Workout> workoutList; // List to store all workouts

    // Constructor initializes the workout list
    public WorkoutLog() {
        workoutList = new ArrayList<>();
    }

    // Add a new workout to the log
    public void addWorkout(Workout workout) {
        workoutList.add(workout);
    }

    // Remove a workout by its index
    public void removeWorkout(int workoutIndex) {
        if (workoutIndex >= 0 && workoutIndex < workoutList.size()) {
            workoutList.remove(workoutIndex);
        } else {
            System.out.println("Invalid index. No workout removed.");
        }
    }

    // Get a workout by its index
    public Workout getWorkout(int workoutIndex) {
        if (workoutIndex >= 0 && workoutIndex < workoutList.size()) {
            return workoutList.get(workoutIndex);
        } else {
            System.out.println("Invalid index. Returning null.");
            return null;
        }
    }

    // List all workouts
    public void listAllWorkouts() {
        if (workoutList.isEmpty()) {
            System.out.println("No workouts in the log.");
        } else {
            for (int i = 0; i < workoutList.size(); i++) {
                System.out.println("[" + i + "] " + workoutList.get(i));
            }
        }
    }

    // Mark a workout as completed
    public void markCompleted(int index) {
        if (index >= 0 && index < workoutList.size()) {
            workoutList.get(index).setCompleted(true);
            System.out.println("Workout marked as completed.");
        } else {
            System.out.println("Invalid index. Cannot mark completed.");
        }
    }

    // Get the total number of workouts
    public int getTotalWorkouts() {
        return workoutList.size();
    }

    // Return all workouts so persistence layer can save them.
    public List<Workout> getAllWorkouts() {
        return new ArrayList<>(workoutList);
    }
}