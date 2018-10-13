package au.edu.unimelb.eng.navibee.social;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import au.edu.unimelb.eng.navibee.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class AddFriendQRActivity extends AppCompatActivity
        implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback,
        ZXingScannerView.ResultHandler {

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;

    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final String ADD_FRIEND_URL = "https://comp30022-18.github.io/NaviBee/user?id=";

    private NfcAdapter mNfcAdapter;
    private ZXingScannerView mScannerView;
    private CoordinatorLayout rootLayout;

    private Bitmap qrCode;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_qr);
        rootLayout = findViewById(R.id.addfriend_coordinatorlayout);
        generateQRCode();
        findViewById(R.id.add_friend_progressbar).setVisibility(View.GONE);

        // Setup scanner.
        ViewGroup contentFrame = findViewById(R.id.addfriend_scanner_frame);
        mScannerView = new ZXingScannerView(this);
        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);
        contentFrame.addView(mScannerView);

        // Check if NFC is available on device
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            // Setup label
            ((TextView) findViewById(R.id.addfriend_prompt))
                    .setText(R.string.friend_scan_qr_code_with_nfc);

            //This will refer back to createNdefMessage for what it will send
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            //This will be called if the message is sent successfully
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setMessage(R.string.friend_request_camera)
                        .setPositiveButton(R.string.button_ok, (dialog, which) ->
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.CAMERA},
                                        PERMISSIONS_REQUEST_CAMERA))
                        .create().show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }

    private void generateQRCode() {
        try {
            qrCode = new BarcodeEncoder().encodeBitmap(ADD_FRIEND_URL + uid,
                    BarcodeFormat.QR_CODE, 768, 768);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            ImageView image = new ImageView(this);
            image.setImageBitmap(qrCode);
            builder.setView(image);
            dialog = builder.create();
        } catch (Exception ignored) {
        }
    }

    public void onClickShowQr(View view) {
        // Show my QR
        if (dialog != null) {
            dialog.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        handleNfcIntent(getIntent());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                    mScannerView.startCamera();
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        addFriend(rawResult.getText());

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
        Handler handler = new Handler();
        handler.postDelayed(() -> mScannerView
                .resumeCameraPreview(AddFriendQRActivity.this), 2000);
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            addFriend(result.getContents());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showMessage(int messageId) {
        showMessage(getResources().getString(messageId));
    }

    private void showMessage(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void addFriend(String url) {
        if (url != null) {
            if (url.startsWith(ADD_FRIEND_URL)) {
                String targetUId = url.replace(ADD_FRIEND_URL, "");
                findViewById(R.id.add_friend_progressbar).setVisibility(View.VISIBLE);
                Task<HttpsCallableResult> task = ConversationManager.getInstance().addFriend(targetUId);

                task.addOnFailureListener(httpsCallableResult -> {
                    showMessage(R.string.error_failed_to_connect_to_server);
                    findViewById(R.id.add_friend_progressbar).setVisibility(View.GONE);
                });
                task.addOnSuccessListener(httpsCallableResult -> {
                    findViewById(R.id.add_friend_progressbar).setVisibility(View.GONE);
                    final Map<String, Object> res = ((Map<String, Object>) httpsCallableResult.getData());
                    if (((Integer) res.get("code")) == 0) {
                        this.finish();
                    } else {
                        Toast.makeText(this, ((String) res.get("msg")), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                showMessage(R.string.friend_wrong_qr_code);
            }
        }
    }

    public NdefRecord[] createRecords() {

        NdefRecord[] records = new NdefRecord[2];

        byte[] payload = (ADD_FRIEND_URL + uid).getBytes(Charset.forName("UTF-8"));

        records[0] = NdefRecord.createMime("application/navibee", payload);

        // Tell OS that which application handles this record
        records[1] =
                NdefRecord.createApplicationRecord(getPackageName());


        return records;
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        //When creating an NdefMessage we need to provide an NdefRecord[]
        return new NdefMessage(createRecords());
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleNfcIntent(intent);
    }

    private void handleNfcIntent(Intent NfcIntent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction())) {
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(receivedArray != null) {
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();

                for (NdefRecord record: attachedRecords) {
                    String string = new String(record.getPayload());
                    // Make sure we don't pass along our AAR (Android Application Record)
                    if (string.equals(getPackageName())) continue;
                    addFriend(string);
                }
            }
        }
    }

}
