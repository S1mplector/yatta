package com.animetui.adapter.tui;

import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Utility class for TUI view operations.
 * Provides common formatting and user interaction helpers.
 */
public class ViewHelpers {
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    
    /**
     * Display a header with styling.
     */
    public static void printHeader(String title) {
        System.out.println();
        System.out.println(ANSI_BOLD + ANSI_CYAN + "=".repeat(50) + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + centerText(title, 50) + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + "=".repeat(50) + ANSI_RESET);
        System.out.println();
    }
    
    /**
     * Display an error message with styling.
     */
    public static void printError(String message) {
        System.out.println(ANSI_RED + "ERROR: " + message + ANSI_RESET);
    }
    
    /**
     * Display a success message with styling.
     */
    public static void printSuccess(String message) {
        System.out.println(ANSI_GREEN + "SUCCESS: " + message + ANSI_RESET);
    }
    
    /**
     * Display a warning message with styling.
     */
    public static void printWarning(String message) {
        System.out.println(ANSI_YELLOW + "WARNING: " + message + ANSI_RESET);
    }
    
    /**
     * Display an info message with styling.
     */
    public static void printInfo(String message) {
        System.out.println(ANSI_BLUE + "INFO: " + message + ANSI_RESET);
    }
    
    /**
     * Center text within a given width.
     */
    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
    
    /**
     * Display a numbered list and let user pick an item.
     */
    public static <T> int pickFromList(String prompt, List<T> items, Function<T, String> displayFunction, Scanner scanner) {
        if (items.isEmpty()) {
            printWarning("No items available to select from.");
            return -1;
        }
        
        System.out.println(ANSI_BOLD + prompt + ANSI_RESET);
        System.out.println();
        
        for (int i = 0; i < items.size(); i++) {
            String displayText = displayFunction.apply(items.get(i));
            System.out.printf("%s%2d.%s %s%n", ANSI_CYAN, i + 1, ANSI_RESET, displayText);
        }
        
        System.out.println();
        System.out.print("Enter your choice (1-" + items.size() + "): ");
        
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.print("Please enter a number: ");
                    continue;
                }
                
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= items.size()) {
                    return choice - 1; // Convert to 0-based index
                } else {
                    System.out.print("Please enter a number between 1 and " + items.size() + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
    
    /**
     * Display a simple menu and get user choice.
     */
    public static int showMenu(String title, List<String> options, Scanner scanner) {
        printHeader(title);
        
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("%s%2d.%s %s%n", ANSI_CYAN, i + 1, ANSI_RESET, options.get(i));
        }
        
        System.out.println();
        System.out.print("Enter your choice (1-" + options.size() + "): ");
        
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.print("Please enter a number: ");
                    continue;
                }
                
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= options.size()) {
                    return choice - 1; // Convert to 0-based index
                } else {
                    System.out.print("Please enter a number between 1 and " + options.size() + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
    
    /**
     * Get user input with a prompt.
     */
    public static String getInput(String prompt, Scanner scanner) {
        System.out.print(ANSI_BOLD + prompt + ANSI_RESET + " ");
        return scanner.nextLine().trim();
    }
    
    /**
     * Display a loading message.
     */
    public static void showLoading(String message) {
        System.out.print(ANSI_YELLOW + message + "..." + ANSI_RESET);
        System.out.flush();
    }
    
    /**
     * Clear the loading message and show completion.
     */
    public static void clearLoading() {
        System.out.println(" " + ANSI_GREEN + "Done!" + ANSI_RESET);
    }
    
    /**
     * Wait for user to press Enter.
     */
    public static void waitForEnter(Scanner scanner) {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
