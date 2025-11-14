package com.example.smartcropapp.userprofile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smartcropapp.DBHelper;
import com.example.smartcropapp.R;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    Spinner spinnerCountryCode;
    EditText etPhone;
    Button btnSendOtp;
    String generatedOtp;
    DBHelper dbHelper;
    final int SMS_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar toolbar = findViewById(R.id.toolbar_forgot);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Forgot Password");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);
        etPhone = findViewById(R.id.etPhone);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        dbHelper = new DBHelper(this);

        spinnerCountryCode.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"+91", "+1", "+44", "+971", "+61"}
        ));

        btnSendOtp.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_CODE);
                return;
            }

            String cc = spinnerCountryCode.getSelectedItem().toString();
            String local = etPhone.getText().toString().trim();
            if (local.isEmpty() || local.length() < 6) {
                Toast.makeText(this, "Enter valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Remove + from country code and concat with phone input
            String fullNumber = (cc.startsWith("+") ? cc.substring(1) : cc) + local;

            if (!dbHelper.checkUserExistsByPhone(fullNumber)) {
                Toast.makeText(this, "Phone not registered", Toast.LENGTH_SHORT).show();
                return;
            }

            sendOtpViaSms(fullNumber);
        });
    }

    private void sendOtpViaSms(String phone) {
        generatedOtp = String.valueOf(100000 + new Random().nextInt(900000));
        String msg = "Your OTP for password reset is: " + generatedOtp;

        try {
            SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null);
            Toast.makeText(this, "OTP sent", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, OTPActivity.class);
            i.putExtra("phone", phone);
            i.putExtra("otp", generatedOtp);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] res) {
        if (req == SMS_PERMISSION_CODE && res.length > 0
                && res[0] == PackageManager.PERMISSION_GRANTED) {
            btnSendOtp.performClick();
        } else {
            Toast.makeText(this, "SMS permission required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
