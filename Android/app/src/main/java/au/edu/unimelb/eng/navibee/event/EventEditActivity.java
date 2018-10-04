package au.edu.unimelb.eng.navibee.event;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.UserInfoManager;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;
import au.edu.unimelb.eng.navibee.utils.URLChipDrawableCacheLoader;

public class EventEditActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, IPickResult {

    private static final int SELECT_FRIEND_REQUEST = 1;
    private static final int IMAGE_REVIEW_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;


    // TODO magic string

    private HashMap<String, UserInfoManager.UserInfo> selectedUsers;
    private Map<String, Integer> dateMap;
    private ArrayList<Bitmap> pics;
    private ArrayList<Uri> picsUri;
    private ArrayList<String> picsStoragePath = new ArrayList<>();
    private GridView picsView;
    private EditText nameView;
    private TextInputEditText timeField;
    private TextInputEditText locationField;
    private TextInputEditText participatnsField;
    private Bitmap addIcon;
    private ScrollView scrollView;
    private ProgressBar progressBar;
    private Place eventLocation;
    private final int MAX_NUM_OF_PHOTOS = 6;

    public static class TimePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), (EventEditActivity)getActivity(), hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

    }

    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), (EventEditActivity)getActivity(), year, month, day);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_edit_new);

        nameView = findViewById(R.id.eventName);
        picsView = findViewById(R.id.eventPics);
        scrollView = findViewById(R.id.eventScrollView);
        progressBar = findViewById(R.id.event_indefinite_progress);
        timeField = findViewById(R.id.event_create_time);
        locationField = findViewById(R.id.event_create_location);
        participatnsField = findViewById(R.id.event_create_participants);

        loadData();
    }

    private void loadData(){
        Intent intent = getIntent();
        Boolean isEdit = intent.getBooleanExtra("isEdit", false);

        // init scrollView
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setOnTouchListener((v, event) -> {
            v.clearFocus();
            return false;
        });
        // init nameView
        nameView.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        // init progress bar
        progressingMode(false);
        // init date map
        dateMap = new HashMap<>();
        // init pics gridView
        Drawable addDrawable = getResources().getDrawable(R.drawable.ic_add_box_yellow_100dp);
        addIcon = drawableToBitmap(addDrawable);
        pics = new ArrayList<>();
        picsUri = new ArrayList<>();
        pics.add(addIcon);
        picsView.setNumColumns(3);
        picsView.setVerticalSpacing(16);
        picsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(), "" + position,
                        Toast.LENGTH_SHORT).show();
                if(pics.get(position).equals(addIcon)){
                    selectPics();
                }
                else{
                    startPicFullscreen(position);
                }
                picsUpdate();
            }
        });
        picsUpdate();
    }

    public void showPlacePicker(View view) {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch ( GooglePlayServicesNotAvailableException |
                  GooglePlayServicesRepairableException ignored) {
        }
    }

    private void startPicFullscreen(int position){
        Intent intent = new Intent(EventEditActivity.this, EventPicFullscreenActivity.class);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        pics.get(position).compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        intent.putExtra("bitmap", byteArray);
        intent.putExtra("position", position);
        startActivityForResult(intent, IMAGE_REVIEW_REQUEST);
    }

    private void picsUpdate(){
        Bitmap lastPic = pics.get(pics.size()-1);
        if(pics.size() > MAX_NUM_OF_PHOTOS){
            if(lastPic.equals(addIcon)){
                pics.remove(lastPic);
            }
        }
        else{
            if(!lastPic.equals(addIcon)){
                pics.add(addIcon);
            }
        }

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams layoutParams = picsView.getLayoutParams();
        if(pics.size() > 3){
            layoutParams.height = 2*width/3 + 16;
        } else {
            layoutParams.height = width/3;
        }
        picsView.setLayoutParams(layoutParams);
        picsView.setAdapter(new ImageAdapter(EventEditActivity.this));

    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return pics.size();
        }

        public Object getItem(int position) {
            return pics.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                imageView.setLayoutParams(new GridView.LayoutParams(10*width/30, 10*width/30));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(pics.get(position));
            return imageView;
        }

    }


    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        setEventTime(hour, minute);
    }

    private void setEventTime(int hour, int minute){
        dateMap.put("hour", hour);
        dateMap.put("minute", minute);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, dateMap.get("year"));
        calendar.set(Calendar.MONTH, dateMap.get("month"));
        calendar.set(Calendar.DAY_OF_MONTH, dateMap.get("day"));
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Date time = calendar.getTime();

        String timeString =
                java.text.DateFormat.getDateInstance(
                        java.text.DateFormat.MEDIUM,
                        Locale.getDefault())
                        .format(time)
                + " " +
                java.text.DateFormat.getTimeInstance(
                        java.text.DateFormat.SHORT,
                        Locale.getDefault())
                        .format(time);
        timeField.setText(timeString);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        setEventDate(year, month, day);
        showTimePickerDialog();
    }

    private void setEventDate(int year, int month, int day){
        dateMap.put("year", year);
        dateMap.put("month", month);
        dateMap.put("day", day);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog() {
        DialogFragment timeFragment = new TimePickerFragment();
        timeFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void onInviteFriendClicked(View view) {
        Intent intent = new Intent(this, EventSelectFriendsActivity.class);
        intent.putExtra("selected", selectedUsers);
        startActivityForResult(intent, SELECT_FRIEND_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // select friend feedback
        switch (requestCode) {
            case SELECT_FRIEND_REQUEST:
            if(resultCode == RESULT_OK) {
                selectedUsers = (HashMap<String, UserInfoManager.UserInfo>)
                        intent.getSerializableExtra("selected");
                // show chips result
                setChipGroupView(selectedUsers);
            }
            break;
            case IMAGE_REVIEW_REQUEST:
            if(resultCode == RESULT_OK) {
                Boolean isDeleted = intent.getBooleanExtra("isDeleted", false);
                int position = intent.getIntExtra("position", -1);
                if(isDeleted){
                    pics.remove(position);
                    picsUri.remove(position);
                    picsUpdate();
                }
            }
            break;
            case PLACE_PICKER_REQUEST:
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, intent);
//                String toastMsg = String.format("Place: %s", place.getName());
//                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                String placeName = place.getName().toString();
                if (placeName.matches("\\d+°((\\d+'([\\d.]+\")?)?)?[WS] \\d+°((\\d+'([\\d.]+\")?)?)?[NE]")) {
                    // if place name is a coordinate
                    placeName = place.getAddress().toString();
                }
                locationField.setText(placeName);
                eventLocation = place;
            }
            break;
        }
    }

    private void setChipGroupView(HashMap<String, UserInfoManager.UserInfo> selected){
        //ArrayList<Chip> chipList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        ArrayList<String> uids = new ArrayList<>(selected.keySet());

        for (String uid: uids) {
            sb.append(uid);
            sb.append(" ");
        }

        participatnsField.setText(sb.toString());
        Editable ss = participatnsField.getText();

        int offset = 0;

        for (String uid: uids){
            UserInfoManager.UserInfo info = selected.get(uid);
            if (info == null) continue;

            String name = info.getName();

            ChipDrawable chip = ChipDrawable.createFromResource(this, R.xml.chip_user_profile);
            chip.setText(name);
            new URLChipDrawableCacheLoader(info.getPhotoUrl(), chip, getResources()).execute();
            chip.setBounds(0, 0, chip.getIntrinsicWidth(), chip.getIntrinsicHeight());

            ImageSpan span = new ImageSpan(chip, name, ImageSpan.ALIGN_BOTTOM);

            ss.setSpan(span, offset, offset + uid.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            offset += uid.length() + 1;
        }

//        participatnsField.setText(sb);
//        addEditChip2Group();
    }

//    private void addEditChip2Group(){
//        Chip chip = new Chip(this);
//        chip.setText("Add Friend");
//        chip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               onInviteFriendClicked();
//            }
//        });
//        chipgroup.addView(chip);
//    }

    public void uploadAll() {

        String name = nameView.getText().toString();

        String holder = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String location = "Test Location";

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, dateMap.get("year"));
        calendar.set(Calendar.MONTH, dateMap.get("month"));
        calendar.set(Calendar.DAY_OF_MONTH, dateMap.get("day"));
        calendar.set(Calendar.HOUR_OF_DAY, dateMap.get("hour"));
        calendar.set(Calendar.MINUTE, dateMap.get("minute"));
        Date eventDate = calendar.getTime();

        Map<String, Boolean> users = new HashMap<>();
        users.put(holder, true);


        EventsActivity.EventItem newEvent = new EventsActivity.EventItem(name, holder, location,
                eventDate, users, picsStoragePath, eventLocation.getName().toString(),
                eventLocation.getLatLng().longitude, eventLocation.getLatLng().latitude);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").add(newEvent).addOnCompleteListener( task -> {
                    if (task.isSuccessful()) {
                        String eid = task.getResult().getId();
                        if(selectedUsers != null && !selectedUsers.isEmpty()){

                            Map<String, String> data = new HashMap<>();
                            data.put("eid", eid);
                            data.put("name", name);

                            Gson gson = new Gson();
                            String payload = gson.toJson(data);

                            for(String user: selectedUsers.keySet()) {
                                ConversationManager.getInstance()
                                    .getPrivateConversation(user).sendMessage("event", payload);
                            }
                        }
                        finish();

                    } else {
                        // fail
                    }
                });

    }

    private void finishedEditEvent() {
        progressingMode(true);
        if (!picsUri.isEmpty()) {
            Uri uri = picsUri.get(0);
            picsUri.remove(0);

            try {
                FirebaseStorageHelper
                    .uploadImage(uri, null, "event", 70, true, (isSuccess, path) -> {
                        if (isSuccess) {
                            picsStoragePath.add(path);
                            finishedEditEvent();
                        } else {
                            //fail
                        }
                    });

            } catch (Exception e) {
                // fail

            }

        } else {
            // complete
            uploadAll();
        }
    }

    public void onPublishClicked(View v) {

        // check if name has entered
        if(nameView.getText().toString().length() == 0){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("You haven't complete event NAME");
            dialog.setPositiveButton("Oops! Forgot", (dialoginterface, i) -> dialoginterface.cancel());
            dialog.show();
        }

        // check if any friends selected
//        if(selectedUidList == null || selectedUidList.size() == 0) {
//            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//            dialog.setMessage("You haven't invite any friends");
//            dialog.setNegativeButton("I don't have friends", (dialoginterface, i) -> {
//                dialoginterface.cancel();
//                selectedUidList = new ArrayList<>();
//            });
//            dialog.setPositiveButton("Oops! Forgot", (dialoginterface, i) -> {
//                Intent intent = new Intent(EventEditActivity.this, EventSelectFriendsActivity.class);
//                startActivityForResult(intent, 1);
//            });
//            dialog.show();
//        }

        // check if date and time selected
        if(dateMap.size() < 5){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("You haven't complete event TIME");
            dialog.setPositiveButton("Oops! Forgot", (dialoginterface, i) -> dialoginterface.cancel());
            dialog.show();
        }

        // check if location selected
        if(eventLocation == null){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("You haven't complete event LOCATION");
            dialog.setPositiveButton("Oops! Forgot", (dialoginterface, i) -> dialoginterface.cancel());
            dialog.show();
        }

        // ready to finish
        else{
            finishedEditEvent();
        }
    }

    private void selectPics(){
        PickImageDialog.build(new PickSetup().setSystemDialog(true)).show(EventEditActivity.this);
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {

            pics.add(pics.size()-1, r.getBitmap());
            picsUri.add(r.getUri());

            picsUpdate();

        } else {
            //Handle possible errors
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void progressingMode(Boolean enterMode) {
        if(enterMode){
            scrollView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else{
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
    }



}
