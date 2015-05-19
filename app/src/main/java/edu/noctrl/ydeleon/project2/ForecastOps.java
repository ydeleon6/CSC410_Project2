package edu.noctrl.ydeleon.project2;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.List;

import edu.noctrl.ydeleon.WeatherXmlParser.DayForecast;

public class ForecastOps {
    //initialize the FORECAST GUI
    private static String[]days = new String[8];
    LruCache<String, Bitmap> cache;
    private static List<DayForecast> forecast = new ArrayList<>(8);

    public ForecastOps(){
        //init pics & cache
        Log.i("FORECASTOPS", "INSIDE THE FORECAST");
        forecast = GUIops.getCurrentWeather().forecast;
        for(int i=0;i<forecast.size();i++){
            DayForecast d = forecast.get(i);
            days[i] = d.day.toString(); //add url to array.
        }
        cache = GUIops.getCache();
        Log.i("FORECASTOPS", "DONE");
    }

    public static String[] getDays(){
        return days;
    }

}