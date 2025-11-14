package com.example.smartcropapp.feedback;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcropapp.FeedbackDBHelper;
import com.example.smartcropapp.R;
import java.util.Calendar;

public class FeedbackActivity extends AppCompatActivity {

    EditText etName, etMessage;
    TextView tvDate;
    RatingBar ratingBar;
    Button btnSubmit, btnView;
    FeedbackDBHelper dbHelper;
    String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        etName = findViewById(R.id.etFarmerName);
        etMessage = findViewById(R.id.etFeedbackMessage);
        tvDate = findViewById(R.id.tvDate);
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmit = findViewById(R.id.btnSubmitFeedback);
        btnView = findViewById(R.id.btnViewFeedbacks);
        dbHelper = new FeedbackDBHelper(this);

        tvDate.setOnClickListener(v -> showDatePicker());

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String message = etMessage.getText().toString();
            float rating = ratingBar.getRating();

            if (name.isEmpty() || selectedDate.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = dbHelper.insertFeedback(name, selectedDate, rating, message);
            if (inserted) {
                Toast.makeText(this, "Feedback Saved!", Toast.LENGTH_SHORT).show();
                etName.setText("");
                etMessage.setText("");
                ratingBar.setRating(0);
                tvDate.setText("Select Date");
            } else {
                Toast.makeText(this, "Error saving feedback!", Toast.LENGTH_SHORT).show();
            }
        });

        btnView.setOnClickListener(v ->
                startActivity(new Intent(this, ViewFeedbackActivity.class))
        );
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    tvDate.setText(selectedDate);
                }, year, month, day);
        dpd.show();
    }
}
