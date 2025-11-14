package com.example.smartcropapp.weather;

import java.util.ArrayList;
import java.util.List;

public class ForecastModel {
    private String date;
    private String temp;
    private String condition;
    private String humidity;
    private List<Float> hourlyTemps;

    public ForecastModel(String date, String temp, String condition, String humidity) {
        this.date = date;
        this.temp = temp;
        this.condition = condition;
        this.humidity = humidity;
        this.hourlyTemps = new ArrayList<>();
    }

    public String getDate() { return date; }
    public String getTemp() { return temp; }
    public String getCondition() { return condition; }
    public String getHumidity() { return humidity; }

    public void setHourlyTemps(List<Float> hourlyTemps) { this.hourlyTemps = hourlyTemps; }
    public List<Float> getHourlyTemps() { return hourlyTemps; }
}
