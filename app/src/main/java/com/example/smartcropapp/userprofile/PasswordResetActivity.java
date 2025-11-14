package com.example.smartcropapp.userprofile;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartcropapp.DBHelper;
import com.example.smartcropapp.DashBoardActivity;
import com.example.smartcropapp.R;
import com.google.android.material.textfield.TextInputEditText;

public class PasswordResetActivity extends AppCompatActivity {

    DBHelper dbHelper;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        dbHelper = new DBHelper(this);
        phone = getIntent().getStringExtra("phone");

        Toolbar toolbar = findViewById(R.id.toolbar_reset);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reset Password");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextInputEditText etPassword = findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnReset = findViewById(R.id.btnResetPassword);

        btnReset.setOnClickListener(v -> {
            String pass = etPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                boolean updated = dbHelper.updatePasswordByPhone(phone, pass);
                if (updated) {
                    Toast.makeText(this, "Password Reset Successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(PasswordResetActivity.this, DashBoardActivity.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(this, "Error resetting password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
