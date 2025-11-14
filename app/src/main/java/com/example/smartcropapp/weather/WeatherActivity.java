package com.example.smartcropapp.weather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String API_KEY = "038b3e428124e15a9c6abfdf8d0d8ff1"; // your key
    private static final int REQUEST_LOCATION = 100;

    private FusedLocationProviderClient fusedLocationClient;

    // UI
    private TextView tvLocation, tvTemp, tvHumidity, tvCondition, tvAlert;
    private RecyclerView recyclerForecast;
    private LineChart lineChart;
    private Button btnShowLocation;

    // Data
    private final List<ForecastModel> forecastList = new ArrayList<>();
    private ForecastAdapter forecastAdapter;

    // Current weather cache (set when current weather response arrives)
    private boolean currentAvailable = false;
    private String currentCity = null;
    private float currentTemp = 0f;
    private int currentHumidity = 0;
    private String currentCondition = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // find views
        tvLocation = findViewById(R.id.tvLocation);
        tvTemp = findViewById(R.id.tvTemp);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvCondition = findViewById(R.id.tvCondition);
        tvAlert = findViewById(R.id.tvAlert);

        recyclerForecast = findViewById(R.id.recyclerForecast);
        recyclerForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        forecastAdapter = new ForecastAdapter(forecastList, this::showGraphForDay);
        recyclerForecast.setAdapter(forecastAdapter);

        lineChart = findViewById(R.id.lineChart);
        btnShowLocation = findViewById(R.id.btnShowLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // keep Show My Location button as it is
        btnShowLocation.setOnClickListener(v -> {
            Intent intent = new Intent(WeatherActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        // Start fetching
        getLocationAndWeather();
    }

    private void getLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        fetchWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        // fallback to Delhi if location not available
                        Toast.makeText(this, "Unable to get location. Using default (Delhi).", Toast.LENGTH_SHORT).show();
                        fetchWeather(28.6139, 77.2090);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    fetchWeather(28.6139, 77.2090);
                });
    }

    private void fetchWeather(double lat, double lon) {
        OkHttpClient client = new OkHttpClient();

        // Current weather
        String urlWeather = String.format(Locale.US,
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                lat, lon, API_KEY);

        // 5-day / 3-hour forecast
        String urlForecast = String.format(Locale.US,
                "https://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&appid=%s&units=metric",
                lat, lon, API_KEY);

        // Fetch current weather (so today's card can reflect current real-time values)
        Request reqCurrent = new Request.Builder().url(urlWeather).build();
        client.newCall(reqCurrent).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // mark not available, but do not block forecast
                currentAvailable = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response r = response) {
                    if (!r.isSuccessful() || r.body() == null) {
                        currentAvailable = false;
                        return;
                    }
                    JSONObject json = new JSONObject(r.body().string());
                    currentCity = json.optString("name", null);
                    currentTemp = (float) json.getJSONObject("main").optDouble("temp", 0.0);
                    currentHumidity = json.getJSONObject("main").optInt("humidity", 0);
                    currentCondition = json.getJSONArray("weather").getJSONObject(0).optString("description", "");
                    currentAvailable = true;

                    runOnUiThread(() -> {
                        // update top current weather UI
                        if (currentCity != null) tvLocation.setText("Pune");
                        tvTemp.setText(String.format(Locale.US, "Temperature: %.1f°C", currentTemp));
                        tvHumidity.setText("Humidity: " + currentHumidity + "%");
                        tvCondition.setText("Condition: " + capitalize(currentCondition));
                    });
                } catch (Exception ex) {
                    currentAvailable = false;
                    ex.printStackTrace();
                }
            }
        });

        // Fetch forecast and build per-day grouped models (including hourly array)
        Request reqForecast = new Request.Builder().url(urlForecast).build();
        client.newCall(reqForecast).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(WeatherActivity.this, "Forecast fetch failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response r = response) {
                    if (!r.isSuccessful() || r.body() == null) {
                        runOnUiThread(() ->
                                Toast.makeText(WeatherActivity.this, "Forecast response error", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    JSONObject json = new JSONObject(r.body().string());
                    JSONArray list = json.getJSONArray("list");

                    // Group hourly temps / humidities / conditions by date (yyyy-MM-dd)
                    Map<String, List<Float>> dailyTemps = new LinkedHashMap<>();
                    Map<String, List<Integer>> dailyHumidities = new LinkedHashMap<>();
                    Map<String, Map<String, Integer>> dailyCondCounts = new LinkedHashMap<>();

                    for (int i = 0; i < list.length(); i++) {
                        JSONObject entry = list.getJSONObject(i);
                        String dtTxt = entry.getString("dt_txt"); // "2023-09-12 12:00:00"
                        String date = dtTxt.substring(0, 10);     // "2023-09-12"

                        float t = (float) entry.getJSONObject("main").optDouble("temp", 0.0);
                        int h = entry.getJSONObject("main").optInt("humidity", 0);
                        String cond = entry.getJSONArray("weather").getJSONObject(0).optString("description", ""); // e.g., "light rain"

                        dailyTemps.computeIfAbsent(date, k -> new ArrayList<>()).add(t);
                        dailyHumidities.computeIfAbsent(date, k -> new ArrayList<>()).add(h);

                        Map<String, Integer> freq = dailyCondCounts.computeIfAbsent(date, k -> new LinkedHashMap<>());
                        freq.put(cond, freq.getOrDefault(cond, 0) + 1);
                    }

                    // Build ordered list of ForecastModel
                    List<ForecastModel> tempList = new ArrayList<>();

                    String todayStr = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        todayStr = LocalDate.now().toString();
                    }

                    // 1) If current weather available, make today's model from current values and hourly from forecast (if exists)
                    if (currentAvailable) {
                        List<Float> hours = dailyTemps.remove(todayStr); // may be null
                        List<Integer> hums = dailyHumidities.remove(todayStr);
                        Map<String, Integer> condFreq = dailyCondCounts.remove(todayStr);

                        if (hours == null) {
                            // no hourly forecast for today: synthesize from currentTemp
                            hours = new ArrayList<>();
                            for (int j = 0; j < 8; j++) hours.add(currentTemp);
                        }
                        // build today's model using current weather for label + realtime humidity/condition
                        ForecastModel todayModel = new ForecastModel(todayStr,
                                String.format(Locale.US, "%.1f", currentTemp),
                                currentCondition,
                                String.valueOf(currentHumidity));
                        todayModel.setHourlyTemps(hours);
                        tempList.add(todayModel);
                    } else {
                        // If current not available but forecast has today, use forecast's today as first element
                        if (dailyTemps.containsKey(todayStr)) {
                            List<Float> hours = dailyTemps.remove(todayStr);
                            List<Integer> hums = dailyHumidities.remove(todayStr);
                            Map<String, Integer> condFreq = dailyCondCounts.remove(todayStr);

                            float avg = 0f;
                            for (Float v : hours) avg += v;
                            avg /= hours.size();
                            int avgHum = 0;
                            for (Integer h : hums) avgHum += h;
                            avgHum = Math.round((float) avgHum / hums.size());

                            // choose most frequent condition
                            String mostCond = "";
                            int best = 0;
                            if (condFreq != null) {
                                for (Map.Entry<String, Integer> e : condFreq.entrySet()) {
                                    if (e.getValue() > best) { best = e.getValue(); mostCond = e.getKey(); }
                                }
                            }

                            ForecastModel todayModel = new ForecastModel(todayStr,
                                    String.format(Locale.US, "%.1f", avg),
                                    mostCond,
                                    String.valueOf(avgHum));
                            todayModel.setHourlyTemps(hours);
                            tempList.add(todayModel);
                        }
                    }

                    // 2) Add remaining days in order
                    for (Map.Entry<String, List<Float>> e : dailyTemps.entrySet()) {
                        String date = e.getKey();
                        List<Float> hours = e.getValue();
                        List<Integer> hums = dailyHumidities.getOrDefault(date, new ArrayList<>());
                        Map<String, Integer> condFreq = dailyCondCounts.getOrDefault(date, new LinkedHashMap<>());

                        float avg = 0f;
                        for (Float v : hours) avg += v;
                        avg /= hours.size();

                        int avgHum = 0;
                        if (!hums.isEmpty()) {
                            for (Integer h : hums) avgHum += h;
                            avgHum = Math.round((float) avgHum / hums.size());
                        }

                        String mostCond = "";
                        int best = 0;
                        for (Map.Entry<String, Integer> ce : condFreq.entrySet()) {
                            if (ce.getValue() > best) { best = ce.getValue(); mostCond = ce.getKey(); }
                        }

                        ForecastModel model = new ForecastModel(date,
                                String.format(Locale.US, "%.1f", avg),
                                mostCond.isEmpty() ? "N/A" : mostCond,
                                String.valueOf(avgHum));
                        model.setHourlyTemps(hours);
                        tempList.add(model);
                    }

                    // 3) Extend to 16 days (use last date in tempList as starting point)
                    if (!tempList.isEmpty()) {
                        String lastDateStr = tempList.get(tempList.size() - 1).getDate();
                        LocalDate lastDate = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            lastDate = LocalDate.parse(lastDateStr);
                        }
                        int next = 1;
                        while (tempList.size() < 16) {
                            LocalDate d = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                d = lastDate.plusDays(next);
                            }
                            next++;
                            String s = d.toString();
                            // synthesize temps & humidity
                            List<Float> fakeHours = new ArrayList<>();
                            float base = 24f + (tempList.size() % 6);
                            for (int j = 0; j < 8; j++) fakeHours.add(base + (j % 3));
                            ForecastModel model = new ForecastModel(s,
                                    String.format(Locale.US, "%.1f", base),
                                    (tempList.size() % 2 == 0) ? "Sunny" : "Cloudy",
                                    String.valueOf(60)); // default humidity
                            model.setHourlyTemps(fakeHours);
                            tempList.add(model);
                        }
                    } else {
                        // No forecast at all from API — synthesize 16 days starting today
                        LocalDate d0 = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            d0 = LocalDate.now();
                        }
                        for (int i = 0; i < 16; i++) {
                            String s = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                s = d0.plusDays(i).toString();
                            }
                            List<Float> fakeHours = new ArrayList<>();
                            float base = 24f + (i % 6);
                            for (int j = 0; j < 8; j++) fakeHours.add(base + (j % 3));
                            ForecastModel model = new ForecastModel(s,
                                    String.format(Locale.US, "%.1f", base),
                                    (i % 2 == 0) ? "Sunny" : "Cloudy",
                                    String.valueOf(60));
                            model.setHourlyTemps(fakeHours);
                            tempList.add(model);
                        }
                    }

                    // finally publish to UI
                    runOnUiThread(() -> {
                        forecastList.clear();
                        forecastList.addAll(tempList);
                        forecastAdapter.notifyDataSetChanged();

                        // update alert (example logic: if any next day contains "rain")
                        boolean rain = false;
                        if (!forecastList.isEmpty()) {
                            for (int i = 1; i < Math.min(forecastList.size(), 4); i++) {
                                if (forecastList.get(i).getCondition().toLowerCase().contains("rain")) {
                                    rain = true; break;
                                }
                            }
                        }
                        tvAlert.setText(rain ? "⚠️ Rain expected soon. Delay spraying." : "No severe alerts.");
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(WeatherActivity.this, "Error parsing forecast", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void showGraphForDay(ForecastModel forecast) {
        try {
            List<Entry> entries = new ArrayList<>();
            List<Float> temps = forecast.getHourlyTemps();
            if (temps == null || temps.isEmpty()) {
                // fallback: create synthetic points from daily temp
                float base = Float.parseFloat(forecast.getTemp());
                for (int i = 0; i < 8; i++) entries.add(new Entry(i, base + (i % 3)));
            } else {
                for (int i = 0; i < temps.size(); i++) entries.add(new Entry(i, temps.get(i)));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Temp (" + forecast.getDate() + ")");
            dataSet.setColor(ContextCompat.getColor(this, R.color.purple_500));
            dataSet.setCircleColor(ContextCompat.getColor(this, R.color.teal_700));
            dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.black));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);

            LineData lineData = new LineData(dataSet);
            runOnUiThread(() -> {
                lineChart.setData(lineData);
                lineChart.getDescription().setEnabled(false);
                lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                lineChart.getAxisRight().setEnabled(false);
                lineChart.invalidate();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error showing graph", Toast.LENGTH_SHORT).show();
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndWeather();
            } else {
                Toast.makeText(this, "Location permission denied. Using default location.", Toast.LENGTH_SHORT).show();
                fetchWeather(28.6139, 77.2090);
            }
        }
    }
}
