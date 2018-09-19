package au.edu.unimelb.eng.navibee;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import au.edu.unimelb.eng.navibee.social.ChatActivity;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.BlackLevelPattern;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventEditActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, IPickResult {

    private ArrayList<String> selectedUidList;
    private ArrayList<String> selectedNameList;
    private Map<String, Integer> dateMap;
    private ArrayList<Bitmap> pics;
    private ArrayList<Uri> picsUri;
    private ArrayList<String> picsStoragePath = new ArrayList<>();
    private GridView picsView;
    private EditText nameView;
    private Button timeButton;
    private Button dateButton;
    private Bitmap addIcon;
    private ChipGroup chipgroup;
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
        timeButton = (Button)findViewById(R.id.eventPickTime);
        dateButton = (Button)findViewById(R.id.eventPickDate);
        chipgroup = (ChipGroup) findViewById(R.id.eventFriendChips);
        picsView = (GridView) findViewById(R.id.eventPics);

        loadData();
    }

    private void loadData(){
        Intent intent = getIntent();
        Boolean isEdit = intent.getBooleanExtra("isEdit", false);
        // init date map
        dateMap = new HashMap<>();
        // init date and time button
        timeButton.setText("Pick Time");
        dateButton.setText("Pick Date");
        // init chipGroup
        addEditChip2Group();
        // init pics gridView
        Drawable addDrawable = getResources().getDrawable(R.drawable.ic_add_black_100dp);
        addIcon = drawableToBitmap(addDrawable);
        pics = new ArrayList<>();
        picsUri = new ArrayList<>();
        pics.add(addIcon);
        picsView.setNumColumns(3);
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

        // load data from previous event if this is not creation activity
        if(isEdit){
            // load name
            nameView.setText(intent.getStringExtra("eventName"));
            // load participants
            selectedUidList = intent.getStringArrayListExtra("selectedUidList");
            selectedNameList = intent.getStringArrayListExtra("selectedNameList");
            setChipGroupView(selectedUidList, selectedNameList);
            // load time and date
            Date oldDate = new Date();
            oldDate.setTime(intent.getLongExtra("eventTime", -1));
            Calendar cal = Calendar.getInstance();
            cal.setTime(oldDate);
            setEventTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            setEventDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
        }
    }

    private void startPicFullscreen(int position){
        Intent intent = new Intent(EventEditActivity.this, EventPicFullscreenActivity.class);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        pics.get(position).compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        intent.putExtra("bitmap", byteArray);
        intent.putExtra("position", position);
        startActivityForResult(intent, 2);
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
                imageView.setLayoutParams(new GridView.LayoutParams(9*width/30, 9*width/30));
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
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Date time = calendar.getTime();

        String timeString = new SimpleDateFormat("HH:mm").format(time);
        timeButton.setText(timeString);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        setEventDate(year, month, day);
    }

    private void setEventDate(int year, int month, int day){
        dateMap.put("year", year);
        dateMap.put("month", month);
        dateMap.put("day", day);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        Date date = calendar.getTime();

        String dateString = new SimpleDateFormat("EEE, MMM d").format(date);
        dateButton.setText(dateString);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void onInviteFriendClicked() {
        Intent intent = new Intent(this, EventSelectFriendsActivity.class);
        intent.putStringArrayListExtra("selectedUid", selectedUidList);
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // select friend feedback
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                selectedUidList = intent.getStringArrayListExtra("selectedUid");
                selectedNameList = intent.getStringArrayListExtra("selectedName");
                // show chips result
                setChipGroupView(selectedUidList, selectedNameList);
            }
        }
        // fullscreen picture feedback
        if (requestCode == 2) {
            if(resultCode == RESULT_OK) {
                Boolean isDeleted = intent.getBooleanExtra("isDeleted", false);
                int position = intent.getIntExtra("position", -1);
                if(isDeleted){
                    pics.remove(position);
                    picsUri.remove(position);
                    picsUpdate();
                }
            }
        }
    }

    private void setChipGroupView(ArrayList<String> selectedUidList, ArrayList<String> selectedNameList){
        //ArrayList<Chip> chipList = new ArrayList<>();
        chipgroup.removeAllViews();

        for(String name: selectedNameList){
            Chip chip = new Chip(this);
            chip.setText(name);

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((Chip)view).isCloseIconVisible()){
                        ((Chip)view).setCloseIconVisible(false);
                    }
                    else{
                        ((Chip)view).setCloseIconVisible(true);
                    }
                }
            });

            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // need to delete from selected list
                    String dName = (String)((Chip)view).getChipText();
                    selectedUidList.remove(selectedNameList.indexOf(dName));
                    selectedNameList.remove(dName);
                    chipgroup.removeView(view);
                }
            });

            chipgroup.addView(chip);
        }
        addEditChip2Group();
    }

    private void addEditChip2Group(){
        Chip chip = new Chip(this);
        chip.setText("Add Friend");
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onInviteFriendClicked();
            }
        });
        chipgroup.addView(chip);
    }

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
        for(String user: selectedUidList) {
            users.put(user, true);
        }
        users.put(holder, true);

        EventsActivity.EventItem newEvent = new EventsActivity.EventItem(name, holder, location, eventDate, users, picsStoragePath);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(UUID.randomUUID().toString()).set(newEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // if need to delete the old event
                if(getIntent().getBooleanExtra("isEdit", false)){
                    // delete old event
                    db.collection("events").document(getIntent().getStringExtra("eventId")).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Task completed successfully
                                finish();
                            } else {
                                // Task failed with an exception
                            }
                        }
                    });
                }
                // it is the creation
                else{
                    finish();
                }
            }
        });
    }

    private void finishedEditEvent() {
        if (!picsUri.isEmpty()) {
            Uri uri = picsUri.get(0);
            picsUri.remove(0);

            try {
                UploadTask task = FirebaseStorageHelper.uploadImage(uri, null, "event", 80);
                task.addOnCompleteListener(taskResult -> {
                    if (taskResult.isSuccessful()) {
                        picsStoragePath.add(taskResult.getResult().getMetadata().getPath());
                        finishedEditEvent();
                    } else {
                        // fail
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
            dialog.setMessage("You haven't complete event name");
            dialog.setPositiveButton("Oops! Forgot", (dialoginterface, i) -> dialoginterface.cancel());
            dialog.show();
        }

        // check if any friends selected
        if(selectedUidList == null || selectedUidList.size() == 0) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("You haven't invite any friends");
            dialog.setNegativeButton("I don't have friends", (dialoginterface, i) -> {
                dialoginterface.cancel();
                selectedUidList = new ArrayList<>();
                finishedEditEvent();
            });
            dialog.setPositiveButton("Oops! Forgot", (dialoginterface, i) -> {
                Intent intent = new Intent(EventEditActivity.this, EventSelectFriendsActivity.class);
                startActivityForResult(intent, 1);
            });
            dialog.show();
        }

        // check if date and time selected
        if(dateMap.size() < 5){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("You haven't complete event time");
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

            pics.add(0, r.getBitmap());
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



}
