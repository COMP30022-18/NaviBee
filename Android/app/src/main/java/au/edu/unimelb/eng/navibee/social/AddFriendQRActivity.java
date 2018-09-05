package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
import android.graphics.Bitmap;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Map;

import au.edu.unimelb.eng.navibee.R;

public class AddFriendQRActivity extends AppCompatActivity {

    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final String ADD_FRIEND_URL = "https://comp30022-18.github.io/NaviBee/user?id=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_qr);
        generateQRCode();
        findViewById(R.id.add_friend_progressbar).setVisibility(View.GONE);
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
                    findViewById(R.id.add_friend_progressbar).setVisibility(View.VISIBLE);
                    Task<HttpsCallableResult> task = FriendManager.getInstance().addFriend(targetUId);

                    task.addOnFailureListener(httpsCallableResult -> {
                        Toast.makeText(this, "Network error.", Toast.LENGTH_LONG).show();
                        findViewById(R.id.add_friend_progressbar).setVisibility(View.GONE);
                    });
                    task.addOnSuccessListener(httpsCallableResult -> {
                        findViewById(R.id.add_friend_progressbar).setVisibility(View.GONE);
                        final Map<String, Object> res = ((Map<String, Object>) httpsCallableResult.getData());
                        if (((Integer) res.get("code"))==0) {
                            Toast.makeText(this, "Success.", Toast.LENGTH_LONG).show();
                            this.finish();
                        } else {
                            Toast.makeText(this, ((String) res.get("msg")), Toast.LENGTH_LONG).show();
                        }

                    });


                } else {
                    Toast.makeText(this, "Wrong QR code", Toast.LENGTH_LONG).show();
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
