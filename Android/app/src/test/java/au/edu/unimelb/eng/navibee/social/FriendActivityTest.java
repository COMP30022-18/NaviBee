package au.edu.unimelb.eng.navibee.social;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FriendActivityTest {

    @Mock
    private Conversation.Message mMessage;
    @Mock
    private Conversation mConversation;
    @Mock
    private FriendActivity mFriendActivity;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testContactItem() {

        Date time = new Date();

        // mock message class
        mMessage = mock(Conversation.Message.class);
        when(mMessage.getType()).thenReturn("text");
        when(mMessage.getTime_()).thenReturn(time);

        assertEquals(mMessage.getData(), mMessage.getData());

        // mock conversation class
        mConversation = mock(Conversation.class);
        when(mConversation.getUnreadMsgCount()).thenReturn(8);
        when(mConversation.getMessage(anyInt())).thenReturn(mMessage);
        when(mConversation.getMessageCount()).thenReturn(2);

        FriendActivity.ContactItem contactItem = new FriendActivity.ContactItem(mConversation);

        // check values
        assertEquals(mConversation, contactItem.getConv());
        assertEquals(8, contactItem.getUnreadMessage());
        assertEquals(mMessage.getSummary(), contactItem.getLastMessage());
        assertEquals(true, contactItem.hasMessage());
        assertEquals(DateManager.DateformatTime(time), contactItem.getLastMessageTime());
        assertEquals(time, contactItem.getTimeForSort());

    }

    @Test
    public void testFriendAdapter() {

        mFriendActivity = mock(FriendActivity.class);

    }

    @After
    public void tearDown() throws Exception {
    }

}