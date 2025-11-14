package com.example.smartcropapp.marketprice;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MarketActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WebsiteAdapter adapter;
    private List<Website> websiteList;
    private List<Website> fullList;
    androidx.appcompat.widget.SearchView searchView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        recyclerView = findViewById(R.id.recyclerViewWebsites);
        searchView= findViewById(R.id.searchView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        websiteList = new ArrayList<>();
        loadWebsitesFromJson();
        fullList = new ArrayList<>(websiteList);

        adapter = new WebsiteAdapter(this, websiteList);
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle text change
                return false;
            }
        });
    }

    private void filter(String text) {
        websiteList.clear();
        if(text.isEmpty()) {
            websiteList.addAll(fullList);
        } else {
            for(Website w : fullList) {
                if(w.name.toLowerCase().contains(text.toLowerCase()) ||
                        w.description.toLowerCase().contains(text.toLowerCase())) {
                    websiteList.add(w);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadWebsitesFromJson() {
        try {
            String jsonStr = Utils.loadJSONFromAsset(this, "market_websites.json"); // your JSON file in assets
            JSONArray jsonArray = new JSONArray(jsonStr);
            for(int i=0;i<jsonArray.length();i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                websiteList.add(new Website(
                        obj.getString("name"),
                        obj.getString("url"),
                        obj.getString("description"),
                        obj.getString("logo"),
                        obj.getString("color"),
                        obj.getString("font")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
