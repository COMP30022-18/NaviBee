package au.edu.unimelb.eng.navibee.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

public class FirebaseStorageHelper {

    private static final int IMAGE_SIZE = 2000;
    private static final int THUMB_IMAGE_SIZE = 500;

    public static int getOrientation(Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = NaviBeeApplication.getInstance().getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    private static byte[] scaleImage(Uri photoUri, int imageSize, int compressQuality) throws IOException {
        Context context = NaviBeeApplication.getInstance();
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > imageSize || rotatedHeight > imageSize) {
            float widthRatio = ((float) rotatedWidth) / ((float) imageSize);
            float heightRatio = ((float) rotatedHeight) / ((float) imageSize);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        String type = context.getContentResolver().getType(photoUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        if (type.equals("image/png")) {
//            srcBitmap.compress(Bitmap.CompressFormat.PNG, compressQuality, baos);
//        } else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
        srcBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, baos);
//        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();

        return bMapArray;
    }



    public static UploadTask uploadImage(Uri uri, String uploadName, String category, int compressQuality) throws FileNotFoundException, IOException {
        // image
        byte[] bo = scaleImage(uri, IMAGE_SIZE, compressQuality);

        // thumb
        byte[] bt = scaleImage(uri, THUMB_IMAGE_SIZE, compressQuality);

        // storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        storageRef = storageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(category);

        if (uploadName==null) {
            uploadName = UUID.randomUUID().toString();
        }

        StorageReference storageRefOri = storageRef.child(uploadName + ".jpg");
        StorageReference storageRefThumb = storageRef.child(uploadName + "-thumb.jpg");


        // upload
        UploadTask uploadTaskOri = storageRefOri.putBytes(bo);
        UploadTask uploadTaskThumb = storageRefThumb.putBytes(bt);
        uploadTaskOri.pause();

        uploadTaskThumb.addOnSuccessListener(taskSnapshot -> uploadTaskOri.resume());
        uploadTaskThumb.addOnFailureListener(taskSnapshot -> uploadTaskOri.cancel());

        return uploadTaskOri;
    }

    public static void loadImage(ImageView imageView, String filePath, boolean isThumb) {

        if (isThumb) {
            int where = filePath.lastIndexOf(".");
            filePath = filePath.substring(0, where) + "-thumb" + filePath.substring(where);
        }

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
