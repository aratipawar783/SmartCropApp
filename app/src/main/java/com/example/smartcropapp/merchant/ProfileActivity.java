package com.example.smartcropapp.merchant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    ImageView ivPhoto;
    TextView tvName, tvLocation, tvPhone, tvFollowersCount, tvPostsCount;
    Button btnEdit, btnFollowers;
    RecyclerView rvPosts;
    SocialDBHelper db;
    long userId = 1; // passed

    @Override protected void onCreate(Bundle s){
        super.onCreate(s); setContentView(R.layout.activity_profile);
        db = new SocialDBHelper(this);

        ivPhoto = findViewById(R.id.imgProfilePhoto);
        tvName = findViewById(R.id.tvName);
        tvLocation = findViewById(R.id.tvLocation);
        tvPhone = findViewById(R.id.tvPhone);
        tvFollowersCount = findViewById(R.id.tvFollowers);
        tvPostsCount = findViewById(R.id.tvPostsCount);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnFollowers = findViewById(R.id.btnFollowers);
        rvPosts = findViewById(R.id.recyclerUserPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        userId = getIntent().getLongExtra("userId", 1L);
        loadProfile();

        btnEdit.setOnClickListener(v-> {
            Intent i = new Intent(this, EditProfileActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
        });

        btnFollowers.setOnClickListener(v-> {
            Intent i = new Intent(this, FollowersActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
        });
    }

    private void loadProfile(){
        User u = db.getUserById(userId);
        if(u!=null){
            tvName.setText(u.getName());
            tvLocation.setText(u.location==null?"":u.location);
            tvPhone.setText(u.getPhone());
            if(u.getPhoto()!=null && !u.getPhoto().isEmpty()){
                try{ ivPhoto.setImageURI(Uri.parse(u.getPhoto())); } catch(Exception e){ ivPhoto.setImageResource(R.drawable.userprofile); }
            } else ivPhoto.setImageResource(R.drawable.userprofile);
        }
        List<User> followers = db.getFollowers(userId);
        tvFollowersCount.setText("Followers: " + followers.size());
        List<Post> posts = db.getPostsByUser(userId);
        tvPostsCount.setText("Posts: " + posts.size());
        PostAdapter adapter = new PostAdapter(this, posts, db, userId);
        rvPosts.setAdapter(adapter);
    }

    @Override protected void onResume(){ super.onResume(); loadProfile(); }
}
