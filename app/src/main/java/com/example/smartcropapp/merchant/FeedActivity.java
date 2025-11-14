package com.example.smartcropapp.merchant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;

import java.util.List;

public class FeedActivity extends AppCompatActivity {
    SocialDBHelper db;
    RecyclerView rv;
    PostAdapter adapter;
    ImageView ivProfile;
    EditText etSearch;
    Button btnCreate;
    long meId = 1; // replace with logged-in user id

    @Override protected void onCreate(Bundle s){
        super.onCreate(s); setContentView(R.layout.activity_feed);
        db = new SocialDBHelper(this);

        // ensure user exists (basic)
        if(db.getAllUsers().isEmpty()){
            meId = db.insertUser("You","", "", "Your location", "", "", "");
        } else {
            meId = db.getAllUsers().get(0).id;
        }

        ivProfile = findViewById(R.id.btnProfile);
        etSearch = findViewById(R.id.etSearch);
        btnCreate = findViewById(R.id.btnCreate);
        rv = findViewById(R.id.recyclerFeed);
        rv.setLayoutManager(new LinearLayoutManager(this));

        loadProfileLogo();
        loadFeed();

        ivProfile.setOnClickListener(v-> {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("userId", meId);
            startActivity(i);
        });

        btnCreate.setOnClickListener(v-> {
            Intent i = new Intent(this, CreatePostActivity.class);
            i.putExtra("meId", meId);
            startActivity(i);
        });
    }

    private void loadProfileLogo(){
        User u = db.getUserById(meId);
        if(u!=null && u.getPhoto()!=null && !u.getPhoto().isEmpty()){
            try{ ivProfile.setImageURI(Uri.parse(u.getPhoto())); } catch(Exception e){ ivProfile.setImageResource(R.drawable.userprofile); }
        } else ivProfile.setImageResource(R.drawable.userprofile);
    }

    private void loadFeed(){
        List<Post> posts = db.getFeed();
        adapter = new PostAdapter(this, posts, db, meId);
        rv.setAdapter(adapter);
    }

    @Override protected void onResume(){ super.onResume(); loadProfileLogo(); loadFeed(); }
}
