package com.example.smartcropapp.soil;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    /**
     * Loads labels from a text file located in the assets folder.
     * Each line of the file corresponds to one label.
     *
     * @param context  The application context
     * @param fileName The file name in assets (e.g., "labels.txt")
     * @return A list of labels
     * @throws IOException if file not found or cannot be read
     */
    public static List<String> loadLabels(Context context, String fileName) throws IOException {
        List<String> labels = new ArrayList<>();
        InputStream is = context.getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line.trim());
        }

        reader.close();
        return labels;
    }
}
