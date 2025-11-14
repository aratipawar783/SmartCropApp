package com.example.smartcropapp.userprofile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        if (data != null) {
            Object[] pdus = (Object[]) data.get("pdus");
            if (pdus == null) return;

            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                String messageBody = smsMessage.getMessageBody();

                // Assuming OTP format: "Your OTP for password reset is: 123456"
                // Extract the 6-digit OTP using regex
                String otp = extractOtp(messageBody);
                if (otp != null) {
                    // Send OTP to OTPActivity via LocalBroadcast
                    Intent otpIntent = new Intent("otp_received");
                    otpIntent.putExtra("otp", otp);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(otpIntent);

                    // Optional: Show a toast or notification
                    Toast.makeText(context, "OTP Received: " + otp, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String extractOtp(String message) {
        // Regex to find 6-digit code in message
        String pattern = "\\b\\d{6}\\b";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(message);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }
}
