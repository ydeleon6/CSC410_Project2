package edu.noctrl.ydeleon.project2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DecimalFormat;

import edu.noctrl.ydeleon.WeatherXmlParser.CurrentObservations;
import edu.noctrl.ydeleon.WeatherXmlParser.Downloader;
import edu.noctrl.ydeleon.WeatherXmlParser.WeatherInfo;
import edu.noctrl.ydeleon.WeatherXmlParser.WeatherInfoIO;

/**
 * Created by deleon118 on 5/10/15.
 */
public class GUIops {
    public static int currentState = -1;
    public static final int FORECAST = 1;
    public static final int CURRENT = 0;
    private static String img;
    private static WeatherInfo currentWeather;
    private static ImageView centerPic;
    private static TextView displayConditions;
    private static EditText enterZip;
    private static TextView temperature;
    private static TextView dew;
    private static TextView humidity;
    private static TextView pressure;
    private static TextView visibility;
    private static TextView windspeed;
    private static TextView gust;
    private static TextView currTime;
    private static RadioButton metricRadio, imperialRadio;
    private static LruCache<String, Bitmap> cache;
    private static MessageMaker makeMessage;
    private static SharedPreferences sp;

    public GUIops(Activity activity){
        makeMessage = new MessageMaker();
        Log.i("GUIops","INSIDE THE CONSTRUCTOR");
        centerPic = (ImageView)activity.findViewById(R.id.pic);
        displayConditions = (TextView)activity.findViewById(R.id.displayConditions);
        //enterZip = (EditText)activity.findViewById(R.id.zip);
        temperature = (TextView)activity.findViewById(R.id.displayTemp);
        dew = (TextView)activity.findViewById(R.id.displayDew);
        humidity = (TextView)activity.findViewById(R.id.displayHumidity);
        pressure = (TextView)activity.findViewById(R.id.displayPressure);
        visibility = (TextView)activity.findViewById(R.id.displayVisibility);
        windspeed = (TextView)activity.findViewById(R.id.displayWindspeed);
        gust = (TextView)activity.findViewById(R.id.displayGust);
        currTime= (TextView)activity.findViewById(R.id.currentTimeDisplay);
        metricRadio = (RadioButton)activity.findViewById(R.id.metric);
        imperialRadio = (RadioButton)activity.findViewById(R.id.imperial);
        Log.i("GUIops","creating the cache");
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        String zip = "60505"; //get zip from textview or whatever
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
        sp = activity.getPreferences(Context.MODE_PRIVATE); //get shared pref
        Log.i("GUIops","Finished inside the constructor");
    }
    public static void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        String getCurrUnits = temperature.getText().toString();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.metric:
                if (checked) {
                    if (getCurrUnits.charAt(getCurrUnits.length() - 1) == 'F') {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("system","metric");
                        editor.commit();
                        toMetric();
                    }
                }
                break;
            case R.id.imperial:
                if (checked) {
                    if (getCurrUnits.charAt(getCurrUnits.length() - 1) == 'C') {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("system","imp");
                        editor.commit();
                        toImp();
                    }
                }
                break;
        }
    }
    protected static void toMetric(){
        double temp, dewpoint, pres, vis, spd, gst;
        DecimalFormat df = new DecimalFormat("###0.0");

        //gets string component of text fields, removes alpha chars (from StackOverflow), and parses int
        temp = Double.parseDouble(temperature.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        dewpoint = Double.parseDouble(dew.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        pres = Double.parseDouble(pressure.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        vis = Double.parseDouble(visibility.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        spd = Double.parseDouble(windspeed.getText().toString().replaceAll("[//A-Za-z ]*", ""));


        temp = (((temp - 32)*5)/9);
        dewpoint = (((dewpoint - 32)*5)/9);
        pres = ((pres*10) * 2.54);
        vis = (vis * 1.60934);
        spd = (spd * 1.60934);

        temperature.setText(df.format(temp) + " C");
        dew.setText(df.format(dewpoint) + " C");
        pressure.setText(df.format(pres) + " mb");
        visibility.setText(df.format(vis) + " km");
        windspeed.setText(df.format(spd) + " km/h");

        if(gust.getText().equals("NaN mph")){
            gust.setText("NaN km/h");
        }
        else{
            gst = Double.parseDouble(gust.getText().toString().replaceAll("[//A-Za-z ]*", ""));
            gst = (gst * 1.60934);
            gust.setText(df.format(gst) + "km/h");
        }
    }
    protected static void toImp(){
        double temp, dewpoint, pres, vis, spd, gst;
        DecimalFormat df = new DecimalFormat("###0.0");

        //gets string component of text fields, removes alpha chars (from StackOverflow), and parses int
        temp = Double.parseDouble(temperature.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        dewpoint = Double.parseDouble(dew.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        pres = Double.parseDouble(pressure.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        vis = Double.parseDouble(visibility.getText().toString().replaceAll("[//A-Za-z ]*", ""));
        spd = Double.parseDouble(windspeed.getText().toString().replaceAll("[//A-Za-z ]*", ""));

        temp = (((temp*9)/5)+32);
        dewpoint = (((dewpoint*9)/5)+32);
        pres = ((pres/10) / 2.54);
        vis = (vis / 1.60934);
        spd = (spd / 1.60934);

        temperature.setText(df.format(temp) + " F");
        dew.setText(df.format(dewpoint) + " F");
        pressure.setText(df.format(pres) + " in");
        visibility.setText(df.format(vis) + " mi");
        windspeed.setText(df.format(spd) + " mph");

        if(gust.getText().equals("NaN km/h")){
            gust.setText("NaN mph");
        }
        else{
            gst = Double.parseDouble(gust.getText().toString().replaceAll("[//A-Za-z ]*", ""));
            gst = (gst / 1.60934);
            gust.setText(df.format(gst) + "mph");
        }
    }
    private static void makeHazardAlerts(){

        if(currentWeather.alerts.size() > 0){
            for(String alert : currentWeather.alerts){
                final String notificationText = alert;
                MainActivity.mainActivity.runOnUiThread(
                    new Runnable() {
                        public void run(){
                            makeMessage.notificationMaker("Weather Alert", notificationText);
                        }
                    }
                );
            }
        }
    }
    public static void populateCurrent(WeatherInfo info){
        currentState = CURRENT;
        currentWeather = info; //populate the static info for forecast to use.
        new ForecastOps(); //to populate the array of days/list
        CurrentObservations current = info.current;
        Bitmap image = null;
        //check if image is in the cache.
        img = current.imageUrl;
        if(cache.get(img)==null){
            //download and get image.
            WeatherInfoIO.ImageListener listener = new WeatherInfoIO.ImageListener();
            Downloader<Bitmap> downloader = new Downloader<>(listener);
            downloader.execute(img);
        }
        else{
            image = cache.get(img); //get image
            GUIops.updateCurrentImage(image);//update it
        }
        makeHazardAlerts();
        //set fields
        temperature.setText(current.temperature+" F");
        dew.setText(current.dewPoint+" F");
        humidity.setText(current.humidity + " F");
        displayConditions.setText(current.summary);
        pressure.setText(current.pressure+" in");
        visibility.setText(current.visibility+" F");
        windspeed.setText(current.windSpeed+" F");
        gust.setText(current.gusts+" mph");
        currTime.setText(current.timestamp);

        //get unit
        /*String system = "Imperial";
        if(sp.contains("system")){
            system = sp.getString("system","Imperial"); //if nothing is there, use default;
        }
        if(system.equals("Imperial")){
            imperialRadio.toggle();
            toImp();
        }
        else if(system.equals("Metric")){
            metricRadio.toggle();
            toMetric();
        }*/

    }
    public static void updateCurrentImage(Bitmap image){
        if(currentState == CURRENT) {
            centerPic.setImageBitmap(image);
        }
        else{
            ExtendedForecast.setIcon(image);
        }
    }
    public static void updateCache(Bitmap image){
        if(img == null || image == null){
            Log.i("ERROR","trying to add a null pic/url to cache");
            return;
        }
        cache.put(img, image);
    }
    public static WeatherInfo getCurrentWeather(){
        return currentWeather;
    }
    public static LruCache<String, Bitmap> getCache(){
        return cache;
    }

}
