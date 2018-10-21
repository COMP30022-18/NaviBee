package au.edu.unimelb.eng.navibee.event;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class EventPicFullscreenActivity extends AppCompatActivity {
    // TODO magic string

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST + 0, 0, R.string.eventPicFullscreen_delete_this);
        getMenuInflater().inflate(R.menu.menu_event_pic_fullscreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST + 0:
                Toast.makeText(this, R.string.eventPicFullscreen_edit_clicked, Toast.LENGTH_SHORT).show();
                deletePic();
                break;
            default:
                break;
        }
        return true;
    }

    private void deletePic(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.eventPicFullscreen_alert));
        dialog.setMessage(getString(R.string.eventFullScreen_delete_question));
        dialog.setNegativeButton("Cancel", (dialoginterface, i) -> dialoginterface.cancel());
        dialog.setPositiveButton("Ok", (dialoginterface, i) -> {
            Intent intent = new Intent(EventPicFullscreenActivity.this, EventEditActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("isDeleted", true);
            setResult(RESULT_OK, intent);
            finish();
        });
        dialog.show();
    }

}
