package com.example.smartcropapp.merchant;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;

import java.util.List;

public class LikesActivity extends AppCompatActivity {
    RecyclerView rv; SocialDBHelper db; long postId;
    FollowerAdapter adapter;

    @Override protected void onCreate(Bundle s){
        super.onCreate(s); setContentView(R.layout.activity_likes);
        db = new SocialDBHelper(this);
        postId = getIntent().getLongExtra("postId", -1);
        rv = findViewById(R.id.recyclerLikes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<User> likes = db.getLikesUsers(postId);
        adapter = new FollowerAdapter(this, likes);
        rv.setAdapter(adapter);
    }
}
