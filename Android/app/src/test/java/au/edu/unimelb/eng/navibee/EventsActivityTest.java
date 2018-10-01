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

        Date dateTime = new Date();
        Timestamp time = new Timestamp(dateTime);

        ArrayList<String> images = new ArrayList<>();
        images.add("image1");
        images.add("image2");
        assertNotNull(images);

        EventsActivity.EventItem eventItem = new EventsActivity.EventItem(name, holder, location, time, users, images);

        // check values
        assertEquals(name, eventItem.getName());
        assertEquals(holder, eventItem.getHolder());
        assertEquals(location, eventItem.getLocation());
        assertEquals(users, eventItem.getUsers());
        assertEquals(dateTime, eventItem.getTime_());
        assertEquals(time, eventItem.getTime());
        assertEquals(images, eventItem.getImages());

    }

    @After
    public void tearDown() throws Exception {
    }

}