package com.mycompany.app; // Package name

import java.util.Scanner; // Allows user input

public class WorkoutApp { // Main application class

    public static void main(String[] args) { // Program starts here

        Scanner sc = new Scanner(System.in); // Create Scanner object
        WorkoutLog log = new WorkoutLog();   // Create WorkoutLog to store workouts

        boolean running = true; // Controls the menu loop

        while (running) { // Keep program running until user exits

            // Display menu options
            System.out.println("\n==== Workout Menu ====");
            System.out.println("1. Add Workout");
            System.out.println("2. View Workouts");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = sc.nextInt(); // Read user choice
            sc.nextLine(); // Clear leftover newline

            switch (choice) { // Check which option user selected

                case 1: // Add a workout

                    System.out.print("Enter date (MM/DD/YR): ");
                    String date = sc.nextLine(); // Get date

                    System.out.print("Enter exercise name: ");
                    String exercise = sc.nextLine(); // Get exercise name

                    System.out.print("Enter weight used: ");
                    double weight = sc.nextDouble(); // Get weight

                    System.out.print("Enter reps: ");
                    int reps = sc.nextInt(); // Get reps

                    System.out.print("Enter number of sets: ");
                    int sets = sc.nextInt(); // Get sets
                    sc.nextLine(); // Clear newline

                    System.out.print("Any notes? ");
                    String note = sc.nextLine(); // Get notes

                    System.out.print("Mark as completed? (y/n): ");
                    boolean completed = sc.nextLine().equalsIgnoreCase("y"); // Convert to true/false

                    // Create Workout object
                    Workout workout = new Workout(date, exercise, weight, reps, sets, note, completed);

                    log.addWorkout(workout); // Add workout to the log

                    System.out.println("Workout added!"); // Confirmation message
                    break; // End case 1

                case 2: // View workouts

                    log.listAllWorkouts(); // Show all stored workouts
                    break; // End case 2

                case 3: // Exit program

                    running = false; // Stop the loop
                    System.out.println("Exiting program..."); // Exit message
                    break; // End case 3

                default: // If user enters invalid option

                    System.out.println("Invalid choice."); // Show error message
            }
        }

        sc.close(); // Close Scanner before program ends
    }
}