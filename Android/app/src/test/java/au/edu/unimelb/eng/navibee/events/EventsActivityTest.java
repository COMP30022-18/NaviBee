package au.edu.unimelb.eng.navibee.events;

import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleConsumer;

import au.edu.unimelb.eng.navibee.event.EventsActivity;

import au.edu.unimelb.eng.navibee.event.EventsActivity;

import static org.junit.Assert.*;

public class EventsActivityTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEventItem() {

        final double unitTestDelta = 0.001;

        String name = "name";
        String holder = "holder";
        String placeName = "placeName";

        double longitude = 0.0;
        double latitude = 0.0;

        Boolean isPrivate = false;

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

        EventsActivity.EventItem eventItem = new EventsActivity.EventItem(name, holder, time_, users, images, placeName, longitude, latitude, isPrivate);
        EventsActivity.EventItem eventItem_ = new EventsActivity.EventItem(name, holder, time_, users, images, placeName, longitude, latitude, isPrivate);

        // check values
        assertEquals(name, eventItem.getName());
        assertEquals(holder, eventItem.getHolder());
        assertEquals(isPrivate, eventItem.getIsPrivate());
        assertEquals(users, eventItem.getUsers());
        assertEquals(time, eventItem.getTime());
        assertEquals(time_, eventItem.getTime_());
        assertEquals(images, eventItem.getImages());
        assertEquals(longitude, eventItem.getLongitude(), unitTestDelta);
        assertEquals(latitude, eventItem.getLatitude(), unitTestDelta);
        assertEquals(placeName, eventItem.getPlaceName());

        assertEquals(name, eventItem_.getName());
        assertEquals(holder, eventItem_.getHolder());
        assertEquals(isPrivate, eventItem_.getIsPrivate());
        assertEquals(users, eventItem_.getUsers());
        assertEquals(time, eventItem_.getTime());
        assertEquals(time_, eventItem_.getTime_());
        assertEquals(images, eventItem_.getImages());
        assertEquals(longitude, eventItem_.getLongitude(), unitTestDelta);
        assertEquals(latitude, eventItem_.getLatitude(), unitTestDelta);
        assertEquals(placeName, eventItem_.getPlaceName());
    }

    @After
    public void tearDown() throws Exception {
    }

}