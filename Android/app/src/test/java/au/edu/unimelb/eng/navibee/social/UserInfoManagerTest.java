package au.edu.unimelb.eng.navibee.social;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({FirebaseAuth.class, FirebaseFirestore.class})
public class UserInfoManagerTest {

    @Mock
    private FirebaseAuth mFirebaseAuth;
    @Mock
    private FirebaseFirestore mFirebaseFirestore;
    @Mock
    private FirebaseUser mFirebaseUser;

    @Before
    public void setUp() {

        // create static mock
        mFirebaseAuth = mock(FirebaseAuth.class);
        PowerMockito.mockStatic(FirebaseAuth.class);

        mFirebaseFirestore = mock(FirebaseFirestore.class);
        PowerMockito.mockStatic(FirebaseFirestore.class);

        // configure mock expected interaction
        when(FirebaseAuth.getInstance()).thenReturn(mFirebaseAuth);
        when(FirebaseFirestore.getInstance()).thenReturn(mFirebaseFirestore);

    }

    @Test
    public void testGetInstance() {

        String uid = "user";

        mFirebaseUser = mock(FirebaseUser.class);
        when(mFirebaseAuth.getCurrentUser()).thenReturn(mFirebaseUser);
        when(mFirebaseUser.getUid()).thenReturn(uid);

        assertNotNull(UserInfoManager.getInstance());

    }

    @Test
    public void testUserInfo() {

        String name = "name";
        String photoUrl = "photo url";

        UserInfoManager.UserInfo userInfo = new UserInfoManager.UserInfo(name, photoUrl);

        assertEquals(name, userInfo.getName());
        assertEquals(photoUrl, userInfo.getPhotoUrl());

        name = "new name";
        photoUrl = "new photo url";
        userInfo.setName(name);
        userInfo.setPhotoUrl(photoUrl);

        assertEquals(name, userInfo.getName());
        assertEquals(photoUrl, userInfo.getPhotoUrl());

    }

    @After
    public void tearDown() {
    }
}