package com.example.smartcropapp.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.smartcropapp.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherAlertsService extends Service {

    private static final String CHANNEL_ID = "REAL_WEATHER_ALERTS";
    private static final String API_KEY = "YOUR_API_KEY_HERE"; // 🔑 Replace with your OpenWeatherMap key
    private static final String CITY = "Mumbai,IN"; // change to your city or dynamic location
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        handler.post(fetchWeatherRunnable);
    }

    private final Runnable fetchWeatherRunnable = new Runnable() {
        @Override
        public void run() {
            fetchWeatherData();
            handler.postDelayed(this, 1000 * 60 * 60 * 3); // every 3 hours
        }
    };

    private void fetchWeatherData() {
        new Thread(() -> {
            try {
                String urlString = "https://api.openweathermap.org/data/2.5/weather?q="
                        + CITY + "&appid=" + API_KEY + "&units=metric";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONObject main = json.getJSONObject("main");

                double temp = main.getDouble("temp");
                int humidity = main.getInt("humidity");
                String weather = json.getJSONArray("weather").getJSONObject(0).getString("description");

                analyzeAndNotify(temp, humidity, weather);

            } catch (Exception e) {
                Log.e("WeatherAlertService", "Error fetching weather: " + e.getMessage());
            }
        }).start();
    }

    private void analyzeAndNotify(double temp, int humidity, String weather) {
        String alertMsg = null;

        if (humidity > 85) {
            alertMsg = "🌫 High humidity (" + humidity + "%). Risk of fungal disease. Dry storage areas!";
        } else if (weather.contains("rain")) {
            alertMsg = "🌧 Rain Alert! Possible flooding — ensure proper drainage.";
        } else if (temp > 35) {
            alertMsg = "🔥 High temperature (" + temp + "°C). Risk of crop dehydration!";
        } else if (temp < 15) {
            alertMsg = "🥶 Low temperature (" + temp + "°C). Risk for tropical crops.";
        }

        if (alertMsg != null)
            showNotification("Weather Alert", alertMsg);
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notify)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Weather and Crop Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Real-time alerts for weather and crop risks");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(fetchWeatherRunnable);
        super.onDestroy();
    }
}
