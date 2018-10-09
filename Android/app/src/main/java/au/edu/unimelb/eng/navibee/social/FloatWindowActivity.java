package au.edu.unimelb.eng.navibee.social;

import androidx.appcompat.app.AppCompatActivity;
import au.edu.unimelb.eng.navibee.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

public class FloatWindowActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQ_CODE = 0x001;

    private DraggableFloatWindow mFloatWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float_window);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case OVERLAY_PERMISSION_REQ_CODE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(this)) {
                            Toast.makeText(FloatWindowActivity.this, "权限授予失败，无法开启悬浮窗", Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO: 18/1/7 已经授权
                        }
                    }
                    break;
            }
        }
    }

    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.showBt:
                mFloatWindow = DraggableFloatWindow.getDraggableFloatWindow(FloatWindowActivity.this, null);
                mFloatWindow.show();
                mFloatWindow.setOnTouchButtonListener(new DraggableFloatView.OnTouchButtonClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(FloatWindowActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.dismissBt:
                // @lhr2528 you can fix issue 2 here
                if (mFloatWindow != null) {
                    mFloatWindow.dismiss();
                }
                break;
        }
    }

}
