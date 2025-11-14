package com.example.smartcropapp.feedback;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcropapp.FeedbackDBHelper;
import com.example.smartcropapp.R;

public class ViewFeedbackActivity extends AppCompatActivity {

    FeedbackDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedback);

        dbHelper = new FeedbackDBHelper(this);
        LinearLayout container = findViewById(R.id.feedbackContainer1);

        Cursor cursor = dbHelper.getAllFeedbacks();
        while (cursor.moveToNext()) {
            String name = cursor.getString(1);
            String date = cursor.getString(2);
            float rating = cursor.getFloat(3);
            String message = cursor.getString(4);

            TextView tv = new TextView(this);
            tv.setText("👩‍🌾 " + name + "\n📅 " + date + "\n⭐ Rating: " + rating + "\n💬 " + message);
            tv.setPadding(20, 20, 20, 20);
            tv.setBackgroundResource(R.drawable.feedback_box);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 10, 0, 10);
            tv.setLayoutParams(params);
            container.addView(tv);
        }
        cursor.close();
    }
}
