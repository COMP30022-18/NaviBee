package au.edu.unimelb.eng.navibee.navigation;

import android.graphics.Color;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

public class ColorAdapter {
    @FromJson
    @HexColor int fromJson(String color) {
        return Color.parseColor(color);
    }

    @ToJson
    String toJson(@HexColor int value) {
        return "#" + Integer.toHexString(value);
    }
}
