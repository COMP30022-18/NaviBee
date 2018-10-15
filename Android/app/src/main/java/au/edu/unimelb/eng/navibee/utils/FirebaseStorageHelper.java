package au.edu.unimelb.eng.navibee.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

public class FirebaseStorageHelper {

    private static final int IMAGE_SIZE = 1500;
    private static final int THUMB_IMAGE_SIZE = 500;

    // for file
    private static int getFileOrientation(File imageFile){
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    // for Content
    public static int getContentOrientation(Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = NaviBeeApplication.getInstance().getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        int i = cursor.getInt(0);
        cursor.close();
        return i;
    }

    private static byte[] scaleImage(Uri photoUri, int imageSize, int compressQuality) throws IOException {
        Context context = NaviBeeApplication.getInstance();
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation;
        if (photoUri.getScheme().equals("content")) {
            orientation = getContentOrientation(photoUri);
        } else {
            orientation = getFileOrientation(new File(photoUri.getPath()));
        }

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



    public static void uploadImage(Uri uri, String uploadName, String category,
              int compressQuality, boolean thumbOnly, UploadCallback callback) throws IOException {


        // storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        storageRef = storageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(category);

        if (uploadName==null) {
            uploadName = UUID.randomUUID().toString();
        }

        StorageReference storageRefOri = storageRef.child(uploadName + ".jpg");
        StorageReference storageRefThumb = storageRef.child(uploadName + "-thumb.jpg");

        String fullFilename = storageRefOri.getPath();




        ArrayList<UploadTask> tasks = new ArrayList<>();


        if (!thumbOnly) {
            // image
            byte[] bo = scaleImage(uri, IMAGE_SIZE, compressQuality);
            tasks.add(storageRefOri.putBytes(bo));
        }

        // thumb
        byte[] bt = scaleImage(uri, THUMB_IMAGE_SIZE, compressQuality);
        tasks.add(storageRefThumb.putBytes(bt));

        AtomicBoolean failed = new AtomicBoolean(false);

        for (UploadTask task : tasks) {
            task.addOnCompleteListener(result -> {
                if (failed.get()) return;

                if (result.isSuccessful()) {
                    tasks.remove(task);
                    if (tasks.isEmpty()) {
                        callback.callback(true, fullFilename);
                    }
                } else {
                    failed.set(true);
                    for (UploadTask t:tasks) {
                        if (t!=task) {
                            t.cancel();
                        }
                    }
                    callback.callback(false, "");
                }
            });
        }
    }

    public interface UploadCallback {
        void callback(boolean isSuccess, String path);
    }



    public static void loadImage(ImageView imageView, String filePath, boolean isThumb) {
        loadImage(imageView, filePath, isThumb, null);
    }

    public static void loadImage(ImageView imageView, String filePath, boolean isThumb, Callback callback) {
        final String tag = filePath;
        imageView.setTag(tag);

        if (isThumb) {
            int where = filePath.lastIndexOf(".");
            filePath = filePath.substring(0, where) + "-thumb" + filePath.substring(where);
        }

        // fs- is for firebase storage caches
        String filename = "fs-"+ sha256(filePath);
        File file = new File(NaviBeeApplication.getInstance().getCacheDir(), filename);

        if (file.exists()) {
            // cache exists
            loadImageFromCacheFile(imageView, file, tag);
            if (callback != null) callback.callback(true);
        } else {
            // cache not exists

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            storageRef = storageRef.child(filePath);
            storageRef.getFile(file).addOnSuccessListener(taskSnapshot -> {
                // Local temp file has been created
                loadImageFromCacheFile(imageView, file, tag);
                if (callback != null) callback.callback(true);
            }).addOnFailureListener(taskSnapshot -> {
                if (callback != null) callback.callback(false);
            });

        }
    }

    public static void loadImage(String filePath, boolean isThumb, PayloadCallback callback) {

        if (isThumb) {
            int where = filePath.lastIndexOf(".");
            filePath = filePath.substring(0, where) + "-thumb" + filePath.substring(where);
        }

        // fs- is for firebase storage caches
        String filename = "fs-"+ sha256(filePath);
        File file = new File(NaviBeeApplication.getInstance().getCacheDir(), filename);

        if (file.exists()) {
            // cache exists
            Bitmap b = loadImageFromCacheFile(file);
            if (callback != null) callback.callback(true, b);
        } else {
            // cache not exists
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            storageRef = storageRef.child(filePath);
            storageRef.getFile(file).addOnSuccessListener(taskSnapshot -> {
                // Local temp file has been created
                Bitmap b = loadImageFromCacheFile(file);
                if (callback != null) callback.callback(true, b);
            }).addOnFailureListener(taskSnapshot -> {
                if (callback != null) callback.callback(false, null);
            });

        }
    }

    public static void loadImage(String filePath, boolean isThumb, FileCallback callback) {

        if (isThumb) {
            int where = filePath.lastIndexOf(".");
            filePath = filePath.substring(0, where) + "-thumb" + filePath.substring(where);
        }

        // fs- is for firebase storage caches
        String filename = "fs-"+ sha256(filePath);
        File file = new File(NaviBeeApplication.getInstance().getCacheDir(), filename);

        if (file.exists()) {
            // cache exists
            if (callback != null) callback.callback(true, file);
        } else {
            // cache not exists
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            storageRef = storageRef.child(filePath);
            storageRef.getFile(file).addOnSuccessListener(taskSnapshot -> {
                // Local temp file has been created
                if (callback != null) callback.callback(true, file);
            }).addOnFailureListener(taskSnapshot -> {
                if (callback != null) callback.callback(false, null);
            });

        }
    }

    public static File loadImageWithFile(String filePath, boolean isThumb) {

        if (isThumb) {
            int where = filePath.lastIndexOf(".");
            filePath = filePath.substring(0, where) + "-thumb" + filePath.substring(where);
        }

        // fs- is for firebase storage caches
        String filename = "fs-"+ sha256(filePath);
        File file = new File(NaviBeeApplication.getInstance().getCacheDir(), filename);

        if (file.exists()) {
            return file;
        } else {
            // cache not exists
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            storageRef = storageRef.child(filePath);
            storageRef.getFile(file).addOnSuccessListener(taskSnapshot -> {
                // Local temp file has been created
                Bitmap b = loadImageFromCacheFile(file);
            }).addOnFailureListener(taskSnapshot -> {
            });
            return file;
        }
    }

    public interface Callback {
        void callback(boolean isSuccess);
    }

    public interface PayloadCallback {
        void callback(boolean isSuccess, Bitmap bitmap);
    }

    public interface FileCallback {
        void callback(boolean isSuccess, File file);
    }

    private static String sha256(String url) {
        String sha256 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-256");
            crypt.reset();
            crypt.update(url.getBytes("UTF-8"));
            sha256 = byteToHex(crypt.digest());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sha256;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static void loadImageFromCacheFile(ImageView imageView, File file, String tag) {
        if (!(imageView.getTag() instanceof String)) return;
        if (!imageView.getTag().equals(tag)) return;

        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Bitmap loadImageFromCacheFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
