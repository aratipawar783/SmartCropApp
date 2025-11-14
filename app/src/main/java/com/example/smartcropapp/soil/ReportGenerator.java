package com.example.smartcropapp.soil;

public class ReportGenerator {

    public String generateReport(String label, float confidence) {
        StringBuilder sb = new StringBuilder();
        sb.append("Soil Type Detected: ").append(label).append("\n");
        sb.append(String.format("Model Confidence: %.2f%%\n\n", confidence * 100));

        String[] lines = new String[]{
                "1. This soil sample appears to be " + label + ".",
                "2. Confidence level indicates the model is " + String.format("%.1f%%", confidence*100) + " sure.",
                "3. Texture: " + suggestTexture(label),
                "4. Drainage: " + suggestDrainage(label),
                "5. Suggested pH range: " + suggestPH(label),
                "6. Organic matter recommendation: " + suggestOrganicMatter(label),
                "7. Fertilizer suggestion: " + suggestFertilizer(label),
                "8. Watering frequency guidance: " + suggestWatering(label),
                "9. Crop suitability: " + suggestCrops(label),
                "10. Tillage recommendation: " + suggestTillage(label),
                "11. Mulching advice: " + suggestMulch(label),
                "12. Erosion control tip: " + suggestErosionControl(label),
                "13. Soil test recommended: Conduct complete nutrient and pH test once per year.",
                "14. If confidence is low (<60%), collect additional samples for accuracy.",
                "15. Store soil samples in dry, clean containers to avoid contamination.",
                "16. For improving soil structure: consider cover crops and organic compost.",
                "17. For micro-nutrient deficiencies, use targeted foliar sprays after testing.",
                "18. Avoid over-compaction by limiting heavy machinery on wet soil.",
                "19. Rotational cropping is recommended to maintain fertility.",
                "20. For personalized advisory, share laboratory test results with an agronomist."
        };

        for (String l : lines) {
            sb.append(l).append("\n");
        }
        return sb.toString();
    }

    private String suggestTexture(String label) {
        if (label.toLowerCase().contains("sandy")) return "Sandy — coarse particles, quick drainage.";
        if (label.toLowerCase().contains("clay")) return "Clay — fine particles, holds water.";
        if (label.toLowerCase().contains("loam")) return "Loamy — balanced texture, ideal.";
        return "Mixed/unknown texture.";
    }
    private String suggestDrainage(String label) {
        if (label.toLowerCase().contains("sandy")) return "Good drainage — may need frequent watering.";
        if (label.toLowerCase().contains("clay")) return "Poor drainage — monitor waterlogging.";
        return "Moderate drainage.";
    }
    private String suggestPH(String label) {
        return "6.0 - 7.5 (general). Test to confirm.";
    }
    private String suggestOrganicMatter(String label) {
        return "Add 2-4% organic matter annually for better structure.";
    }
    private String suggestFertilizer(String label) {
        return "Balanced NPK (10-10-10) or based on soil test.";
    }
    private String suggestWatering(String label) {
        if (label.toLowerCase().contains("sandy")) return "Frequent but light watering.";
        if (label.toLowerCase().contains("clay")) return "Less frequent, allow drainage between cycles.";
        return "Water based on crop and weather.";
    }
    private String suggestCrops(String label) {
        if (label.toLowerCase().contains("clay")) return "Rice, certain legumes (with proper drainage).";
        if (label.toLowerCase().contains("sandy")) return "Root vegetables, carrots, potatoes with irrigation.";
        return "Vegetables, cereals — depends on fertility.";
    }
    private String suggestTillage(String label) { return "Conservation tillage recommended where possible."; }
    private String suggestMulch(String label) { return "Use organic mulch to conserve moisture and add OM."; }
    private String suggestErosionControl(String label) { return "Use contour planting and cover crops on slopes."; }
}
