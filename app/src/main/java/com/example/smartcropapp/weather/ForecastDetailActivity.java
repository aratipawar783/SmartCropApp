package com.example.smartcropapp.weather;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcropapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ForecastDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_detail);

        LineChart lineChart = findViewById(R.id.lineChart);

        String date = getIntent().getStringExtra("date");
        String temp = getIntent().getStringExtra("temp");

        float baseTemp = Float.parseFloat(temp);
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, baseTemp - 2));  // Morning
        entries.add(new Entry(1, baseTemp));      // Afternoon
        entries.add(new Entry(2, baseTemp + 2));  // Evening
        entries.add(new Entry(3, baseTemp - 1));  // Night

        LineDataSet dataSet = new LineDataSet(entries, "Temp (" + date + ")");
        dataSet.setColor(getResources().getColor(R.color.teal_700));
        dataSet.setCircleColor(getResources().getColor(R.color.purple_700));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(12f);

        lineChart.setData(new LineData(dataSet));
        lineChart.getDescription().setText("Daily temperature changes");
        lineChart.invalidate();
    }
}
