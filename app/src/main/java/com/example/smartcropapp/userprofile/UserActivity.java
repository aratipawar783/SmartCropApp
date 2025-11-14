package com.example.smartcropapp.userprofile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.smartcropapp.userprofile.CropHelper;
import com.example.smartcropapp.DBHelper;
import com.example.smartcropapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

public class UserActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText etName, etPhone, etEmail, etAddress, etAadhar, etDob;
    private Button btnSave;
    private DBHelper dbHelper;
    private boolean isEditable = true;

    private Uri tempCameraUri;

    private ActivityResultLauncher<Intent> pickGalleryLauncher;
    private ActivityResultLauncher<Intent> takeCameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// Set the title text
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
        }

// Set the title color to white
        toolbar.setTitleTextColor(Color.WHITE);

        dbHelper = new DBHelper(this);

        imgProfile = findViewById(R.id.imgProfile);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etAadhar = findViewById(R.id.etAadhar);
        etDob = findViewById(R.id.etDob);
        btnSave = findViewById(R.id.btnSave);

        // Disable editing by default
        setEditable(false);

        etDob.setOnClickListener(v -> {
            if (isEditable) showDatePicker();
        });

        // Gallery picker
        pickGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) startCrop(uri);
                    }
                });

        // Camera capture
        takeCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (tempCameraUri != null) startCrop(tempCameraUri);
                    }
                });

        btnSave.setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    private void setEditable(boolean editable) {
        isEditable = editable;
        etName.setEnabled(editable);
        etPhone.setEnabled(editable);
        etEmail.setEnabled(editable);
        etAddress.setEnabled(editable);
        etAadhar.setEnabled(editable);
        etDob.setEnabled(editable);
        btnSave.setEnabled(editable);
        btnSave.setAlpha(editable ? 1f : 0.5f);
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and Phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String aadhar = etAadhar.getText().toString().trim();
        String dob = etDob.getText().toString().trim();

        // Get existing photo URI
        Cursor c = dbHelper.getUserProfile();
        String photoUri = "";
        if (c.moveToFirst()) {
            int photoIdx = c.getColumnIndex("photo");
            if (photoIdx != -1) photoUri = c.getString(photoIdx);
        }
        c.close();

        boolean ok = dbHelper.saveUserProfile(name, phone, email, address, aadhar, dob, photoUri);
        if (ok) {
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
            setEditable(false);
        } else {
            Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        final Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            etDob.setText(date);
        }, y, m, d);
        dp.show();
    }

    @SuppressLint("Range")
    private void loadProfile() {
        Cursor cursor = dbHelper.getUserProfile();
        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndex("name")));
            etPhone.setText(cursor.getString(cursor.getColumnIndex("phone")));
            etEmail.setText(cursor.getString(cursor.getColumnIndex("email")));
            etAddress.setText(cursor.getString(cursor.getColumnIndex("address")));
            etAadhar.setText(cursor.getString(cursor.getColumnIndex("aadhar")));
            etDob.setText(cursor.getString(cursor.getColumnIndex("dob")));
            String photo = cursor.getString(cursor.getColumnIndex("photo"));
            if (photo != null && !photo.isEmpty()) imgProfile.setImageURI(Uri.parse(photo));
        }
        if (cursor != null) cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_edit_profile) {
            setEditable(true);
            return true;
        } else if (id == R.id.menu_update_photo) {
            showPhotoOptions();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void showPhotoOptions() {
        String[] options = {"Gallery", "Camera"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) pickGallery();
                    else takeCamera();
                }).show();
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickGalleryLauncher.launch(intent);
    }

    private void takeCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Profile_" + System.currentTimeMillis());
        tempCameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri);
        takeCameraLauncher.launch(intent);
    }

    private void startCrop(@NonNull Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if (bitmap != null) {
                imgProfile.setImageBitmap(bitmap);
                CropHelper.enableMoveAndScale(imgProfile);

                // Ask user crop type
                String[] options = {"Circle", "Square", "Rectangle"};
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Select Crop Shape")
                        .setItems(options, (dialog, which) -> {
                            CropHelper.CropShape shape;
                            if (which == 0) shape = CropHelper.CropShape.CIRCLE;
                            else if (which == 1) shape = CropHelper.CropShape.SQUARE;
                            else shape = CropHelper.CropShape.RECTANGLE;

                            Bitmap cropped = CropHelper.cropImage(imgProfile, shape);
                            if (cropped != null) {
                                imgProfile.setImageBitmap(cropped);

                                try {
                                    File tempFile = new File(getCacheDir(), "profile_" + System.currentTimeMillis() + ".png");
                                    java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile);
                                    cropped.compress(Bitmap.CompressFormat.PNG, 100, out);
                                    out.flush();
                                    out.close();

                                    dbHelper.updateProfilePhoto(Uri.fromFile(tempFile).toString());
                                    Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).show();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 101 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takeCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
