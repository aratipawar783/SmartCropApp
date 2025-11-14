package com.example.smartcropapp.userprofile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.smartcropapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class OTPActivity extends AppCompatActivity {

    TextInputEditText etOtp;
    MaterialButton btnVerifyOtp, btnResendOtp;
    TextView tvCountdown;

    String phone, sentOtp;
    CountDownTimer countDownTimer;

    private BroadcastReceiver otpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receivedOtp = intent.getStringExtra("otp");
            if (receivedOtp != null) {
                etOtp.setText(receivedOtp);
                Toast.makeText(OTPActivity.this, "OTP auto-filled", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        Toolbar toolbar = findViewById(R.id.toolbar_otp);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Verify OTP");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        tvCountdown = findViewById(R.id.tvCountdown);

        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        sentOtp = intent.getStringExtra("otp");

        btnVerifyOtp.setOnClickListener(v -> {
            String enteredOtp = etOtp.getText().toString().trim();

            if (enteredOtp.isEmpty()) {
                Toast.makeText(OTPActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!enteredOtp.equals(sentOtp)) {
                Toast.makeText(OTPActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            // OTP is valid, launch PasswordResetActivity
            Intent resetIntent = new Intent(OTPActivity.this, PasswordResetActivity.class);
            resetIntent.putExtra("phone", phone);
            startActivity(resetIntent);
            finish();
        });

        btnResendOtp.setOnClickListener(v -> {
            // Disable button and start countdown
            btnResendOtp.setEnabled(false);
            startCountdown();

            // You should implement sending a new OTP here, for example:
            resendOtpToUser();
        });

        // Start countdown on activity start to limit resend spamming
        startCountdown();

        // Register broadcast receiver for OTP (you must send a broadcast with action "otp_received" and extra "otp")
        LocalBroadcastManager.getInstance(this).registerReceiver(otpReceiver, new IntentFilter("otp_received"));
    }

    private void startCountdown() {
        tvCountdown.setVisibility(TextView.VISIBLE);

        countDownTimer = new CountDownTimer(30000, 1000) { // 30 seconds countdown
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText("Resend available in " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("");
                btnResendOtp.setEnabled(true);
            }
        }.start();
    }

    private void resendOtpToUser() {
        // TODO: Implement the logic to resend the OTP here.
        // Example: Generate new OTP, send SMS via SmsManager, update sentOtp variable
        // For now, let's simulate:
        sentOtp = String.valueOf(100000 + (int)(Math.random() * 900000)); // new random 6-digit OTP

        Toast.makeText(this, "New OTP sent: " + sentOtp, Toast.LENGTH_SHORT).show();

        // You might want to send the new OTP to your SMS receiver/broadcast here so auto-fill works
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(otpReceiver);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
