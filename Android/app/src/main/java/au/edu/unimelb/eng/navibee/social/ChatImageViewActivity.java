package au.edu.unimelb.eng.navibee.social;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;
import au.edu.unimelb.eng.navibee.utils.FirebaseStorageHelper;

public class ChatImageViewActivity extends AppCompatActivity implements FirebaseStorageHelper.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image_view);

        String imageFsPath = getIntent().getStringExtra("IMG_FS_PATH");
        PhotoView photoView = findViewById(R.id.chat_photo_view);
        FirebaseStorageHelper.loadImage(photoView, imageFsPath, true);

        FirebaseStorageHelper.loadImage(photoView, imageFsPath, false, this);
    }

    @Override
    public void callback(boolean isSuccess) {
        ProgressBar progressBar = findViewById(R.id.chat_photo_progressBar);
        progressBar.setVisibility(View.GONE);
        if (!isSuccess) {
            Toast.makeText(ChatImageViewActivity.this, "Failed to load the image.", Toast.LENGTH_LONG).show();
        }
    }
}
