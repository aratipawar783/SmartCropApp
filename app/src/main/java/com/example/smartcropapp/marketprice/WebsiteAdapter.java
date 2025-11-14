package com.example.smartcropapp.marketprice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebsiteAdapter extends RecyclerView.Adapter<WebsiteAdapter.WebsiteViewHolder> {

    private Context context;
    private List<Website> websiteList;
    private SharedPreferences prefs;

    public WebsiteAdapter(Context context, List<Website> websiteList) {
        this.context = context;
        this.websiteList = websiteList;
        prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE);

        // Load saved favorites
        Set<String> favorites = prefs.getStringSet("fav_set", new HashSet<>());
        for (Website site : websiteList) {
            site.isFavorite = favorites.contains(site.name);
        }
    }

    @NonNull
    @Override
    public WebsiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_website, parent, false);
        return new WebsiteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WebsiteViewHolder holder, int position) {
        Website site = websiteList.get(position);
        holder.tvName.setText(site.name);
        holder.tvDesc.setText(site.description);

        // Set background color
        holder.card.setBackgroundColor(Color.parseColor(site.color));

        // Load logo
        int logoResId = context.getResources().getIdentifier(site.logo.replace(".png",""), "drawable", context.getPackageName());
        holder.ivLogo.setImageResource(logoResId);

        // Load font
        try {
            Typeface tf = Typeface.createFromAsset(context.getAssets(), site.font);
            holder.tvName.setTypeface(tf);
        } catch (Exception e) { e.printStackTrace(); }

        // Favorite icon
        holder.ivFavorite.setImageResource(site.isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        holder.ivFavorite.setOnClickListener(v -> {
            site.isFavorite = !site.isFavorite;
            holder.ivFavorite.setImageResource(site.isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            saveFavorites();
        });

        // Open website on click
        holder.card.setOnClickListener(v -> {
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, site.url);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return websiteList.size();
    }

    private void saveFavorites() {
        Set<String> favSet = new HashSet<>();
        for (Website site : websiteList) {
            if (site.isFavorite) favSet.add(site.name);
        }
        prefs.edit().putStringSet("fav_set", favSet).apply();
    }

    public static class WebsiteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo, ivFavorite;
        TextView tvName, tvDesc;
        View card;

        public WebsiteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivLogo);
            tvName = itemView.findViewById(R.id.tvName);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            card = itemView.findViewById(R.id.card);
        }
    }
}
