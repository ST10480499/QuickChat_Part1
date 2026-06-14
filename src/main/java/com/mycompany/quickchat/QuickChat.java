package com.mycompany.quickchat;

import java.util.Scanner;

/**
 * Main application class for QuickChat.
 * Part 1: Registration and Login
 * Part 2: Messaging system with send, disregard and store
 * Part 3: Stored messages menu with arrays and report
 * @author khoza
 */
public class QuickChat {

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {

            // Part 1: Registration 
            System.out.print("Enter your first name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter your last name: ");
            String lastName = scanner.nextLine();

            // Create Login object with user's name
            Login login = new Login(firstName, lastName);

            System.out.println("\n=== Registration ===");

            // Validate and register username
            System.out.print("Enter username (must contain '_' and be 5 or fewer characters): ");
            String username = scanner.nextLine();
            System.out.println(login.validateUsername(username));

            // Validate and register password
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            System.out.println(login.validatePassword(password));

            // Validate and register cell phone number
            System.out.print("Enter cell phone number (e.g. +27838968976): ");
            String cell = scanner.nextLine();
            System.out.println(login.validateCellPhone(cell));

            // Complete registration
            System.out.println(login.registerUser(username, password, cell));

            // Part 1: Login 
            System.out.println("\n=== Login ===");

            System.out.print("Enter username: ");
            String lu = scanner.nextLine();

            System.out.print("Enter password: ");
            String lp = scanner.nextLine();

            // Display login result
            System.out.println(login.returnLoginStatus(lu, lp));

            // Part 2: Only continue if login was successful 
            if (!login.loginUser(lu, lp)) {
                System.out.println("Login failed. Exiting application.");
                return;
            }

            // Display welcome message as required by spec
            System.out.println("\nWelcome to QuickChat.");

            // Ask user how many messages to send upfront
            System.out.print("How many messages do you wish to send? ");
            int maxMessages = 0;
            try {
                maxMessages = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number entered. Exiting.");
                return;
            }

            //  Part 3: Load stored messages from JSON at startup 
            System.out.println(Message.readStoredMessagesFromJSON());

            // Main Menu Loop: runs until user selects Quit
            boolean running = true;
            while (running) {
                System.out.println("\n--- Menu ---");
                System.out.println("1) Send Messages");
                System.out.println("2) Show recently sent messages");
                System.out.println("3) Quit");
                System.out.println("4) Stored Messages");
                System.out.print("Choose an option: ");
                String menuChoice = scanner.nextLine().trim();

                switch (menuChoice) {
                    case "1" -> sendMessages(scanner, maxMessages);
                    case "2" -> // Feature still in development
                        System.out.println("Coming Soon.");
                    case "3" -> {
                        running = false;
                        System.out.println("Goodbye! Thank you for using QuickChat.");
                    }
                    case "4" -> // Part 3: Stored messages menu
                        storedMessagesMenu(scanner);
                    default -> System.out.println("Invalid option. Please enter 1, 2, 3, or 4.");
                }
            }
        }
    }

    //  Send Messages Method 
    // Handles the full send message flow including validation
    private static void sendMessages(Scanner scanner, int maxMessages) {

        int sentThisSession = 0;

        while (sentThisSession < maxMessages) {

            System.out.println("\n--- Message " + (sentThisSession + 1)
                    + " of " + maxMessages + " ---");

            // Get and validate recipient number
            System.out.print("Enter recipient cell number (with international code e.g. +27...): ");
            String recipient = scanner.nextLine().trim();

            Message msg = new Message(recipient, "placeholder");
            String recipientCheck = msg.checkRecipientCell();
            System.out.println(recipientCheck);

            if (!recipientCheck.equals("Cell phone number successfully captured.")) {
                System.out.println("Message cancelled. Returning to menu.");
                return;
            }

            // Get and validate message text
            String messageText;
            while (true) {
                System.out.print("Enter your message (max 250 characters): ");
                messageText = scanner.nextLine();
                msg = new Message(recipient, messageText);

                String validation = msg.validateMessage();
                System.out.println(validation);

                if (validation.equals("Message ready to send.")) {
                    break;
                }
                System.out.println("Please enter a message of less than 250 characters.");
            }

            // Show message hash preview before sending
            System.out.println("Message Hash: " + msg.createMessageHash());

            // Ask user what to do with the message
            System.out.println("\nWhat would you like to do?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Choose: ");

            int action = 0;
            try {
                action = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Message discarded.");
                continue;
            }

            // Process user choice and display result
            String result = msg.sentMessage(action);
            System.out.println(result);

            switch (action) {
                case 1 -> {
                    // Message sent — show full details and increment counter
                    sentThisSession++;
                    System.out.println("\n--- Message Details ---");
                    System.out.println("Message ID  : " + msg.getMessageID());
                    System.out.println("Message Hash: " + msg.getMessageHash());
                    System.out.println("Recipient   : " + msg.getRecipient());
                    System.out.println("Message     : " + msg.getMessage());
                }
                case 2 -> {
                    // Disregard — ask user to confirm deletion
                    System.out.print("Press 0 to confirm deletion: ");
                    String del = scanner.nextLine().trim();
                    if (del.equals("0")) {
                        System.out.println("Message deleted.");
                    }
                    // Does not count toward message limit
                }
                case 3 -> // Stored — counts toward the message limit
                    sentThisSession++;
                default -> {
                }
            }
        }

        // Display total and all sent messages after session ends
        System.out.println("\n========================================");
        System.out.println("Total messages sent: " + Message.getTotalSent());
        System.out.println("\n--- All Sent Messages ---");
        Message temp = new Message("", " ");
        System.out.println(temp.printMessages());
    }

    // ── Part 3: Stored Messages Menu ─────────────────────────────────────
    // Provides sub-menu to interact with stored messages arrays
    private static void storedMessagesMenu(Scanner scanner) {

        boolean back = false;
        while (!back) {
            System.out.println("\n--- Stored Messages Menu ---");
            System.out.println("a) Display sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message by ID");
            System.out.println("d) Search messages by recipient");
            System.out.println("e) Delete a message using message hash");
            System.out.println("f) Display full message report");
            System.out.println("x) Back to main menu");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "a" -> // Display all stored message recipients
                    System.out.println(Message.displayStoredSenderRecipient());
                case "b" -> // Find and display the longest message
                    System.out.println(Message.displayLongestMessage());
                case "c" -> {
                    // Search by message ID or recipient number
                    System.out.print("Enter Message ID to search: ");
                    String id = scanner.nextLine().trim();
                    System.out.println(Message.searchByMessageID(id));
                }
                case "d" -> {
                    // Search all messages for a specific recipient
                    System.out.print("Enter recipient number to search: ");
                    String rec = scanner.nextLine().trim();
                    System.out.println(Message.searchByRecipient(rec));
                }
                case "e" -> {
                    // Delete a message using its hash
                    System.out.print("Enter message hash to delete: ");
                    String hash = scanner.nextLine().trim();
                    System.out.println(Message.deleteMessageByHash(hash));
                }
                case "f" -> // Display full report of all messages
                    System.out.println(Message.displayReport());
                case "x" -> // Return to main menu
                    back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }
}