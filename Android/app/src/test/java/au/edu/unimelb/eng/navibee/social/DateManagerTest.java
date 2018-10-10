package au.edu.unimelb.eng.navibee.social;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateManagerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testDateFormatTime() throws Exception {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        Date justNow = new DateTime().minusMillis(10).toDate();
        assertEquals("just now", DateManager.DateformatTime(justNow));

        Date minsAgo = new DateTime().minusMinutes(20).toDate();
        assertEquals("20 mins ago", DateManager.DateformatTime(minsAgo));

        Date hoursAgo = new DateTime().minusHours(5).toDate();
        assertEquals(simpleDateFormat.format(hoursAgo), DateManager.DateformatTime(hoursAgo));

    }

    @After
    public void tearDown() throws Exception {
    }

}