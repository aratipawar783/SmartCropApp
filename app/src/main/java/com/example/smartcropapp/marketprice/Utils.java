package com.example.smartcropapp.marketprice;

import android.content.Context;

import java.io.InputStream;

public class Utils {
    public static String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Exception ex) { ex.printStackTrace(); }
        return json;
    }
}
