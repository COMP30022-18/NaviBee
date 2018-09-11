package au.edu.unimelb.eng.navibee;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import au.edu.unimelb.eng.navibee.social.ChatActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private Date eventDate;
    private Map<String, Integer> dateMap;
    private ArrayList<Bitmap> pics;
    private GridView picsView;
    private Bitmap addIcon;

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

        dateMap = new HashMap<>();

        Button time_button = (Button)findViewById(R.id.eventPickTime);
        time_button.setText("Pick Time");
        Button date_button = (Button)findViewById(R.id.eventPickDate);
        date_button.setText("Pick Date");

        addIcon = createImage(50, 50, R.color.landing_red);

        pics = new ArrayList<>();
        pics.add(addIcon);

        picsView = (GridView) findViewById(R.id.eventPics);
        picsView.setNumColumns(3);
        picsUpdate();

        picsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(), "" + position,
                        Toast.LENGTH_SHORT).show();
                if(pics.size() <= 9){
                    if(position == (pics.size()-1)){
                        selectPics();
                    }
                    else{
                        // preview pics or edit it
                        startPicFullscreen(position);
                    }
                }
                else{
                    // preview pics or edit it
                    startPicFullscreen(position);
                }
                picsUpdate();
            }
        });
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
        if(pics.size() > 9){
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

    public void deletePicTest(View v){
        pics.remove(0);
        picsUpdate();
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
        dateMap.put("hour", hour);
        dateMap.put("minute", minute);
        Button time_button = (Button)findViewById(R.id.eventPickTime);
        Date time = new Date(0, 0, 0, hour, minute);
        String timeString = new SimpleDateFormat("HH:mm").format(time);
        time_button.setText(timeString);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        dateMap.put("year", year);
        dateMap.put("month", month);
        dateMap.put("day", day);
        Date date = new Date(year, month, day);
        String dateString = new SimpleDateFormat("EEE, MMM d").format(date);
        Button date_button = (Button)findViewById(R.id.eventPickDate);
        date_button.setText(dateString);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void onInviteFriendClicked(View v) {
        Intent intent = new Intent(this, EventSelectFriendsActivity.class);
        intent.putStringArrayListExtra("selectedUid", selectedUidList);
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                selectedUidList = intent.getStringArrayListExtra("selectedUid");
                //TextView participantsView = (TextView)findViewById(R.id.textView8);
                //participantsView.setText(selectedUidList.toString());

                // show chips result
            }
        }
        if (requestCode == 2) {
            if(resultCode == RESULT_OK) {
                Boolean isDeleted = intent.getBooleanExtra("isDeleted", false);
                int position = intent.getIntExtra("position", -1);
                if(isDeleted){
                    pics.remove(position);
                    picsUpdate();
                }
            }
        }
    }

    public void finishedEditEvent() {

        EditText editText = (EditText) findViewById(R.id.eventName);
        String name = editText.getText().toString();

        String holder = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String location = "Test Location";

        if(dateMap.isEmpty()){
            eventDate = new Date();
        }
        else{
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, dateMap.get("year"));
            calendar.set(Calendar.MONTH, dateMap.get("month"));
            calendar.set(Calendar.DAY_OF_MONTH, dateMap.get("day"));
            calendar.set(Calendar.HOUR, dateMap.get("hour"));
            calendar.set(Calendar.MINUTE, dateMap.get("minute"));
            Date date = calendar.getTime();
            eventDate = date;
        }

        Map<String, Boolean> users = new HashMap<>();
        for(String user: selectedUidList) {
            users.put(user, true);
        }
        users.put(holder, true);

        EventActivity.EventItem newEvent = new EventActivity.EventItem(name, holder, location, eventDate, users);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(UUID.randomUUID().toString()).set(newEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
            }
        });
    }

    public void onPublishClicked(View v) {

        if(selectedUidList == null || selectedUidList.size() == 0) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("You haven't invite any friends");
            dialog.setNegativeButton("I don't have friends", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    dialoginterface.cancel();
                    selectedUidList = new ArrayList<>();
                    finishedEditEvent();
                }});
            dialog.setPositiveButton("Oops! Forgot", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    Intent intent = new Intent(EventEditActivity.this, EventSelectFriendsActivity.class);
                    startActivityForResult(intent, 1);
                }});
            dialog.show();
        }
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
            //If you want the Uri.
            //Mandatory to refresh image from Uri.
            //getImageView().setImageURI(null);

            //Setting the real returned image.
            //getImageView().setImageURI(r.getUri());

            //If you want the Bitmap.
            pics.add(0, r.getBitmap());
            picsUpdate();
            //Image path
            //r.getPath();
        } else {
            //Handle possible errors
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static Bitmap createImage(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }

}
