package au.edu.unimelb.eng.navibee.social;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MovableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {

    // Often, there will be a slight, unintentional,
    // drag when the user taps the FAB,
    // so we need to account for this.
    private final static float CLICK_DRAG_TOLERANCE = 10;

    private float downRawX, downRawY;
    private float dX, dY;

    public MovableFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent){

        int action = motionEvent.getAction();

        if (action == MotionEvent.ACTION_DOWN) {

            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dX = view.getX() - downRawX;
            dY = view.getY() - downRawY;

            // Consumed
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {

            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            View viewParent = (View)view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight = viewParent.getHeight();

            float newX = motionEvent.getRawX() + dX;
            // Don't allow the FAB past the left hand side of the parent
            newX = Math.max(0, newX);
            // Don't allow the FAB past the right hand side of the parent
            newX = Math.min(parentWidth - viewWidth, newX);

            float newY = motionEvent.getRawY() + dY;
            // Don't allow the FAB past the top of the parent
            newY = Math.max(0, newY);
            // Don't allow the FAB past the bottom of the parent
            newY = Math.min(parentHeight - viewHeight, newY);

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();

            // Consumed
            return true;

        } else if (action == MotionEvent.ACTION_UP) {

            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();

            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;

            // A click
            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) {
                return performClick();
            }
            // A drag
            else {
                // Consumed
                return true;
            }

        } else {
            return super.onTouchEvent(motionEvent);
        }

    }

}
