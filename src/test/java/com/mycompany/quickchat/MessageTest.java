package com.mycompany.quickchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Message class.
 * Test data from PROG5121 POE Part 2:
 *   Message 1: +27718693002 / "Hi Mike, can you join us for dinner tonight?" / Send
 *   Message 2: 08575975889  / "Hi Keegan, did you receive the payment?"      / Discard
 * @author khoza
 */
public class MessageTest {

    @BeforeEach
    void setUp() {
        Message.resetAll();
    }

    // ── Test 1: Message length — Success ─────────────────────────────────
    @Test
    public void testMessageLength_Success() {
        Message msg = new Message(
            "+27718693002",
            "Hi Mike, can you join us for dinner tonight?"
        );
        assertEquals(
            "Message ready to send.",
            msg.validateMessage()
        );
    }

    // ── Test 2: Message length — Failure ─────────────────────────────────
    @Test
    public void testMessageLength_Failure() {
        String longMsg = "A".repeat(260);
        Message msg = new Message("+27718693002", longMsg);
        assertEquals(
            "Message exceeds 250 characters by 10; please reduce the size.",
            msg.validateMessage()
        );
    }

    // ── Test 3: Recipient cell — Success ─────────────────────────────────
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

    // ── Test 4: Recipient cell — Failure ─────────────────────────────────
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

    // ── Test 5: Message hash — Test Case 1 ───────────────────────────────
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
        assertEquals(
            hash.toUpperCase(),
            hash,
            "Hash must be all uppercase"
        );
    }

    // ── Test 6: Message hash — Loop ───────────────────────────────────────
    @Test
    public void testMessageHash_Loop() {
        String[] recipients = {
            "+27718693002",
            "+27811234567",
            "+27835556677"
        };
        String[] messages = {
            "Hi Mike, can you join us for dinner tonight?",
            "Please send the documents by end of day",
            "Meeting confirmed for tomorrow at nine"
        };
        for (int i = 0; i < recipients.length; i++) {
            Message msg = new Message(recipients[i], messages[i]);
            msg.sentMessage(1);
            assertNotNull(
                msg.getMessageHash(),
                "Hash should not be null for message " + (i + 1)
            );
            assertFalse(
                msg.getMessageHash().isEmpty(),
                "Hash should not be empty for message " + (i + 1)
            );
        }
        assertEquals(3, Message.getTotalSent());
    }

    // ── Test 7: Message ID generated and valid ────────────────────────────
    @Test
    public void testMessageID_GeneratedAndValid() {
        Message msg = new Message("+27718693002", "Test message here");
        assertTrue(
            msg.checkMessageID(),
            "Message ID should be 10 characters or less"
        );
        assertNotNull(msg.getMessageID());
        System.out.println("Message ID generated: " + msg.getMessageID());
    }

    // ── Test 8: Send message ──────────────────────────────────────────────
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

    // ── Test 9: Discard message ───────────────────────────────────────────
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

    // ── Test 10: Store message ────────────────────────────────────────────
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

    // ── Test 11: Total messages count ─────────────────────────────────────
    @Test
    public void testReturnTotalMessages() {
        Message m1 = new Message(
            "+27718693002",
            "Hi Mike, can you join us for dinner tonight?"
        );
        Message m2 = new Message(
            "08575975889",
            "Hi Keegan, did you receive the payment?"
        );
        m1.sentMessage(1);
        m2.sentMessage(2);
        assertEquals(1, m1.returnTotalMessages());
    }
}