package au.edu.unimelb.eng.navibee;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    private RecyclerView recyclerView;
    private RecyclerView.Adapter viewAdapter;
    private RecyclerView.LayoutManager viewManager;
    private ArrayList<SimpleRecyclerViewItem> listItems = new ArrayList<>();

    private CarouselView carouselView;

    private EventActivity.EventItem eventItem;
    private Map<String, HashMap<String, String>> result;

    private int titleRowHeight = -1;
    private int primaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eid = getIntent().getStringExtra("eventId");

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

        // Get primary color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            primaryColor = getResources().getColor(R.color.colorPrimary, null);
        } else {
            primaryColor = getResources().getColor(R.color.colorPrimary);
        }

        // Action Bar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                if (listItems.size() > 0 && (listItems.get(0) instanceof SimpleRVTextPrimarySecondaryStatic)) {
                    if (i == BottomSheetBehavior.STATE_DRAGGING && titleRowHeight == -1) {
                        if (viewManager.findViewByPosition(0) != null) {
                            titleRowHeight = viewManager.findViewByPosition(0).getHeight();
                        }
                    }
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

        // Get event data
        db.collection("events").document(eid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eventItem = documentSnapshot.toObject(EventActivity.EventItem.class);

                getEventInfo();
            }
        });

    }

    private void getEventInfo() {
        ArrayList<String> uidList = new ArrayList<>();

        uidList.add(eventItem.getHolder());
        uidList.addAll(eventItem.getUsers().keySet());

        Map<String, Object> data = new HashMap<>();
        data.put("uidList", uidList);

        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();

        mFunctions
                .getHttpsCallable("getUserInfoFromUidList")
                .call(data)
                .addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {
                        result = (Map<String, HashMap<String, String>>) httpsCallableResult.getData();

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

                        String holder = null;
                        ArrayList<String> participants = new ArrayList<>();
                        ArrayList<String> photos = new ArrayList<>();

                        for (String uid : result.keySet()) {
                            HashMap<String, String> users = result.get(uid);
                            if (uid.equals(eventItem.getHolder())) {
                                holder = users.get("name");
                            }
                            participants.add(users.get("name"));
                            photos.add(users.get("photoURL"));
                        }

                        // Event organiser
                        if (eventItem.getHolder() != null) {
                            listItems.add(new SimpleRVTextSecondaryPrimaryStatic(
                                    holder,
                                    getResources().getString(R.string.event_details_organiser)
                            ));
                        }

//                        ChipGroup chipGroup = (ChipGroup) findViewById(R.id.general_recycler_view_user_chip_chipgroup);

//                        for (String participant : participants) {
//                            Chip chip = new Chip(this);
//                            chip.setText(participant);
//                            chip.setCloseIconVisible(true);
                            //chip.setCloseIconResource(R.drawable.your_icon);
                            //chip.setChipIconResource(R.drawable.your_icon);
//                            chip.setChipBackgroundColorResource(R.color.colorPrimary);
//                            chip.setTextAppearanceResource(R.style.ChipTextStyle);
                            //chip.setElevation(15);
//                            Chip chip = generateChip(participant);
//
//                            chipGroup.addView(chip);
//                        }

                        // Event participants
                        if (eventItem.getUsers() != null) {
                            listItems.add(new SimpleRVTextSecondaryPrimaryStatic(
                                    participants.toString(),
                                    getResources().getString(R.string.event_details_participants)
                            ));
//                            listItems.add(new SimpleRVUserChips(
//                                    getResources().getString(R.string.event_details_participants),
//                                    chipGroup
//                            ));
                        }

                        viewAdapter.notifyDataSetChanged();
                    }
                });
    }

    private ChipDrawable generateChip(String text) {
        ChipDrawable chip = ChipDrawable.createFromResource(this, R.xml.standalone_chip);

        chip.setChipBackgroundColorResource(R.color.colorPrimary);
        chip.setText(text);

        return chip;
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
}
