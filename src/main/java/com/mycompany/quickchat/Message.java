package com.mycompany.quickchat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// Message class for the QuickChat application.
// Handles message creation, validation, sending, storing,
// hashing, searching and report display.
// Part 1 and 2: Core messaging functionality
// Part 3: Arrays, JSON reading, search, delete and report features
// @author khoza
public class Message {

    // Instance fields
    private String messageID;       // Auto-generated 10-digit ID
    private int    numMessagesSent; // Message sequence number
    private String recipient;       // Recipient cell number
    private String message;         // Message body, max 250 chars
    private String messageHash;     // Auto-generated hash

    // Part 3 arrays — store all messages during the session
    private static List<Message> sentMessages        = new ArrayList<>();
    private static List<Message> disregardedMessages = new ArrayList<>();
    private static List<Message> storedMessages      = new ArrayList<>();
    private static List<String>  messageHashes       = new ArrayList<>();
    private static List<String>  messageIDs          = new ArrayList<>();
    private static int           totalSent           = 0;

    // Constructor — creates a new Message with recipient and message text
    public Message(String recipient, String message) {
        this.recipient       = recipient;
        this.message         = message;
        this.messageID       = generateMessageID();
        this.numMessagesSent = totalSent;
    }

    // Generates a random 10-digit number to use as the message ID
    private String generateMessageID() {
        Random rand = new Random();
        long id = (long)(rand.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
        return String.valueOf(id);
    }

    // Method 1: checkMessageID
    // Checks that the message ID is no more than 10 characters
    // Returns true if valid, false if not
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Method 2: checkRecipientCell
    // Checks that the recipient number starts with + and is max 13 chars
    // Returns a success or failure message
    public String checkRecipientCell() {
        if (recipient != null
                && recipient.startsWith("+")
                && recipient.length() <= 13) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not "
             + "contain an international code. Please correct the number "
             + "and try again.";
    }

    // Method 3: createMessageHash
    // Builds the message hash using the format:
    // firstTwoDigitsOfID:messageNumber:FIRSTWORDLASTWORD  (all caps)
    // Example: 30:1:HITONIGHT
    public String createMessageHash() {
        String   firstTwo  = messageID.substring(0, 2);
        String[] words     = message.trim().split("\\s+");
        String   firstWord = words[0].replaceAll("[^a-zA-Z]", "");
        String   lastWord  = words[words.length - 1].replaceAll("[^a-zA-Z]", "");
        messageHash = (firstTwo + ":" + totalSent + ":"
                     + firstWord + lastWord).toUpperCase();
        return messageHash;
    }

    // Method 4: sentMessage
    // Handles what happens when the user chooses to send, disregard or store
    // 1 = Send   → adds to sentMessages array
    // 2 = Disregard → adds to disregardedMessages array
    // 3 = Store  → adds to storedMessages array and saves to JSON file
    public String sentMessage(int choice) {
        if (choice == 1) {
            totalSent++;
            numMessagesSent = totalSent;
            createMessageHash();
            sentMessages.add(this);
            messageHashes.add(this.messageHash);
            messageIDs.add(this.messageID);
            return "Message successfully sent.";
        }
        if (choice == 2) {
            disregardedMessages.add(this);
            return "Press 0 to delete the message.";
        }
        if (choice == 3) {
            createMessageHash();
            storedMessages.add(this);
            messageHashes.add(this.messageHash);
            messageIDs.add(this.messageID);
            storeMessage();
            return "Message successfully stored.";
        }
        return "Invalid option.";
    }

    // Method 5: printMessages
    // Returns a formatted string of all sent messages
    // Order: Message ID, Message Hash, Recipient, Message
    public String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : sentMessages) {
            sb.append("Message ID  : ").append(m.messageID).append("\n");
            sb.append("Message Hash: ").append(m.messageHash).append("\n");
            sb.append("Recipient   : ").append(m.recipient).append("\n");
            sb.append("Message     : ").append(m.message).append("\n");
            sb.append("----------------------------------------").append("\n");
        }
        return sb.toString();
    }

    // Method 6: returnTotalMessages
    // Returns the total number of messages sent this session
    public int returnTotalMessages() {
        return totalSent;
    }

    // Method 7: validateMessage
    // Checks that the message does not exceed 250 characters
    // Returns success or failure with the exact number of characters over
    public String validateMessage() {
        if (message.length() <= 250) {
            return "Message ready to send.";
        }
        int over = message.length() - 250;
        return "Message exceeds 250 characters by " + over
             + "; please reduce the size.";
    }

    // Method 8: storeMessage
    // Saves the message to stored_messages.json using the json-simple library
    // Reads the existing file first, adds the new message, then rewrites the file
    // JSON library reference: https://code.google.com/archive/p/json-simple/
    @SuppressWarnings("unchecked")
    public void storeMessage() {
        String filename = "stored_messages.json";
        try {
            // Build a JSON object for this message
            JSONObject obj = new JSONObject();
            obj.put("messageID",       messageID);
            obj.put("numMessagesSent", numMessagesSent);
            obj.put("recipient",       recipient);
            obj.put("message",         message);
            obj.put("messageHash",     messageHash != null ? messageHash : "");

            // Load existing messages from file if it already exists
            JSONArray array = new JSONArray();
            java.io.File file = new java.io.File(filename);
            if (file.exists()) {
                try {
                    JSONParser parser = new JSONParser();
                    Object parsed = parser.parse(new FileReader(file));
                    if (parsed instanceof JSONArray jSONArray) {
                        array = jSONArray;
                    }
                } catch (IOException | ParseException e) {
                    array = new JSONArray();
                }
            }

            // Add new message and write the updated array back to file
            array.add(obj);
            try (FileWriter fw = new FileWriter(filename)) {
                fw.write(array.toJSONString());
                fw.flush();
            }
            System.out.println("Message successfully stored to " + filename);

        } catch (IOException e) {
            System.out.println("Error saving message: " + e.getMessage());
        }
    }

    // Part 3 Method a-support: readStoredMessagesFromJSON
    // Reads stored_messages.json and loads all messages into the storedMessages array
    // Called at startup so previously stored messages are available immediately
    @SuppressWarnings("unchecked")
    public static String readStoredMessagesFromJSON() {
        String filename = "stored_messages.json";
        try {
            java.io.File file = new java.io.File(filename);
            if (!file.exists()) {
                return "No stored messages found.";
            }
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(new FileReader(file));

            // Clear and reload the stored messages array from the file
            storedMessages.clear();
            for (Object obj : array) {
                JSONObject jsonObj = (JSONObject) obj;
                String r    = (String) jsonObj.get("recipient");
                String m    = (String) jsonObj.get("message");
                String id   = (String) jsonObj.get("messageID");
                String hash = (String) jsonObj.get("messageHash");

                // Rebuild each Message object from the stored JSON data
                Message msg = new Message(r, m);
                msg.messageID   = id;
                msg.messageHash = hash;
                storedMessages.add(msg);
            }
            return "Stored messages loaded: " + storedMessages.size();
        } catch (IOException | ParseException e) {
            return "Error reading stored messages: " + e.getMessage();
        }
    }

    // Part 3 Method a: displayStoredSenderRecipient
    // Shows the recipient and message text of all stored messages
    public static String displayStoredSenderRecipient() {
        if (storedMessages.isEmpty()) {
            return "No stored messages available.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("--- Stored Messages: Recipient and Message ---\n");
        for (Message m : storedMessages) {
            sb.append("Recipient : ").append(m.recipient).append("\n");
            sb.append("Message   : ").append(m.message).append("\n");
            sb.append("----------------------------------------\n");
        }
        return sb.toString();
    }

    // Part 3 Method b: displayLongestMessage
    // Finds and returns the longest message from sent and stored arrays combined
    public static String displayLongestMessage() {
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(sentMessages);
        allMessages.addAll(storedMessages);

        if (allMessages.isEmpty()) {
            return "No messages available.";
        }

        Message longest = allMessages.get(0);
        for (Message m : allMessages) {
            if (m.message.length() > longest.message.length()) {
                longest = m;
            }
        }
        return "Longest message: \"" + longest.message + "\"";
    }

    // Part 3 Method c: searchByMessageID
    // Searches sent and stored arrays by recipient number or message ID
    // Returns the matching recipient and message if found
    public static String searchByMessageID(String searchTerm) {
        for (Message m : sentMessages) {
            if (m.recipient.equals(searchTerm) || m.messageID.equals(searchTerm)) {
                return "Recipient : " + m.recipient + "\nMessage   : " + m.message;
            }
        }
        for (Message m : storedMessages) {
            if (m.recipient.equals(searchTerm) || m.messageID.equals(searchTerm)) {
                return "Recipient : " + m.recipient + "\nMessage   : " + m.message;
            }
        }
        return "Message not found.";
    }

    // Part 3 Method d: searchByRecipient
    // Returns all messages sent or stored for a given recipient number
    public static String searchByRecipient(String searchRecipient) {
        StringBuilder sb = new StringBuilder();
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(sentMessages);
        allMessages.addAll(storedMessages);

        for (Message m : allMessages) {
            if (m.recipient.equals(searchRecipient)) {
                sb.append("Message: ").append(m.message).append("\n");
            }
        }
        if (sb.length() == 0) {
            return "No messages found for recipient: " + searchRecipient;
        }
        return sb.toString().trim();
    }

    // Part 3 Method e: deleteMessageByHash
    // Finds and removes a message from stored or sent array using its hash
    // Returns a confirmation or not-found message
    public static String deleteMessageByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            if (hash.equalsIgnoreCase(storedMessages.get(i).messageHash)) {
                String deleted = storedMessages.get(i).message;
                storedMessages.remove(i);
                messageHashes.remove(hash);
                return "Message: \"" + deleted + "\" successfully deleted.";
            }
        }
        for (int i = 0; i < sentMessages.size(); i++) {
            if (hash.equalsIgnoreCase(sentMessages.get(i).messageHash)) {
                String deleted = sentMessages.get(i).message;
                sentMessages.remove(i);
                messageHashes.remove(hash);
                return "Message: \"" + deleted + "\" successfully deleted.";
            }
        }
        return "Hash not found. No message deleted.";
    }

    // Part 3 Method f: displayReport
    // Builds and returns a full report of all sent and stored messages
    // Shows Message Hash, Recipient and Message for each entry
    public static String displayReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== MESSAGE REPORT ==========\n");

        sb.append("--- Sent Messages ---\n");
        if (sentMessages.isEmpty()) {
            sb.append("No sent messages.\n");
        } else {
            for (Message m : sentMessages) {
                sb.append("Message Hash: ").append(m.messageHash).append("\n");
                sb.append("Recipient   : ").append(m.recipient).append("\n");
                sb.append("Message     : ").append(m.message).append("\n");
                sb.append("----------------------------------------\n");
            }
        }

        sb.append("--- Stored Messages ---\n");
        if (storedMessages.isEmpty()) {
            sb.append("No stored messages.\n");
        } else {
            for (Message m : storedMessages) {
                sb.append("Message Hash: ").append(m.messageHash).append("\n");
                sb.append("Recipient   : ").append(m.recipient).append("\n");
                sb.append("Message     : ").append(m.message).append("\n");
                sb.append("----------------------------------------\n");
            }
        }
        sb.append("====================================\n");
        return sb.toString();
    }

    // Getters
    public String getMessageID()   { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient()   { return recipient; }
    public String getMessage()     { return message; }
    public int    getNumSent()     { return numMessagesSent; }

    public static List<Message> getSentMessages()        { return sentMessages; }
    public static List<Message> getDisregardedMessages() { return disregardedMessages; }
    public static List<Message> getStoredMessages()      { return storedMessages; }
    public static List<String>  getMessageHashes()       { return messageHashes; }
    public static List<String>  getMessageIDs()          { return messageIDs; }
    public static int           getTotalSent()           { return totalSent; }

    // Resets all arrays and counters — called before each unit test
    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        totalSent = 0;
    }
}