package com.example.smartcropapp.merchant;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcropapp.R;

public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMG = 7001;
    ImageView ivPhoto;
    EditText etName, etPhone, etEmail, etLocation, etCrops, etBio;
    Button btnSave;
    SocialDBHelper db;
    long userId;
    String photoUri = "";

    @Override protected void onCreate(Bundle s){
        super.onCreate(s); setContentView(R.layout.activity_edit_profile);
        db = new SocialDBHelper(this);

        ivPhoto = findViewById(R.id.imgEditProfilePhoto);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etLocation = findViewById(R.id.etLocation);
        etCrops = findViewById(R.id.etCrops);
        etBio = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSaveProfile);

        userId = getIntent().getLongExtra("userId", 1L);
        loadUser();

        ivPhoto.setOnClickListener(v-> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK_IMG);
        });

        btnSave.setOnClickListener(v-> {
            db.updateUser(userId, etName.getText().toString(), etPhone.getText().toString(), etEmail.getText().toString(),
                    etLocation.getText().toString(), etCrops.getText().toString(), etBio.getText().toString(), photoUri);
            finish();
        });
    }

    private void loadUser(){
        User u = db.getUserById(userId);
        if(u!=null){
            etName.setText(u.getName());
            etPhone.setText(u.phone);
            etEmail.setText(u.email);
            etLocation.setText(u.location);
            etCrops.setText(u.crops);
            etBio.setText(u.bio);
            photoUri = u.photoUri;
            if(photoUri!=null && !photoUri.isEmpty()) ivPhoto.setImageURI(Uri.parse(photoUri));
        }
    }
    @Override protected void onActivityResult(int req,int res, Intent data){
        super.onActivityResult(req,res,data);
        if(res== Activity.RESULT_OK && req==PICK_IMG && data!=null){
            Uri u = data.getData();
            if(u!=null){
                ivPhoto.setImageURI(u);
                photoUri = u.toString();
            }
        }
    }
}
