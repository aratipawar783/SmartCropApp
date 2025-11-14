package com.example.smartcropapp.soil;

import java.util.HashMap;
import java.util.Map;

public class Recommendations {
    public static Map<String, String[]> cropMap = new HashMap<>();
    public static Map<String, String> soilInfo = new HashMap<>();

    static {
        cropMap.put("Clay", new String[]{"Rice", "Wheat", "Pulses"});
        cropMap.put("Sandy", new String[]{"Groundnut", "Potato", "Watermelon"});
        cropMap.put("Loamy", new String[]{"Sugarcane", "Cotton", "Vegetables"});
        cropMap.put("Black", new String[]{"Cotton", "Soybean", "Maize"});

        soilInfo.put("Clay", "Clay soil has fine particles, retains water, and is nutrient-rich but poorly aerated.");
        soilInfo.put("Sandy", "Sandy soil has large particles, drains quickly, and needs organic matter for fertility.");
        soilInfo.put("Loamy", "Loamy soil is a balanced mixture, fertile, and ideal for most crops.");
        soilInfo.put("Black", "Black soil is rich in minerals like lime, calcium, magnesium; ideal for cotton and soybean.");
    }

    public static String[] getCrops(String soilType) {
        return cropMap.getOrDefault(soilType, new String[]{"No recommendations"});
    }

    public static String getSoilInfo(String soilType) {
        return soilInfo.getOrDefault(soilType, "No info available");
    }
}
