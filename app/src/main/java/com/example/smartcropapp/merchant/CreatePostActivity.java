package com.example.smartcropapp.merchant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcropapp.R;

public class CreatePostActivity extends AppCompatActivity {
    private static final int PICK = 8001;
    ImageView ivPreview; EditText etCaption, etPrice; Button btnPick, btnPost;
    SocialDBHelper db; long me;
    String imgUri = "";

    @Override protected void onCreate(Bundle s){
        super.onCreate(s); setContentView(R.layout.activity_create_post);
        db = new SocialDBHelper(this);
        me = getIntent().getLongExtra("meId", 1L);

        ivPreview = findViewById(R.id.imgPostPreview);
        etCaption = findViewById(R.id.etCaption);
        etPrice = findViewById(R.id.etPrice);
        btnPick = findViewById(R.id.btnSelectImage);
        btnPost = findViewById(R.id.btnPost);

        btnPick.setOnClickListener(v-> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK);
        });

        btnPost.setOnClickListener(v-> {
            if(imgUri.isEmpty()){ Toast.makeText(this,"Pick image", Toast.LENGTH_SHORT).show(); return; }
            long id = db.insertPost(me, imgUri, etCaption.getText().toString(), etPrice.getText().toString(), System.currentTimeMillis());
            if(id>0){ Toast.makeText(this,"Posted", Toast.LENGTH_SHORT).show(); finish(); }
            else Toast.makeText(this,"Failed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override protected void onActivityResult(int req,int res, Intent data){
        super.onActivityResult(req,res,data);
        if(res== Activity.RESULT_OK && req==PICK && data!=null){
            Uri u = data.getData();
            if(u!=null){
                ivPreview.setImageURI(u);
                imgUri = u.toString();
            }
        }
    }
}
