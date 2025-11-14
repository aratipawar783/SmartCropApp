package com.example.smartcropapp.merchant;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;

import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    RecyclerView rv;
    EditText et;
    Button btn;
    SocialDBHelper db;
    long postId = 1; // example postId
    long me = 1;     // example userId
    CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        db = new SocialDBHelper(this);

        rv = findViewById(R.id.recyclerComments);
        rv.setLayoutManager(new LinearLayoutManager(this));
        et = findViewById(R.id.etComment);
        btn = findViewById(R.id.btnSend);

        loadComments();

        btn.setOnClickListener(v -> {
            String t = et.getText().toString().trim();
            if (t.isEmpty()) return;

            // ✅ This now works
            long id = db.addComment(postId, me, t, System.currentTimeMillis());

            et.setText("");
            loadComments();
        });
    }

    private void loadComments() {
        List<Comment> list = db.getComments(postId);
        adapter = new CommentAdapter(this, list);
        rv.setAdapter(adapter);
    }
}
