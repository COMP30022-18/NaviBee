package au.edu.unimelb.eng.navibee.event;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.social.ConversationManager;
import au.edu.unimelb.eng.navibee.social.UserInfoManager;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;
import au.edu.unimelb.eng.navibee.utils.SquareImageView;
import au.edu.unimelb.eng.navibee.utils.URLChipDrawableCacheLoader;
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class EventEditActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, IPickResult {

    private static final int SELECT_FRIEND_REQUEST = 1;
    private static final int IMAGE_REVIEW_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;
    private final int MAX_NUM_OF_PHOTOS = 6;

    private HashMap<String, UserInfoManager.UserInfo> selectedUsers;
    private HashMap<String, Integer> dateMap;
    private ArrayList<Bitmap> pics;
    private ArrayList<Uri> picsUri;
    private ArrayList<String> picsStoragePath = new ArrayList<>();
    private GridView picsView;
    private TextInputEditText nameView;
    private TextInputEditText timeField;
    private TextInputEditText locationField;
    private TextInputEditText participatnsField;
    private TextInputLayout nameLayout;
    private TextInputLayout timeLayout;
    private TextInputLayout locationLayout;
    private ScrollView scrollView;
    private CircularProgressButton submit;
    private Place eventLocation;
    private Switch privateSwitch;

    private CoordinatorLayout coord;

    private boolean isEnabled = true;

    public static class TimePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(),
                    (EventEditActivity)getActivity(), hour, minute,
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
            DatePickerDialog dialog =
                    new DatePickerDialog(getActivity(),
                            (EventEditActivity)getActivity(), year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            return dialog;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_edit_new);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameView = findViewById(R.id.eventName);
        picsView = findViewById(R.id.eventPics);
        scrollView = findViewById(R.id.eventScrollView);
        timeField = findViewById(R.id.event_create_time);
        locationField = findViewById(R.id.event_create_location);
        participatnsField = findViewById(R.id.event_create_participants);
        privateSwitch = findViewById(R.id.eventPrivateSwitch);

        nameLayout = findViewById(R.id.event_create_name_layout);
        timeLayout = findViewById(R.id.event_create_time_layout);
        locationLayout = findViewById(R.id.event_create_location_layout);

        submit = findViewById(R.id.eventPublish);

        coord = findViewById(R.id.event_edit_coord);

        loadData();

        setupFields();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupFields() {
        // init nameView
        nameView.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        nameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    nameLayout.setError(getResources().getString(R.string.event_create_name_required));
                } else {
                    nameLayout.setError(null);
                }
            }
        });
    }

    private void loadData(){
        // init scrollView
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setOnTouchListener((v, event) -> {
            v.clearFocus();
            return false;
        });
        // init progress bar
        progressingMode(false);
        // init date map
        dateMap = new HashMap<>();
        // init pics gridView
        pics = new ArrayList<>();
        picsUri = new ArrayList<>();
        picsView.setOnItemClickListener((parent, v, position, id) -> {
            if (isEnabled) {
                if (position == pics.size()) {
                    selectPics();
                } else {
                    startPicFullscreen(position);
                }
                picsUpdate();
            }
        });
        picsUpdate();
    }

    public void showPlacePicker(View view) {
        locationLayout.setError(null);
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
        picsView.setAdapter(new ImageAdapter(EventEditActivity.this));
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return Math.min(pics.size() + 1, MAX_NUM_OF_PHOTOS);
        }

        public Object getItem(int position) {
            if (position < pics.size())
                return pics.get(position);
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            SquareImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new SquareImageView(mContext);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setPadding(8, 8, 8, 8);
                imageView.setPadding(8, 8, 8, 8);
                imageView.setPadding(8, 8, 8, 8);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (SquareImageView) convertView;
            }
            if (position < pics.size()) {
                imageView.setImageBitmap(pics.get(position));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setBackgroundResource(R.color.transparentBlack);
            } else {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white_48dp));
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setBackgroundResource(R.color.black_overlay);
            }
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

        final Calendar c = Calendar.getInstance();
        int tYear = c.get(Calendar.YEAR);
        int tMonth = c.get(Calendar.MONTH);
        int tDay = c.get(Calendar.DAY_OF_MONTH);

        boolean isToday = year == tYear && month == tMonth && day == tDay;

        showTimePickerDialog(isToday);
    }

    private void setEventDate(int year, int month, int day){
        dateMap.put("year", year);
        dateMap.put("month", month);
        dateMap.put("day", day);
    }

    public void showDatePickerDialog(View v) {
        timeLayout.setError(null);
        DatePickerFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(boolean isToday) {
        TimePickerFragment timeFragment = new TimePickerFragment();
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
                String placeName = getPlaceDisplayName(place);
                locationField.setText(placeName);
                eventLocation = place;
            }
            break;
        }
    }

    private String getPlaceDisplayName(Place place) {
        String placeName = place.getName().toString();
        if (placeName.matches("\\d+°((\\d+'([\\d.]+\")?)?)?[NS] \\d+°((\\d+'([\\d.]+\")?)?)?[WE]")) {
            // if place name is a coordinate
            placeName = place.getAddress().toString();
        }
        return placeName;
    }

    private void setChipGroupView(HashMap<String, UserInfoManager.UserInfo> selected){

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

    }


    public void uploadAll() {

        String name = nameView.getText().toString();

        String holder = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, dateMap.get("year"));
        calendar.set(Calendar.MONTH, dateMap.get("month"));
        calendar.set(Calendar.DAY_OF_MONTH, dateMap.get("day"));
        calendar.set(Calendar.HOUR_OF_DAY, dateMap.get("hour"));
        calendar.set(Calendar.MINUTE, dateMap.get("minute"));
        Date eventDate = calendar.getTime();

        Map<String, Boolean> users = new HashMap<>();
        users.put(holder, true);

        EventsActivity.EventItem newEvent = new EventsActivity.EventItem(name, holder,
                eventDate, users, picsStoragePath, getPlaceDisplayName(eventLocation),
                eventLocation.getLatLng().longitude, eventLocation.getLatLng().latitude, privateSwitch.isChecked());

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
                        badInternetConnection();
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
                            badInternetConnection();
                        }
                    });

            } catch (Exception e) {
                badInternetConnection();
            }

        } else {
            // complete
            uploadAll();
        }
    }

    private void badInternetConnection() {
        Snackbar.make(coord, R.string.error_failed_to_connect_to_server, Snackbar.LENGTH_LONG).show();
        progressingMode(false);
    }

    public void onPublishClicked(View v) {
        Boolean valid = true;

        // check if name has entered
        if(nameView.getText().length() == 0){
            nameLayout.setError(getResources().getString(R.string.event_create_name_required));
            valid = false;
        }

        // check if date and time selected
        if(dateMap.size() < 5){
            timeLayout.setError(getResources().getString(R.string.event_create_time_required));
            valid = false;
        }

        // check if date is after now
        if(!isTimeAfterNow(dateMap)){
            timeLayout.setError(getString(R.string.event_create_time_invalid));
            valid = false;
        }

        // check if location selected
        if(eventLocation == null){
            locationLayout.setError(getResources().getString(R.string.event_create_location_required));
            valid = false;
        }

        if (valid) {
            // ready to finish
            finishedEditEvent();
        }
    }

    private boolean isTimeAfterNow(HashMap<String, Integer> dateMap){
        Calendar calendar = Calendar.getInstance();
        if (dateMap == null ||
            dateMap.get("year") == null ||
            dateMap.get("month") == null ||
            dateMap.get("day") == null ||
            dateMap.get("hour") == null ||
            dateMap.get("minute") == null)
            return false;
        calendar.set(Calendar.YEAR, dateMap.get("year"));
        calendar.set(Calendar.MONTH, dateMap.get("month"));
        calendar.set(Calendar.DAY_OF_MONTH, dateMap.get("day"));
        calendar.set(Calendar.HOUR_OF_DAY, dateMap.get("hour"));
        calendar.set(Calendar.MINUTE, dateMap.get("minute"));
        Date testTime = calendar.getTime();

        Date currentTime = new Date();

        return testTime.compareTo(currentTime) >= 0;
    }


    private void selectPics(){
        PickImageDialog.build(new PickSetup().setSystemDialog(true)).show(EventEditActivity.this);
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {

            pics.add(r.getBitmap());
            picsUri.add(r.getUri());

            picsUpdate();

        } else {
            //Handle possible errors
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void progressingMode(Boolean enterMode) {
        if (enterMode) {
            nameView.setEnabled(false);
            participatnsField.setEnabled(false);
            locationField.setEnabled(false);
            timeField.setEnabled(false);
            privateSwitch.setEnabled(false);
            isEnabled = false;
            submit.startAnimation();
        } else {
            nameView.setEnabled(true);
            participatnsField.setEnabled(true);
            locationField.setEnabled(true);
            timeField.setEnabled(true);
            privateSwitch.setEnabled(true);
            isEnabled = true;
            submit.revertAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        submit.dispose();
    }
}
