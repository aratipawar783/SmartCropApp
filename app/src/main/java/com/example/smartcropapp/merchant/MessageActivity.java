package com.example.smartcropapp.merchant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcropapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    SocialDBHelper db; long meId, otherId;
    EditText et; Button btn; ListView lv;

    @Override protected void onCreate(Bundle s){
        super.onCreate(s); setContentView(R.layout.activity_message);
        db = new SocialDBHelper(this);
        meId = getIntent().getLongExtra("meId", 1L);
        otherId = getIntent().getLongExtra("otherId", -1L);

        et = findViewById(R.id.etMessage);
        btn = findViewById(R.id.btnSend);
        lv = findViewById(R.id.lvMessages);
        load();

        btn.setOnClickListener(v-> {
            String t = et.getText().toString().trim();
            if(t.isEmpty()) return;
            db.sendMessage(meId, otherId, t, System.currentTimeMillis());
            // also open SMS app to send externally
            User other = db.getUserById(otherId);
            if(other!=null && other.getPhone()!=null && !other.getPhone().isEmpty()){
                Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + other.getPhone()));
                sms.putExtra("sms_body", t);
                startActivity(sms);
            }
            et.setText(""); load();
        });
    }

    private void load(){
        List<String> list = db.getMessagesBetween(meId, otherId);
        List<HashMap<String,String>> data = new ArrayList<>();
        for(String s: list){
            HashMap<String,String> m = new HashMap<>(); m.put("line1", s); data.add(m);
        }
        SimpleAdapter sa = new SimpleAdapter(this, data, android.R.layout.simple_list_item_1, new String[]{"line1"}, new int[]{android.R.id.text1});
        lv.setAdapter(sa);
    }
}
