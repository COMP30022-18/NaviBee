package au.edu.unimelb.eng.navibee.utils;

import android.graphics.Bitmap;
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
}
