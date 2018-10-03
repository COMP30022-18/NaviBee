package au.edu.unimelb.eng.navibee.social;

import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.nio.charset.Charset;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import au.edu.unimelb.eng.navibee.R;

public class AddFriendQRActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback{

    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final String ADD_FRIEND_URL = "https://comp30022-18.github.io/NaviBee/user?id=";

    private NfcAdapter mNfcAdapter;

    private CoordinatorLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_qr);
        rootLayout = (CoordinatorLayout) getWindow().getDecorView().getRootView();
        generateQRCode();
        findViewById(R.id.add_friend_progressbar).setVisibility(View.GONE);

        // Check if NFC is available on device
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            //This will refer back to createNdefMessage for what it will send
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            //This will be called if the message is sent successfully
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
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
        integrator.setPrompt(getResources().getString(R.string.friend_scan_qr_code));
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    public void onResume() {
        super.onResume();
        handleNfcIntent(getIntent());
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
