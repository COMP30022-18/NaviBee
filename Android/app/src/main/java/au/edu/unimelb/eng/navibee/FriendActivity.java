package au.edu.unimelb.eng.navibee;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;
    private FirebaseFunctions mFunctions;

    ArrayList<String> contactList = new ArrayList<String>();
    ArrayAdapter<String> contactListAdapter;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

//        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFunctions = FirebaseFunctions.getInstance();


        contactListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                contactList);

        ListView listView = (ListView) findViewById(R.id.contactListView);
        listView.setAdapter(contactListAdapter);

        loadContactList();
    }

    private void loadContactList() {
        mFunctions.getHttpsCallable("getFriendList")
                .call(new HashMap<>()).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                if (!task.isSuccessful()) {
//                    Exception e = task.getException();
//                    if (e instanceof FirebaseFunctionsException) {
//                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
//                        FirebaseFunctionsException.Code code = ffe.getCode();
//                        Object details = ffe.getDetails();
//                    }
//
                } else {
                    Map<String, Object> data = (Map<String, Object>) task.getResult().getData();
                    ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) data.get("list");

                    contactList.clear();
                    for (Map<String, String> item: list) {
                        contactList.add(item.get("name"));
                    }

                    contactListAdapter.notifyDataSetChanged();
                }
            }
        });




    }



}
