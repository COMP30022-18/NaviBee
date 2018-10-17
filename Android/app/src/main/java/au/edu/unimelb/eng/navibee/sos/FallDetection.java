package au.edu.unimelb.eng.navibee.sos;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import au.edu.unimelb.eng.navibee.NaviBeeApplication;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FallDetection implements SensorEventListener {
    private static final FallDetection instance = new FallDetection();
    private static final double FALL_THRESHOLD = 0.5d;

    public static FallDetection getInstance() {
        return instance;
    }


    enum State{
        Normal, Fall
    }


    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isRunning;
    private State state = State.Normal;
    private long fallTimestamp;



    private FallDetection() {
        mSensorManager = (SensorManager)NaviBeeApplication
                .getInstance().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        isRunning = false;
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            double laX = event.values[0];
            double laY = event.values[1];
            double laZ = event.values[2];

            double laTotal = Math.sqrt(Math.pow(laX, 2)
                    + Math.pow(laY, 2)
                    + Math.pow(laZ, 2));


            if (state == State.Normal && laTotal < FALL_THRESHOLD) {
                state = State.Fall;
                fallTimestamp = System.currentTimeMillis();

                Intent intent = new Intent(NaviBeeApplication.getInstance(), SosActivity.class);
                intent.putExtra("fall_detection", true);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                NaviBeeApplication.getInstance().startActivity(intent);

            } else if (System.currentTimeMillis() >= fallTimestamp + 2000) {
                state = State.Normal;
            }
        }

    }

    public void start() {
        if (!isRunning) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            isRunning = true;
        }

    }

    public void stop() {
        if (isRunning) {
            mSensorManager.unregisterListener(this);
            isRunning = false;
        }

    }


}
