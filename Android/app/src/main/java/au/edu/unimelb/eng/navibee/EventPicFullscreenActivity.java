package au.edu.unimelb.eng.navibee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class EventPicFullscreenActivity extends AppCompatActivity {

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_pic_fullscreen);

        Intent intent = getIntent();

        byte[] byteArray = getIntent().getByteArrayExtra("bitmap");
        Bitmap pic = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        position = intent.getIntExtra("position", -1);

        ImageView fullscreenView = findViewById(R.id.eventPicFullscreen);
        fullscreenView.setImageBitmap(pic);

    }

}
