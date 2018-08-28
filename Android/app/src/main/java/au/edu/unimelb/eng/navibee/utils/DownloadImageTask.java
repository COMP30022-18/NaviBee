package au.edu.unimelb.eng.navibee.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;

import au.edu.unimelb.eng.navibee.NaviBeeApplication;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
//        String urldisplay = urls[0];
//        Bitmap bitmap = null;
//        String hashedUrl = hashUrl(urldisplay);
//        String[] fileNames = NaviBeeApplication.getInstance().fileList();
//
//        if (!Arrays.asList(fileNames).contains(hashedUrl)) {
//            System.out.println("not found");
//            try {
//                File file = new File(NaviBeeApplication.getInstance().getFilesDir(), hashedUrl);
//                InputStream input = new java.net.URL(urldisplay).openStream();
//                bitmap = BitmapFactory.decodeStream(input);
//                OutputStream outputStream = new FileOutputStream(file);
//                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
//                outputStream.close();
//            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("loading");
//            try {
//                FileInputStream inputStream = NaviBeeApplication.getInstance().openFileInput(hashedUrl);
//                bitmap = BitmapFactory.decodeStream(inputStream);
//            } catch (FileNotFoundException e) { }
//        }

//        File file = new File(NaviBeeApplication.getInstance().getCacheDir(), hashedUrl);
//        if (!file.exists()) {
//            System.out.println("not found");
//            try {
//                file.createNewFile();
//                InputStream input = new java.net.URL(urldisplay).openStream();
//                bitmap = BitmapFactory.decodeStream(input);
//                OutputStream outputStream = new FileOutputStream(file);
//                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
//                outputStream.close();
//            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("loading");
//            try {
//                FileInputStream inputStream = NaviBeeApplication.getInstance().openFileInput(hashedUrl);
//                bitmap = BitmapFactory.decodeStream(inputStream);
//            } catch (FileNotFoundException e) { }
//        }

//        if (!Arrays.asList(fileNames).contains(hashedUrl)) {
//            System.out.println("not found");
//            try {
//                File file = new File(NaviBeeApplication.getInstance().getFilesDir(), hashedUrl);
//                InputStream inputStream = new java.net.URL(urldisplay).openStream();
//                byte[] buffer = new byte[2000000];
//                inputStream.read(buffer);
//                OutputStream outputStream = new FileOutputStream(file);
//                outputStream.write(buffer);
//                outputStream.close();
//            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//        }
//        try {
//            FileInputStream inputStream = NaviBeeApplication.getInstance().openFileInput(hashedUrl);
//            bitmap = BitmapFactory.decodeStream(inputStream);
//        } catch (FileNotFoundException e) { }

        String urldisplay = urls[0];
        Bitmap bitmap = null;
        String hashedUrl = hashUrl(urldisplay);
        File file = new File(NaviBeeApplication.getInstance().getCacheDir(), hashedUrl);
        if (!file.exists()) {
            System.out.println("not found");
            try {
                file.createTempFile(hashedUrl, null, NaviBeeApplication.getInstance().getCacheDir());
                InputStream inputStream = new java.net.URL(urldisplay).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
//                byte[] buffer = new byte[2000000];
//                inputStream.read(buffer);
                OutputStream outputStream = new FileOutputStream(file);
//                outputStream.write(buffer);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
                outputStream.close();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        }
        try {
            System.out.println("loading from file");
            FileInputStream fileInputStream = NaviBeeApplication.getInstance().openFileInput(hashedUrl);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
        } catch (FileNotFoundException e) {}
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
    private static String hashUrl(String url){
        String sha256 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-256");
            crypt.reset();
            crypt.update(url.getBytes("UTF-8"));
            sha256 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sha256;
    }

    private static String byteToHex(final byte[] hash){
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}

