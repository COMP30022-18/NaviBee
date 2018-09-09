package au.edu.unimelb.eng.navibee.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Formatter;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import au.edu.unimelb.eng.navibee.NaviBeeApplication;
import timber.log.Timber;

public class NetworkImageHelper {

    public static void loadImage(ImageView imageView, String url) {
        loadImage(imageView, url, url);
    }

    public static void loadImage(ImageView imageView, String url, String key) {
        // ni- is for network image
        String filename = "ni-" + NetworkImageHelper.sha256(key);
        File file = new File(NaviBeeApplication.getInstance().getCacheDir(), filename);

        if (file.exists()) {
            // cache exists
            loadImageFromCacheFile(imageView, file);
        } else {
            // cache not exists
            new DownloadFileFromURLAsync(url, file, imageView).execute();

        }
    }

    public static void loadRoundImage(ImageView imageView, String url) {
        String key = url;

        // ni- is for network image
        String filename = "ni-" + NetworkImageHelper.sha256(key);
        File file = new File(NaviBeeApplication.getInstance().getCacheDir(), filename);

        if (file.exists()) {
            // cache exists
            loadImageFromCacheFile(imageView, file);
        } else {
            // cache not exists
            new DownloadFileFromURLAsync(url, file, imageView, true).execute();

        }
    }

    protected static String sha256(String url) {
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

    protected static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    protected static void loadImageFromCacheFile(ImageView imageView, File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void loadRoundImageFromCacheFile(ImageView imageView, File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Resources r = imageView.getResources();
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(r, bitmap);
            drawable.setAntiAlias(true);
            drawable.setCircular(true);
            imageView.setImageDrawable(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DownloadFileFromURLAsync extends AsyncTask<Void, Void, Void> {

        private String url;
        private File file;
        private ImageView imageView;
        private boolean isRounded = false;

        private DownloadFileFromURLAsync(String url, File file, ImageView imageView) {
            this(url, file, imageView, false);
        }

        private DownloadFileFromURLAsync(String url, File file, ImageView imageView, boolean isRounded) {
            this.url = url;
            this.file = file;
            this.imageView = imageView;
            this.isRounded = isRounded;
        }

        // Downloading file in background thread
        @Override
        protected Void doInBackground(Void... params) {
            try {
                InputStream input = new java.net.URL(url).openStream();
                OutputStream output = new FileOutputStream(file);

                ByteStreams.copy(input, output);
            } catch (Exception e) {
                Timber.e(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            if (isRounded)
                loadRoundImageFromCacheFile(imageView, file);
            else
                loadImageFromCacheFile(imageView, file);
        }

    }
}
