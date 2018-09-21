package au.edu.unimelb.eng.navibee.utils;

import android.widget.ImageView;

public class NetworkImageHelper {

    public static void loadImage(ImageView imageView, String url) {
        new URLImageViewCacheLoader(url, imageView).execute();
    }

}
