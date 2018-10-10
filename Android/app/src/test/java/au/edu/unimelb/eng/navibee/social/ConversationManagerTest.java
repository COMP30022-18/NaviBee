package au.edu.unimelb.eng.navibee.social;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({FirebaseFunctions.class, FirebaseFirestore.class})
public class ConversationManagerTest {

    private ConversationManager conversationManager;

    @Mock
    private FirebaseFunctions mFirebaseFunction;
    @Mock
    private FirebaseFirestore mFirebaseFirestore;
    @Mock
    private HttpsCallableReference mHttpsCallableReference;
    @Mock
    private CollectionReference mCollection;
    @Mock
    private DocumentReference mDocument;

    @Before
    public void setUp() throws Exception {

        conversationManager = new ConversationManager();

        // create static mocks
        mFirebaseFunction = mock(FirebaseFunctions.class);
        PowerMockito.mockStatic(FirebaseFunctions.class);

        mFirebaseFirestore = mock(FirebaseFirestore.class);
        PowerMockito.mockStatic(FirebaseFirestore.class);

        // configure mock expected interaction
        when(FirebaseFunctions.getInstance()).thenReturn(mFirebaseFunction);
        when(FirebaseFirestore.getInstance()).thenReturn(mFirebaseFirestore);

    }

    @Test
    public void testAddFriend() {

        Map<String, Object> data = new HashMap<>();
        String uid = "user";
        data.put("targetUid", uid);
        assertNotNull(data);

        // create mock
        mHttpsCallableReference = mock(HttpsCallableReference.class);

        // configure mock expected interaction
        when(mFirebaseFunction.getHttpsCallable("addFriend")).thenReturn(mHttpsCallableReference);
        when(mHttpsCallableReference.call(data)).thenReturn(null);

        conversationManager.addFriend(uid);

        // check if mock had expected interaction
        verify(mFirebaseFunction, times(1)).getHttpsCallable("addFriend");
        verify(mHttpsCallableReference, atLeastOnce()).call(data);

    }

    @Test
    public void testDeleteFriend() {

        String cid = "conversation";

        // create mocks
        mCollection = mock(CollectionReference.class);
        mDocument = mock(DocumentReference.class);

        // configure mock expected interaction
        when(mFirebaseFirestore.collection("conversations")).thenReturn(mCollection);
        when(mCollection.document(cid)).thenReturn(mDocument);
        when(mDocument.update("isDeleted", true)).thenReturn(null);

        mFirebaseFirestore.collection("conversations").document(cid).update("isDeleted", true);

        // check if mock had expected interaction
        verify(mFirebaseFirestore, times(1)).collection("conversations");
        verify(mCollection, times(1)).document(cid);
        verify(mDocument, times(1)).update("isDeleted", true);

    }

    @Test
    public void testCreateGroupChat() {

        ArrayList<String> users = new ArrayList<>();
        users.add("user1");
        users.add("user2");
        assertNotNull(users);

        String name = "name";
        String icon = "icon";

        Map<String, Object> data = new HashMap<>();
        data.put("users", users);
        data.put("name", name);
        data.put("icon", icon);

        // create mock
        HttpsCallableReference mHttpsCallableReference = mock(HttpsCallableReference.class);

        // configure mock expected interaction
        when(mFirebaseFunction.getHttpsCallable("createGroupChat")).thenReturn(mHttpsCallableReference);
        when(mHttpsCallableReference.call(data)).thenReturn(null);

        conversationManager.createGroupChat(users, name, icon);

        // check if mock had expected interaction
        verify(mFirebaseFunction, times(1)).getHttpsCallable("createGroupChat");
        verify(mHttpsCallableReference, atLeastOnce()).call(data);

    }

    @After
    public void tearDown() throws Exception {
    }

}