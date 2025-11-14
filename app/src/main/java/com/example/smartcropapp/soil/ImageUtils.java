package com.example.smartcropapp.soil;

import android.graphics.Bitmap;

public class ImageUtils {

    /**
     * Very light-weight heuristic: count proportion of 'brownish' pixels.
     * Brownish defined as R > G and R > B and R not too dark.
     */
    public static boolean isLikelySoil(Bitmap bmp) {
        if (bmp == null) return false;

        // sample scale-down to speed up
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int sampleStep = Math.max(1, Math.min(w, h) / 100); // up to ~100x100 sampling
        int total = 0;
        int soilCount = 0;

        for (int y = 0; y < h; y += sampleStep) {
            for (int x = 0; x < w; x += sampleStep) {
                int p = bmp.getPixel(x, y);
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = p & 0xFF;

                total++;
                // simple brownish test
                boolean brownish = (r > g + 8) && (r > b + 8) && (r > 80);
                if (brownish) soilCount++;
            }
        }

        float ratio = total == 0 ? 0f : ((float) soilCount / (float) total);
        // require at least ~20-25% brownish pixels to consider it soil
        return ratio >= 0.20f;
    }
}
