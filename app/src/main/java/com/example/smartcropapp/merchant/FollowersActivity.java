package com.example.smartcropapp.merchant;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;

import java.util.List;

public class FollowersActivity extends AppCompatActivity {
    RecyclerView rv; SocialDBHelper db; long userId; FollowerAdapter adapter;

    @Override protected void onCreate(Bundle s){
        super.onCreate(s); setContentView(R.layout.activity_followers);
        db = new SocialDBHelper(this);
        userId = getIntent().getLongExtra("userId", -1);
        rv = findViewById(R.id.recyclerFollowers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<User> followers = db.getFollowers(userId);
        adapter = new FollowerAdapter(this, followers);
        rv.setAdapter(adapter);
    }
}
