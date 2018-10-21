package au.edu.unimelb.eng.navibee.social;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({RtcEngine.class, NaviBeeApplication.class})
public class VoiceCallEngineTest {

    @Mock
    private NaviBeeApplication mNaviBeeApplication;
    @Mock
    private Context mContext;
    @Mock
    private RtcEngine mRtcEngine;

    @Before
    public void setUp() throws Exception {

        mContext = mock(Context.class);

        mNaviBeeApplication = mock(NaviBeeApplication.class);
        PowerMockito.mockStatic(NaviBeeApplication.class);

        when(NaviBeeApplication.getInstance()).thenReturn(mNaviBeeApplication);
        when(mNaviBeeApplication.getBaseContext()).thenReturn(mContext);
        when(mNaviBeeApplication.getString(anyInt())).thenReturn("testappid");

        mRtcEngine = mock(RtcEngine.class);
        PowerMockito.mockStatic(RtcEngine.class);

        when(RtcEngine.create(eq(mContext), anyString(),
                any(IRtcEngineEventHandler.class))).thenReturn(mRtcEngine);

    }

    @Test
    public void testGetInstance() {

        assertNotNull(VoiceCallEngine.getInstance());

    }

    @After
    public void tearDown() throws Exception {
    }
}