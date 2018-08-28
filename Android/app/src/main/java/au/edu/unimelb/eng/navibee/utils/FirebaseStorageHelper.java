package au.edu.unimelb.eng.navibee.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

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

    public static void loadImage(ImageView imageView, String filePath) {

        // fs- is for firebase storage caches
        String filename = "fs-"+ NetworkImageHelper.sha256(filePath);
        File file = new File(NaviBeeApplication.getInstance().getCacheDir(), filename);

        if (file.exists()) {
            // cache exists
            NetworkImageHelper.loadImageFromCacheFile(imageView, file);
        } else {
            // cache not exists

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            storageRef = storageRef.child(filePath);
            storageRef.getFile(file).addOnSuccessListener(taskSnapshot -> {
                // Local temp file has been created
                NetworkImageHelper.loadImageFromCacheFile(imageView, file);
            });

        }


    }

}
