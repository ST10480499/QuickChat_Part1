package com.mycompany.quickchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// Unit tests for the Message class
// Part 2 test data:
//   Message 1: +27718693002 / "Hi Mike, can you join us for dinner tonight?" / Send
//   Message 2: 08575975889  / "Hi Keegan, did you receive the payment?"      / Discard
//
// Part 3 test data from POE spec:
//   Message 1: +27834557896 / "Did you get the cake?"                                        / Sent
//   Message 2: +27838884567 / "Where are you? You are late! I have asked you to be on time." / Stored
//   Message 3: +27834484567 / "Yohoooo, I am at your gate."                                  / Disregard
//   Message 4: 0838884567   / "It is dinner time !"                                          / Sent
//   Message 5: +27838884567 / "Ok, I am leaving without you."                                / Stored
// @author khoza
public class MessageTest {

    // Runs before each test — resets arrays so tests don't affect each other
    @BeforeEach
    void setUp() {
        Message.resetAll();
    }

    // Loads Part 3 POE test data into the arrays
    // Called at the start of each Part 3 test that needs pre-loaded data
    private void populatePart3TestData() {
        Message m1 = new Message("+27834557896", "Did you get the cake?");
        m1.sentMessage(1); // Sent

        Message m2 = new Message("+27838884567",
                "Where are you? You are late! I have asked you to be on time.");
        m2.sentMessage(3); // Stored

        Message m3 = new Message("+27834484567", "Yohoooo, I am at your gate.");
        m3.sentMessage(2); // Disregarded

        Message m4 = new Message("0838884567", "It is dinner time !");
        m4.sentMessage(1); // Sent

        Message m5 = new Message("+27838884567", "Ok, I am leaving without you.");
        m5.sentMessage(3); // Stored
    }

    // ── PART 2 TESTS ─────────────────────────────────────────────────────

    // Message under 250 characters should pass validation
    @Test
    public void testMessageLength_Success() {
        Message msg = new Message(
            "+27718693002",
            "Hi Mike, can you join us for dinner tonight?"
        );
        assertEquals("Message ready to send.", msg.validateMessage());
    }

    // Message over 250 characters should fail — 260 chars = 10 over limit
    @Test
    public void testMessageLength_Failure() {
        String longMsg = "A".repeat(260);
        Message msg = new Message("+27718693002", longMsg);
        assertEquals(
            "Message exceeds 250 characters by 10; please reduce the size.",
            msg.validateMessage()
        );
    }

    // Cell number with + and valid length should be accepted
    @Test
    public void testRecipientCell_Success() {
        Message msg = new Message(
            "+27718693002",
            "Hi Mike, can you join us for dinner tonight?"
        );
        assertEquals(
            "Cell phone number successfully captured.",
            msg.checkRecipientCell()
        );
    }

    // Cell number without + international code should be rejected
    @Test
    public void testRecipientCell_Failure() {
        Message msg = new Message(
            "08575975889",
            "Hi Keegan, did you receive the payment?"
        );
        assertEquals(
            "Cell phone number is incorrectly formatted or does not "
          + "contain an international code. Please correct the number "
          + "and try again.",
            msg.checkRecipientCell()
        );
    }

    // Hash for "Hi Mike...tonight?" must end with :HITONIGHT and be uppercase
    @Test
    public void testMessageHash_TestCase1() {
        Message msg = new Message(
            "+27718693002",
            "Hi Mike, can you join us for dinner tonight?"
        );
        String hash = msg.createMessageHash();
        assertTrue(
            hash.endsWith(":HITONIGHT"),
            "Hash should end with :HITONIGHT but was: " + hash
        );
        assertEquals(hash.toUpperCase(), hash, "Hash must be all uppercase");
    }

