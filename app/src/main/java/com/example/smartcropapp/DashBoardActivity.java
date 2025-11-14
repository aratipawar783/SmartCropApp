package com.example.smartcropapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.smartcropapp.feedback.FeedbackActivity;
import com.example.smartcropapp.marketprice.MarketActivity;
import com.example.smartcropapp.merchant.Feed1Activity;
import com.example.smartcropapp.notifications.NotificationsActivity;
import com.example.smartcropapp.pest.PestDetectionActivity;
import com.example.smartcropapp.soil.SoilActivity;
import com.example.smartcropapp.userprofile.UserActivity;
import com.example.smartcropapp.weather.WeatherActivity;

public class DashBoardActivity extends AppCompatActivity implements View.OnClickListener {

    CardView cardProfile, cardSoil, cardWeather, cardPest, cardMarket, cardNotification, cardFeedback, cdMerchant;
    ImageView btnProfileLogo;
    ImageButton imgbtn;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        dbHelper = new DBHelper(this);

        // Set Toolbar as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Smart Crop App");


        // Profile logo button
        btnProfileLogo = findViewById(R.id.btnProfilelogo);
        btnProfileLogo.setOnClickListener(v -> {
            Intent intent = new Intent(DashBoardActivity.this, UserActivity.class);
            startActivity(intent);
        });

        // Initialize Cards

        cardSoil = findViewById(R.id.cardSoil);
        cardWeather = findViewById(R.id.cardWeather);
        cardPest = findViewById(R.id.cardPest);
        cardMarket = findViewById(R.id.cardMarket);
        imgbtn = findViewById(R.id.imgbtn);
        cardFeedback = findViewById(R.id.cardFeedback);
        cdMerchant = findViewById(R.id.cardFarmMerchant);

        // Set Listeners
        cdMerchant.setOnClickListener(this);
        cardFeedback.setOnClickListener(this);
        cardMarket.setOnClickListener(this);
        cardSoil.setOnClickListener(this);
        cardWeather.setOnClickListener(this);
        cardPest.setOnClickListener(this);
        imgbtn.setOnClickListener(this);

        // Load profile image
        loadProfileLogo();
    }

    private void loadProfileLogo() {
        Cursor cursor = dbHelper.getUserProfile();
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String photo = cursor.getString(cursor.getColumnIndex("photo"));
            if (photo != null && !photo.isEmpty()) {
                btnProfileLogo.setImageURI(Uri.parse(photo));
            }
        }
        if (cursor != null) cursor.close();
    }

    // Inflate menu into ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    // Handle ActionBar item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.cardFarmMerchant) {
            startActivity(new Intent(this, Feed1Activity.class));
        } else if (view.getId() == R.id.cardFeedback) {
            startActivity(new Intent(this, FeedbackActivity.class));
        }
         else if (view.getId() == R.id.cardMarket) {
            startActivity(new Intent(this, MarketActivity.class));
        } else if (view.getId() == R.id.cardPest) {
            Intent intent=new Intent(DashBoardActivity.this, PestDetectionActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.imgbtn) {
           Intent intent=new Intent(DashBoardActivity.this, NotificationsActivity.class);
           startActivity(intent);
        } else if (view.getId() == R.id.cardWeather) {
            startActivity(new Intent(this, WeatherActivity.class));
        } else if (view.getId() == R.id.cardSoil) {
            startActivity(new Intent(this, SoilActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile image when returning from UserActivity
        loadProfileLogo();
    }
}
