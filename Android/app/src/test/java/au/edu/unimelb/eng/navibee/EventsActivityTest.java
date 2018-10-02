package au.edu.unimelb.eng.navibee;

import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class EventsActivityTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEventItem() {

        String name = "name";
        String holder = "holder";
        String location = "location";

        Map<String, Boolean> users = new HashMap<>();
        users.put("user1", true);
        users.put("user2", true);
        assertNotNull(users);

        Date time_ = new Date();
        Timestamp time = new Timestamp(time_);

        ArrayList<String> images = new ArrayList<>();
        images.add("image1");
        images.add("image2");
        assertNotNull(images);

        EventsActivity.EventItem eventItem = new EventsActivity.EventItem(name, holder, location, time, users, images);
        EventsActivity.EventItem eventItem_ = new EventsActivity.EventItem(name, holder, location, time_, users, images);

        // check values
        assertEquals(name, eventItem.getName());
        assertEquals(holder, eventItem.getHolder());
        assertEquals(location, eventItem.getLocation());
        assertEquals(users, eventItem.getUsers());
        assertEquals(time, eventItem.getTime());
        assertEquals(time_, eventItem.getTime_());
        assertEquals(images, eventItem.getImages());

        assertEquals(name, eventItem_.getName());
        assertEquals(holder, eventItem_.getHolder());
        assertEquals(location, eventItem_.getLocation());
        assertEquals(users, eventItem_.getUsers());
        assertEquals(time, eventItem_.getTime());
        assertEquals(time_, eventItem_.getTime_());
        assertEquals(images, eventItem_.getImages());

        assertTrue(!eventItem.isTag());
        eventItem.setTag(true);
        assertTrue(eventItem.isTag());

        assertNull(eventItem.getEventId());
        String eventId = "event id";
        eventItem.setEventId(eventId);
        assertNotNull(eventItem.getEventId());
        assertEquals(eventId, eventItem.getEventId());

    }

    @After
    public void tearDown() throws Exception {
    }

}