    // Hashes must be generated for all messages when sent in a loop
    @Test
    public void testMessageHash_Loop() {
        String[] recipients = {"+27718693002", "+27811234567", "+27835556677"};
        String[] messages = {
            "Hi Mike, can you join us for dinner tonight?",
            "Please send the documents by end of day",
            "Meeting confirmed for tomorrow at nine"
        };
        for (int i = 0; i < recipients.length; i++) {
            Message msg = new Message(recipients[i], messages[i]);
            msg.sentMessage(1);
            assertNotNull(msg.getMessageHash(),
                    "Hash should not be null for message " + (i + 1));
            assertFalse(msg.getMessageHash().isEmpty(),
                    "Hash should not be empty for message " + (i + 1));
        }
        assertEquals(3, Message.getTotalSent());
    }

    // Message ID must be auto-generated, non-null and 10 chars or less
    @Test
    public void testMessageID_GeneratedAndValid() {
        Message msg = new Message("+27718693002", "Test message here");
        assertTrue(msg.checkMessageID(),
                "Message ID should be 10 characters or less");
        assertNotNull(msg.getMessageID());
        System.out.println("Message ID generated: " + msg.getMessageID());
    }

    // Sending a message must return correct text and increment total
    @Test
    public void testSentMessage_Send() {
        Message msg = new Message(
            "+27718693002",
            "Hi Mike, can you join us for dinner tonight?"
        );
        String result = msg.sentMessage(1);
        assertEquals("Message successfully sent.", result);
        assertEquals(1, Message.getTotalSent());
    }

    // Discarding must return correct text and not increment total
    @Test
    public void testSentMessage_Discard() {
        Message msg = new Message(
            "08575975889",
            "Hi Keegan, did you receive the payment?"
        );
        String result = msg.sentMessage(2);
        assertEquals("Press 0 to delete the message.", result);
        assertEquals(0, Message.getTotalSent());
    }

    // Storing must return correct text and add to stored array
    @Test
    public void testSentMessage_Store() {
        Message msg = new Message(
            "+27718693002",
            "Storing this for later"
        );
        String result = msg.sentMessage(3);
        assertEquals("Message successfully stored.", result);
        List<Message> stored = Message.getStoredMessages();
        assertEquals(1, stored.size());
        assertEquals(0, Message.getTotalSent());
    }

    // After sending 1 and discarding 1, total sent must equal 1
    @Test
    public void testReturnTotalMessages() {
        Message m1 = new Message("+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        Message m2 = new Message("08575975889",
                "Hi Keegan, did you receive the payment?");
        m1.sentMessage(1);
        m2.sentMessage(2);
        assertEquals(1, m1.returnTotalMessages());
    }

    // ── PART 3 TESTS ─────────────────────────────────────────────────────

    // Sent array must contain exactly messages 1 and 4 from test data
    @Test
    public void testSentMessagesArray_CorrectlyPopulated() {
        populatePart3TestData();
        List<Message> sent = Message.getSentMessages();
        assertEquals(2, sent.size(),
                "Sent array should contain 2 messages");
        assertEquals("Did you get the cake?", sent.get(0).getMessage(),
                "First sent message should be message 1");
        assertEquals("It is dinner time !", sent.get(1).getMessage(),
                "Second sent message should be message 4");
    }

    // Disregarded array must contain exactly message 3 from test data
    @Test
    public void testDisregardedMessagesArray_CorrectlyPopulated() {
        populatePart3TestData();
        List<Message> disregarded = Message.getDisregardedMessages();
        assertEquals(1, disregarded.size(),
                "Disregarded array should contain 1 message");
        assertEquals("Yohoooo, I am at your gate.",
                disregarded.get(0).getMessage());
    }

