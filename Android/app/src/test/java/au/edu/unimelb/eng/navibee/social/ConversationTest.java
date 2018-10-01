package au.edu.unimelb.eng.navibee.social;

import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class ConversationTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testMessage() {

        String data = "data";
        String sender = "sender";
        Date time_ = new Date();
        Timestamp time = new Timestamp(time_);
        String type = "type";

        Conversation.Message message = new Conversation.Message(data, sender, time, type);
        Conversation.Message message_ = new Conversation.Message(data, sender, time_, type);

        assertNotNull(message);
        assertNotNull(message_);

        // Assert getter functions
        assertEquals(data, message.getData());
        assertEquals(sender, message.getSender());
        assertEquals(time, message.getTime());
        assertEquals(time_, message.getTime_());
        assertEquals(type, message.getType());

        assertEquals(data, message_.getData());
        assertEquals(sender, message_.getSender());
        assertEquals(time, message_.getTime());
        assertEquals(time_, message_.getTime_());
        assertEquals(type, message_.getType());

    }

    @Test
    public void testGetSummary() {

        String data = "data";
        String sender = "sender";
        Date time = new Date();
        String type;

        Conversation.Message message;

        // 4 cases need to be tested
        type = "text";
        message = new Conversation.Message(data, sender, time, type);
        assertEquals(data, message.getSummary());

        type = "image";
        message = new Conversation.Message(data, sender, time, type);
        assertEquals("[Photo]", message.getSummary());

        type = "voicecall";
        message = new Conversation.Message(data, sender, time, type);
        assertEquals("[Voice Call]", message.getSummary());

        type = "location";
        message = new Conversation.Message(data, sender, time, type);
        assertEquals("[Location]", message.getSummary());

    }

    @After
    public void tearDown() throws Exception {
    }

}