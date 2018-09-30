package au.edu.unimelb.eng.navibee.social;

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
    public void testMessage() throws Exception {
        Date testTime = new Date();

        String mData = "data";
        String mSender = "sender";
        Date mTime = testTime;
        String mType = "type";

        Conversation.Message mMessage = new Conversation.Message(mData, mSender, mTime, mType);

        // Getter function
        assertEquals("data", mMessage.getData());
        assertEquals("sender", mMessage.getSender());
        assertEquals(testTime, mMessage.getTime_());
        assertEquals("type", mMessage.getType());

        // getSummary function
        mType = "text";
        mMessage = new Conversation.Message(mData, mSender, mTime, mType);
        assertEquals(mData, mMessage.getSummary());

        mType = "image";
        mMessage = new Conversation.Message(mData, mSender, mTime, mType);
        assertEquals("[Photo]", mMessage.getSummary());

        mType = "voicecall";
        mMessage = new Conversation.Message(mData, mSender, mTime, mType);
        assertEquals("[Voice Call]", mMessage.getSummary());

        mType = "location";
        mMessage = new Conversation.Message(mData, mSender, mTime, mType);
        assertEquals("[Location]", mMessage.getSummary());

    }

    @After
    public void tearDown() throws Exception {
    }

}