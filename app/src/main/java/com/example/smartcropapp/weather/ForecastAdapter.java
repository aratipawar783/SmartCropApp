package com.example.smartcropapp.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcropapp.R;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    public interface OnForecastClickListener {
        void onForecastClick(ForecastModel forecast);
    }

    private final List<ForecastModel> forecastList;
    private final OnForecastClickListener listener;

    public ForecastAdapter(List<ForecastModel> forecastList, OnForecastClickListener listener) {
        this.forecastList = forecastList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_forcast, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastModel f = forecastList.get(position);
        holder.tvDate.setText(f.getDate());
        holder.tvTemp.setText(f.getTemp() + "°C");
        holder.tvCondition.setText(capitalizeShort(f.getCondition()));
        holder.tvHumidity.setText("Hum: " + f.getHumidity() + "%");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onForecastClick(f);
        });
    }

    @Override
    public int getItemCount() { return forecastList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTemp, tvCondition, tvHumidity;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvHumidity = itemView.findViewById(R.id.tvHumidity);
        }
    }

    private static String capitalizeShort(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : "");
    }
}
