package com.mycompany.quickchat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * Message class for QuickChat application.
 * Handles all message operations including creation,
 * validation, sending, storing and hashing.
 * @author khoza
 */
public class Message {

    // ── Instance fields ──────────────────────────────────────────────────
    private String  messageID;
    private int     numMessagesSent;
    private String  recipient;
    private String  message;
    private String  messageHash;

    // ── Static shared state for the session ──────────────────────────────
    private static List<Message> sentMessages    = new ArrayList<>();
    private static List<Message> storedMessages  = new ArrayList<>();
    private static int           totalSent        = 0;

    // ── Constructor ───────────────────────────────────────────────────────
    public Message(String recipient, String message) {
        this.recipient       = recipient;
        this.message         = message;
        this.messageID       = generateMessageID();
        this.numMessagesSent = totalSent;
    }

    // ── Generate random 10-digit Message ID ───────────────────────────────
    private String generateMessageID() {
        Random rand = new Random();
        long id = (long)(rand.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
        return String.valueOf(id);
    }

    // ── Method 1: checkMessageID ──────────────────────────────────────────
    // Ensures message ID is not more than 10 characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // ── Method 2: checkRecipientCell ─────────────────────────────────────
    // Ensures recipient cell number starts with + and is max 13 characters
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

    // ── Method 3: createMessageHash ──────────────────────────────────────
    // Format: firstTwoOfID:messageNumber:FIRSTWORDLASTWORD  (all caps)
    // Example: 00:0:HITONIGHT
    public String createMessageHash() {
        String   firstTwo  = messageID.substring(0, 2);
        String[] words     = message.trim().split("\\s+");
        String   firstWord = words[0].replaceAll("[^a-zA-Z]", "");
        String   lastWord  = words[words.length - 1].replaceAll("[^a-zA-Z]", "");
        messageHash = (firstTwo + ":" + totalSent + ":"
                     + firstWord + lastWord).toUpperCase();
        return messageHash;
    }

    // ── Method 4: sentMessage ─────────────────────────────────────────────
    // 1 = Send, 2 = Disregard, 3 = Store
    public String sentMessage(int choice) {
        if (choice == 1) {
            totalSent++;
            numMessagesSent = totalSent;
            createMessageHash();
            sentMessages.add(this);
            return "Message successfully sent.";
        }
        if (choice == 2) {
            return "Press 0 to delete the message.";
        }
        if (choice == 3) {
            storedMessages.add(this);
            storeMessage();
            return "Message successfully stored.";
        }
        return "Invalid option.";
    }

    // ── Method 5: printMessages ───────────────────────────────────────────
    // Returns all sent messages: ID, Hash, Recipient, Message
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

    // ── Method 6: returnTotalMessages ────────────────────────────────────
    // Returns total number of messages sent
    public int returnTotalMessages() {
        return totalSent;
    }

    // ── Method 7: validateMessage ────────────────────────────────────────
    // Message must not exceed 250 characters
    public String validateMessage() {
        if (message.length() <= 250) {
            return "Message ready to send.";
        }
        int over = message.length() - 250;
        return "Message exceeds 250 characters by " + over
             + "; please reduce the size.";
    }

    // ── Method 8: storeMessage ───────────────────────────────────────────
    // Saves stored messages to a JSON file using json-simple library
    @SuppressWarnings("unchecked")
    public void storeMessage() {
        String filename = "stored_messages.json";
        try {
            // Build JSON object for this message
            JSONObject obj = new JSONObject();
            obj.put("messageID",       messageID);
            obj.put("numMessagesSent", numMessagesSent);
            obj.put("recipient",       recipient);
            obj.put("message",         message);
            obj.put("messageHash",     messageHash != null ? messageHash : "");

            // Read existing array if file exists, otherwise start fresh
            JSONArray array = new JSONArray();
            java.io.File file = new java.io.File(filename);
            if (file.exists()) {
                try {
                    org.json.simple.parser.JSONParser parser =
                        new org.json.simple.parser.JSONParser();
                    Object parsed = parser.parse(new java.io.FileReader(file));
                    if (parsed instanceof JSONArray jSONArray) {
                        array = jSONArray;
                    }
                } catch (IOException | ParseException e) {
                    // File was empty or malformed — start fresh
                    array = new JSONArray();
                }
            }

            // Add new message and write back to file
            array.add(obj);
            try (FileWriter fw = new FileWriter(filename)) {
                fw.write(array.toJSONString());
                fw.flush();
            }
            System.out.println("Message successfully stored to "
                             + filename);

        } catch (IOException e) {
            System.out.println("Error saving message: " + e.getMessage());
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────
    public String getMessageID()   { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient()   { return recipient; }
    public String getMessage()     { return message; }
    public int    getNumSent()     { return numMessagesSent; }

    public static List<Message> getSentMessages()   { return sentMessages; }
    public static List<Message> getStoredMessages() { return storedMessages; }
    public static int           getTotalSent()      { return totalSent; }

    // ── resetAll — used in unit tests only ───────────────────────────────
    public static void resetAll() {
        sentMessages.clear();
        storedMessages.clear();
        totalSent = 0;
    }
}