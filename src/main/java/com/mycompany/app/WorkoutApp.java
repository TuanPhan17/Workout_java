package com.mycompany.app;

import java.util.Scanner; // Allows user input from terminal

public class WorkoutApp {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in); // Create Scanner object

        System.out.println("=== Create a Workout ==="); // Title

        System.out.print("Enter date (MM/DD/YR): ");
        String date = sc.nextLine(); // Read date input

        System.out.print("Enter exercise name: ");
        String exercise = sc.nextLine(); // Read exercise input

        System.out.print("Enter weight used: ");
        double weight = sc.nextDouble(); // Read weight

        System.out.print("Enter reps: ");
        int reps = sc.nextInt(); // Read reps

        System.out.print("Enter number of sets: ");
        int sets = sc.nextInt(); // Read sets
        sc.nextLine(); // Clear leftover newline from nextInt

        System.out.print("Any notes? (press Enter to skip): ");
        String note = sc.nextLine(); // Read note (can be empty)

        System.out.print("Mark as completed? (y/n): ");
        String answer = sc.nextLine(); // Read y or n
        boolean completed = answer.equalsIgnoreCase("y"); // Convert to true/false

        Workout workout = new Workout(date, exercise, weight, reps, sets, note, completed); // Create Workout object

        System.out.println("\nWorkout Created Successfully!");
        System.out.println(workout); // Print workout using toString()

        sc.close(); // Close Scanner
    }
}