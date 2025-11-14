package com.example.smartcropapp.soil;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcropapp.R;

public class SoilReportActivity extends AppCompatActivity {

    private String[] reportLabels = {
            "Soil pH", "Moisture Level", "Nitrogen (N)", "Phosphorus (P)",
            "Potassium (K)", "Organic Carbon", "Sulphur", "Calcium",
            "Magnesium", "Iron", "Zinc", "Manganese",
            "Boron", "Salinity", "Overall Recommendation"
    };

    private String[] reportValues = {
            "6.5 (Slightly acidic)", "Moderate (45%)", "Low", "Optimal",
            "Deficient", "Good", "Slightly Low", "Adequate",
            "Adequate", "Deficient", "Low", "Adequate",
            "Deficient", "Normal", "Add compost + NPK fertilizer mix"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_report);

        ImageView imageView = findViewById(R.id.ivSoilReport);
        TableLayout tableLayout = findViewById(R.id.tableReport);

        // Show the uploaded soil image
        String imgUri = getIntent().getStringExtra("soilImage");
        if (imgUri != null) {
            imageView.setImageURI(Uri.parse(imgUri));
        }

        // Generate 15 rows of report
        for (int i = 0; i < reportLabels.length; i++) {
            TableRow row = new TableRow(this);

            TextView col1 = new TextView(this);
            col1.setText(reportLabels[i]);
            col1.setPadding(10, 10, 30, 10);

            TextView col2 = new TextView(this);
            col2.setText(reportValues[i]);
            col2.setPadding(10, 10, 10, 10);

            row.addView(col1);
            row.addView(col2);

            tableLayout.addView(row);
        }
    }
}
