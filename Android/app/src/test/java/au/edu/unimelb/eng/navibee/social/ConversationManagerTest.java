package au.edu.unimelb.eng.navibee.social;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({ FirebaseFunctions.class})
public class ConversationManagerTest {

    private ConversationManager concM;
    FirebaseFunctions mockedFirebaseFunction;

    @Before
    public void setUp() throws Exception {
        concM = new ConversationManager();

        mockedFirebaseFunction = Mockito.mock(FirebaseFunctions.class);
        PowerMockito.mockStatic(FirebaseFunctions.class);
        when(FirebaseFunctions.getInstance()).thenReturn(mockedFirebaseFunction);



    }

    @Test
    public void testAddFriend() {

        String uid = "user1";
        Map<String, Object> data = new HashMap<>();
        data.put("targetUid", uid);


        HttpsCallableReference mockedHttpsCallableReference = Mockito.mock(HttpsCallableReference.class);
        when(mockedFirebaseFunction.getHttpsCallable("addFriend")).thenReturn(mockedHttpsCallableReference);
        when(mockedHttpsCallableReference.call(data)).thenReturn(null);

        concM.addFriend(uid);
    }

    @After
    public void tearDown() throws Exception {
    }
}