package au.edu.unimelb.eng.navibee;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import au.edu.unimelb.eng.navibee.social.UserInfoManager;
import au.edu.unimelb.eng.navibee.utils.SimpleRVIndefiniteProgressBar;
import au.edu.unimelb.eng.navibee.utils.SimpleRVTextPrimarySecondaryStatic;
import au.edu.unimelb.eng.navibee.utils.SimpleRVTextSecondaryPrimaryStatic;
import au.edu.unimelb.eng.navibee.utils.SimpleRVUserChips;
import au.edu.unimelb.eng.navibee.utils.SimpleRecyclerViewAdaptor;
import au.edu.unimelb.eng.navibee.utils.SimpleRecyclerViewItem;

public class EventDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String uid;
    private String eid;
    private String relationship;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter viewAdapter;
    private RecyclerView.LayoutManager viewManager;
    private ArrayList<SimpleRecyclerViewItem> listItems = new ArrayList<>();

    private CarouselView carouselView;

    private EventsActivity.EventItem eventItem;
//    private Map<String, HashMap<String, String>> result;

    private Map<String, String> userMap;

    private int titleRowHeight = -1;
    private int primaryColor;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (relationship.equals("holder")) {
            menu.add(Menu.NONE, Menu.FIRST + 0, 0, "Edit the Event");
            menu.add(Menu.NONE, Menu.FIRST + 1, 1, "Delete the Event");
        } else if (relationship.equals("participant")) {
            menu.add(Menu.NONE, Menu.FIRST + 0, 0, "Quit the Event");
        } else {
            menu.add(Menu.NONE, Menu.FIRST + 0, 0, "Join the Event");
            menu.add(Menu.NONE, Menu.FIRST + 1, 1, "Follow the Event");
        }
        getMenuInflater().inflate(R.menu.menu_event_detial, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eid = getIntent().getStringExtra("eventId");
        relationship = getIntent().getStringExtra("relationship");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.event_details_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.event_details_appbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_details_toolbar);
        View toolbarPadding = (View) findViewById(R.id.event_details_toolbar_padding);
        TextView fabText = (TextView) findViewById(R.id.event_details_fab_text);

        primaryColor = primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);

        // Action Bar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // Set padding for status bar
        // Require API 20
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            toolbarPadding.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    ViewGroup.LayoutParams layoutParams = toolbarPadding.getLayoutParams();
                    layoutParams.height = windowInsets.getSystemWindowInsetTop();
                    toolbarPadding.setLayoutParams(layoutParams);

                    return windowInsets;
                }
            });
        } else {
            ViewGroup.LayoutParams layoutParams = toolbarPadding.getLayoutParams();

            int resId = toolbarPadding.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resId > 0) {
                layoutParams.height = toolbarPadding.getResources().getDimensionPixelOffset(resId);
            } else {
                layoutParams.height = 1024;
            }
            toolbarPadding.setLayoutParams(layoutParams);
        }

        // Remove redundant shadow in transparent app bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appbar.setOutlineProvider(null);
        }

        // Loading screen
        toolbar.setTitleTextColor(0);
        listItems.add(new SimpleRVIndefiniteProgressBar());

        // Recycler View
        recyclerView = (RecyclerView) findViewById(R.id.event_details_recycler_view);

        viewManager = new LinearLayoutManager(this);
        viewAdapter = new SimpleRecyclerViewAdaptor(listItems);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(viewManager);
        recyclerView.setAdapter(viewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        // Carousel view
        carouselView = (CarouselView) findViewById(R.id.event_details_image_preview);
        carouselView.setPageCount(1);
        carouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
//                imageView.setImageBitmap();
//                imageView.setImageResource(R.drawable.navibee_placeholder);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.navibee_placeholder));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.navibee_placeholder, null));
                } else {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.navibee_placeholder));
                }
            }
        });

        // Layout adjustment
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(recyclerView);
        bottomSheetBehavior.setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.618));
        recyclerView.setMinimumHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.618));

        ViewGroup.LayoutParams layoutParams = carouselView.getLayoutParams();
        layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.380);
        carouselView.setLayoutParams(layoutParams);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (i == BottomSheetBehavior.STATE_DRAGGING) {
                    updateTitleRowHeight(listItems);
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                if (listItems.size() > 0 && (listItems.get(0) instanceof  SimpleRVTextPrimarySecondaryStatic)) {
                    View item = viewManager.findViewByPosition(0);
                    if (item == null) {
                        return;
                    }
                    setViewHeightPercent(item, 1 - v, getSupportActionBar().getHeight(), titleRowHeight);

                    toolbar.setTitleTextColor(colorRGBA(0, 0, 0, v));
                    toolbar.setBackgroundColor(colorA(primaryColor, v));
                    toolbarPadding.setBackgroundColor(colorA(primaryColor, v));
                    fabText.setScaleX(1 - v);
                    fabText.setScaleY(1 - v);
                }
            }
        });

        recyclerView.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
            @Override
            public void onDraw() {
                updateTitleRowHeight(listItems);
            }
        });

        // Get event data
        db.collection("events").document(eid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eventItem = documentSnapshot.toObject(EventsActivity.EventItem.class);

                getEventInfo();
            }
        });

    }

    private void getEventInfo() {
        ArrayList<String> uidList = new ArrayList<>();
        uidList.addAll(eventItem.getUsers().keySet());

        // get the user information
        UserInfoManager.getInstance().getUserInfo(uidList, stringUserInfoMap -> {
            updateEventInfo();

            String holder = stringUserInfoMap.get(eventItem.getHolder()).getName();
            ArrayList<String> participants = new ArrayList<>();
//            ArrayList<String> photos = new ArrayList<>();

            userMap = new HashMap<>();

            for (int i = 0; i < uidList.size(); i++) {
                String userName = stringUserInfoMap.get(uidList.get(i)).getName();
                participants.add(userName);

                // Store user id and name into map
                userMap.put(uidList.get(i), userName);
            }

            // Event organiser
            if (eventItem.getHolder() != null) {
                listItems.add(new SimpleRVTextSecondaryPrimaryStatic(
                        holder,
                        getResources().getString(R.string.event_details_organiser)
                ));
            }

            ArrayList<Chip> chipList = new ArrayList<>();

            for (String participant : participants) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_user_profile, null);
                chip.setText(participant);
                // TODO use profile picture instead
                chip.setChipIconResource(R.drawable.ic_people_black_24dp);

                chipList.add(chip);
            }

            // Event participants
            if (eventItem.getUsers() != null) {
                listItems.add(new SimpleRVUserChips(
                        getResources().getString(R.string.event_details_participants),
                        chipList
                ));
            }

            viewAdapter.notifyDataSetChanged();

        });
    }

    private void updateEventInfo() {
        listItems.clear();

        // Event name && time
        if (eventItem.getName() != null && eventItem.getTime_() != null) {
            // Set support action bar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(eventItem.getName());
            }

            listItems.add(new SimpleRVTextPrimarySecondaryStatic(
                    eventItem.getName(),
                    new SimpleDateFormat(getResources().getString(R.string.date_format)).format(eventItem.getTime_())
            ));
        }

        // Event location
        if (eventItem.getLocation() != null) {
            listItems.add(new SimpleRVTextSecondaryPrimaryStatic(
                    eventItem.getLocation(),
                    getResources().getString(R.string.event_details_location)
            ));
        }
    }

    private void updateTitleRowHeight(ArrayList<SimpleRecyclerViewItem> listItems) {
        if (viewManager.getItemCount() > 0 && (listItems.get(0) instanceof SimpleRVTextPrimarySecondaryStatic)) {
            if (titleRowHeight == -1) {
                if (viewManager.findViewByPosition(0) != null) {
                    titleRowHeight = viewManager.findViewByPosition(0).getHeight();
                } else {
                    titleRowHeight = -1;
                }
            }
        }
    }

    private void setViewHeightPercent(View view, float percentage, int min, int max) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = (int) (min + (max - min) * percentage);
        view.setLayoutParams(params);
    }

    private int colorA(int color, float alpha) {
        return colorRGBA(Color.red(color), Color.green(color), Color.blue(color), alpha);
    }

    private int colorRGBA(int red, int green, int blue, float alpha) {
        return (int) (alpha * 255) << 24 | (red << 16) | (green << 8) | blue;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (relationship.equals("holder")) {
            switch (item.getItemId()) {
                case Menu.FIRST + 0:
                    Toast.makeText(this, "Edit is clicked", Toast.LENGTH_SHORT).show();
                    editEvent();
                    break;
                case Menu.FIRST + 1:
                    Toast.makeText(this, "Delete is clicked", Toast.LENGTH_SHORT).show();
                    deleteEvent();
                    break;
                default:
                    break;
            }
        } else if (relationship.equals("participant")) {
            switch (item.getItemId()) {
                case Menu.FIRST + 0:
                    Toast.makeText(this, "Quit is clicked", Toast.LENGTH_SHORT).show();
                    quitEvent();
                    break;
                default:
                    break;
            }
        } else {
            switch (item.getItemId()) {
                case Menu.FIRST + 0:
                    Toast.makeText(this, "Join is clicked", Toast.LENGTH_SHORT).show();
                    joinEvent();
                    break;
                case Menu.FIRST + 1:
                    Toast.makeText(this, "Follow is clicked", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private void quitEvent() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Alert");
        dialog.setMessage("Are you sure you want to QUIT this event?");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }
        });
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("users." + uid, FieldValue.delete());
                db.collection("events").document(eid).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        });
        dialog.show();
    }

    private void deleteEvent() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Alert");
        dialog.setMessage("Are you sure you want to DELETE this event?");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }
        });
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                db.collection("events").document(eid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
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
        });
        dialog.show();
    }

    private void joinEvent() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Alert");
        dialog.setMessage("Are you sure you want to JOIN this event?");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }
        });
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("users." + uid, true);
                db.collection("events").document(eid).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        });
        dialog.show();
    }

    private void editEvent() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Alert");
        dialog.setMessage("Are you sure you want to EDIT this event?");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }
        });
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                // collect event info and start edit event activity
                Intent intent = new Intent(EventDetailsActivity.this, EventEditActivity.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("eventName", eventItem.getName());

                startActivity(intent);

                finish();

                // delete old event
//                db.collection("events").document(eid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            // Task completed successfully
//                            finish();
//                        } else {
//                            // Task failed with an exception
//                        }
//                    }
//                });
            }
        });
        dialog.show();
    }

}
