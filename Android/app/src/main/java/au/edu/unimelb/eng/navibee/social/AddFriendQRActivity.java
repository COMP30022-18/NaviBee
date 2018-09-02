package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
import android.graphics.Bitmap;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import au.edu.unimelb.eng.navibee.R;

public class AddFriendQRActivity extends AppCompatActivity {

    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final String ADD_FRIEND_URL = "https://comp30022-18.github.io/NaviBee/user?id=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_qr);
        generateQRCode();
    }

    private void generateQRCode() {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(ADD_FRIEND_URL + uid, BarcodeFormat.QR_CODE, 400, 400);
            ImageView imageViewQrCode = findViewById(R.id.addfriend_imageView_qrcode);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch(Exception e) {

        }
    }

    public void buttonScanOnClick(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan friend's QR code");
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                if (result.getContents().startsWith(ADD_FRIEND_URL)) {
                    String targetUId = result.getContents().replace(ADD_FRIEND_URL, "");
                    FriendManager.getInstance().addFriend(targetUId);
                } else {
                    Toast.makeText(this, "Wrong QR code", Toast.LENGTH_LONG).show();
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
