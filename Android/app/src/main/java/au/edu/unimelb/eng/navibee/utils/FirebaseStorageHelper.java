package au.edu.unimelb.eng.navibee.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class FirebaseStorageHelper {


    public static UploadTask uploadImage(Bitmap bmp, String uploadName, String category, int compressQuality) {

        // compress image
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, compressQuality, bos);

        // storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        storageRef = storageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(category);

        if (uploadName==null) {
            uploadName = UUID.randomUUID().toString();
        }

        storageRef = storageRef.child(uploadName + ".jpg");

        // upload
        UploadTask uploadTask = storageRef.putBytes(bos.toByteArray());

        return uploadTask;
    }

    public static void downloadImage(ImageView imageView, String filePath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        storageRef = storageRef.child(filePath);

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                new DownloadImageTask(imageView)
                        .execute(uri.toString());
            }
        });

    }
}
