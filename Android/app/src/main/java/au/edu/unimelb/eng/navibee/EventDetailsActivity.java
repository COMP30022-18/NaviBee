package au.edu.unimelb.eng.navibee;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import static au.edu.unimelb.eng.navibee.utils.DimensionsUtilitiesKt.getStatusBarHeight;

public class EventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_details_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.event_details_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // set padding for status bar
        View toolbarPadding = findViewById(R.id.event_details_toolbar_padding);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            toolbarPadding.setOnApplyWindowInsetsListener((View view, WindowInsets insets) -> {
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.height = insets.getSystemWindowInsetTop();
                view.setLayoutParams(lp);

                return insets;
            });
        } else {
            ViewGroup.LayoutParams lp = toolbarPadding.getLayoutParams();
            lp.height = getStatusBarHeight(toolbarPadding);
            toolbarPadding.setLayoutParams(lp);

        }

    }
}
