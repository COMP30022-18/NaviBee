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

    private DateManager dateManager = new DateManager();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testDateFormatTime() throws Exception {

        Date time = new DateTime().minusMillis(10).toDate();
//        assertEquals("just now", );


    }

    @After
    public void tearDown() throws Exception {
    }

}