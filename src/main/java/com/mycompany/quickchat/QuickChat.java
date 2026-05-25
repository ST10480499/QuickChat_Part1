package com.mycompany.quickchat;

import java.util.Scanner;

public class QuickChat {

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your first name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter your last name: ");
            String lastName = scanner.nextLine();

            Login login = new Login(firstName, lastName);

            System.out.println("\n=== Registration ===");

            System.out.print("Enter username (must contain '_' and be 5 or fewer characters): ");
            String username = scanner.nextLine();
            System.out.println(login.validateUsername(username));

            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            System.out.println(login.validatePassword(password));

            System.out.print("Enter cell phone number (e.g. +27838968976): ");
            String cell = scanner.nextLine();
            System.out.println(login.validateCellPhone(cell));

            System.out.println(login.registerUser(username, password, cell));

            System.out.println("\n=== Login ===");

            System.out.print("Enter username: ");
            String lu = scanner.nextLine();

            System.out.print("Enter password: ");
            String lp = scanner.nextLine();

            System.out.println(login.returnLoginStatus(lu, lp));

            if (!login.loginUser(lu, lp)) {
                System.out.println("Login failed. Exiting application.");
                return;
            }

            System.out.println("\nWelcome to QuickChat.");

            System.out.print("How many messages do you wish to send? ");
            int maxMessages = 0;
            try {
                maxMessages = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number entered. Exiting.");
                return;
            }

            boolean running = true;
            while (running) {
                System.out.println("\n--- Menu ---");
                System.out.println("1) Send Messages");
                System.out.println("2) Show recently sent messages");
                System.out.println("3) Quit");
                System.out.print("Choose an option: ");
                String menuChoice = scanner.nextLine().trim();

                switch (menuChoice) {
                    case "1" -> sendMessages(scanner, maxMessages);
                    case "2" -> System.out.println("Coming Soon.");
                    case "3" -> {
                        running = false;
                        System.out.println("Goodbye! Thank you for using QuickChat.");
                    }
                    default -> System.out.println("Invalid option. Please enter 1, 2, or 3.");
                }
            }
        }
    }

    private static void sendMessages(Scanner scanner, int maxMessages) {

        int sentThisSession = 0;

        while (sentThisSession < maxMessages) {

            System.out.println("\n--- Message " + (sentThisSession + 1) + " of " + maxMessages + " ---");

            System.out.print("Enter recipient cell number (with international code e.g. +27...): ");
            String recipient = scanner.nextLine().trim();

            Message msg = new Message(recipient, "placeholder");
            String recipientCheck = msg.checkRecipientCell();
            System.out.println(recipientCheck);

            if (!recipientCheck.equals("Cell phone number successfully captured.")) {
                System.out.println("Message cancelled. Returning to menu.");
                return;
            }

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

            System.out.println("Message Hash: " + msg.createMessageHash());

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

            String result = msg.sentMessage(action);
            System.out.println(result);

            switch (action) {
                case 1 -> {
                    sentThisSession++;
                    System.out.println("\n--- Message Details ---");
                    System.out.println("Message ID  : " + msg.getMessageID());
                    System.out.println("Message Hash: " + msg.getMessageHash());
                    System.out.println("Recipient   : " + msg.getRecipient());
                    System.out.println("Message     : " + msg.getMessage());
                }
                case 2 -> {
                    System.out.print("Press 0 to confirm deletion: ");
                    String del = scanner.nextLine().trim();
                    if (del.equals("0")) {
                        System.out.println("Message deleted.");
                    }
                }
                case 3 -> sentThisSession++;
                default -> {
                }
            }
        }

        System.out.println("\n========================================");
        System.out.println("Total messages sent: " + Message.getTotalSent());
        System.out.println("\n--- All Sent Messages ---");
        Message temp = new Message("", " ");
        System.out.println(temp.printMessages());
    }
}
 