    // Stored array must contain exactly messages 2 and 5 from test data
    @Test
    public void testStoredMessagesArray_CorrectlyPopulated() {
        populatePart3TestData();
        List<Message> stored = Message.getStoredMessages();
        assertEquals(2, stored.size(),
                "Stored array should contain 2 messages");
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                stored.get(0).getMessage());
    }

    // messageIDs array must contain 4 entries (2 sent + 2 stored)
    @Test
    public void testMessageIDsArray_CorrectlyPopulated() {
        populatePart3TestData();
        List<String> ids = Message.getMessageIDs();
        assertEquals(4, ids.size(),
                "MessageIDs array should have 4 entries");
    }

    // messageHashes array must contain 4 entries (2 sent + 2 stored)
    @Test
    public void testMessageHashesArray_CorrectlyPopulated() {
        populatePart3TestData();
        List<String> hashes = Message.getMessageHashes();
        assertEquals(4, hashes.size(),
                "MessageHashes array should have 4 entries");
    }

    // Message 2 is the longest — must be returned by displayLongestMessage
    @Test
    public void testDisplayLongestMessage() {
        populatePart3TestData();
        String result = Message.displayLongestMessage();
        assertTrue(
            result.contains("Where are you? You are late! I have asked you to be on time."),
            "Should return the longest message but got: " + result
        );
    }

    // Searching by message 4 recipient must return message 4 text
    @Test
    public void testSearchByMessageID_Found() {
        populatePart3TestData();
        String result = Message.searchByMessageID("0838884567");
        assertTrue(
            result.contains("It is dinner time !"),
            "Should find message 4 but got: " + result
        );
    }

    // Searching for a number that does not exist must return not found
    @Test
    public void testSearchByMessageID_NotFound() {
        populatePart3TestData();
        String result = Message.searchByMessageID("0000000000");
        assertEquals("Message not found.", result);
    }

    // Recipient +27838884567 has messages 2 and 5 — both must be returned
    @Test
    public void testSearchByRecipient_Found() {
        populatePart3TestData();
        String result = Message.searchByRecipient("+27838884567");
        assertTrue(
            result.contains("Where are you? You are late! I have asked you to be on time."),
            "Should find message 2 for this recipient"
        );
        assertTrue(
            result.contains("Ok, I am leaving without you."),
            "Should find message 5 for this recipient"
        );
    }

    // Searching for a recipient with no messages must return not found
    @Test
    public void testSearchByRecipient_NotFound() {
        populatePart3TestData();
        String result = Message.searchByRecipient("+27000000000");
        assertTrue(result.contains("No messages found for recipient"),
                "Should return not found message");
    }

    // Deleting a message by hash must confirm deletion and shrink the array
    @Test
    public void testDeleteMessageByHash_Success() {
        populatePart3TestData();
        List<Message> stored = Message.getStoredMessages();
        int sizeBefore = stored.size();
        String hashToDelete = stored.get(0).getMessageHash();
        String result = Message.deleteMessageByHash(hashToDelete);
        assertTrue(
            result.contains("successfully deleted"),
            "Should confirm deletion but got: " + result
        );
        assertEquals(sizeBefore - 1, Message.getStoredMessages().size(),
                "Stored array should have one less message after deletion");
    }

    // Deleting with a fake hash must return not found
    @Test
    public void testDeleteMessageByHash_NotFound() {
        populatePart3TestData();
        String result = Message.deleteMessageByHash("XX:99:FAKEHASH");
        assertEquals("Hash not found. No message deleted.", result);
    }

    // Report must contain header, sent section and stored section with correct data
    @Test
    public void testDisplayReport() {
        populatePart3TestData();
        String result = Message.displayReport();
        assertTrue(result.contains("MESSAGE REPORT"),
                "Report should contain MESSAGE REPORT header");
        assertTrue(result.contains("Did you get the cake?"),
                "Report should contain sent message 1");
        assertTrue(result.contains("Where are you? You are late!"),
                "Report should contain stored message 2");
        assertTrue(result.contains("Sent Messages"),
                "Report should contain Sent Messages section");
        assertTrue(result.contains("Stored Messages"),
                "Report should contain Stored Messages section");
    }
}