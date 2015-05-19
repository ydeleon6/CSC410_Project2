package edu.noctrl.ydeleon.project2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

import edu.noctrl.ydeleon.WeatherXmlParser.DayForecast;
import edu.noctrl.ydeleon.WeatherXmlParser.Downloader;
import edu.noctrl.ydeleon.WeatherXmlParser.WeatherInfo;
import edu.noctrl.ydeleon.WeatherXmlParser.WeatherInfoIO;


public class ExtendedForecast extends ActionBarActivity implements AMForecast.OnFragmentInteractionListener,
PMForecast.OnFragmentInteractionListener{
    private float x1,x2;
    static final int MIN_DISTANCE = 150;
    public static int currentFragment = -1;
    public static final int AM = 0;
    public static final int PM = 1;
    private static TextView forecastDay;
    private static TextView precipitation;
    private static TextView min;
    private static TextView max;
    public static int position; //public static so your fragments can access it
    public static ImageView image; //static so your async can just pop the value in
    SharedPreferences sharedPreference;
    protected void swapFragments(){
        if(currentFragment == AM){
            currentFragment = PM;
            launchPM();
        }
        else{
            currentFragment = AM;
            launchAM();
        }
    }
    protected void launchPM(){
        getFragmentManager().beginTransaction()
                .add(R.id.forecastContainer, new PMForecast())
                .commit();
    }
    protected void launchAM(){
        getFragmentManager().beginTransaction()
                .add(R.id.forecastContainer, new AMForecast())
                .commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extended_forecast);
        Bundle bundle = getIntent().getExtras();
        GUIops.currentState = GUIops.FORECAST;
        currentFragment = AM; //arbitrary decision
        if(bundle.containsKey("position")){
            position = bundle.getInt("position");
        }
        else{
            position = -1;
        }
        sharedPreference = MainActivity.mainActivity.getPreferences(Context.MODE_PRIVATE); //get shared pref
        TextView name = (TextView)findViewById(R.id.location);
        image = (ImageView)findViewById(R.id.forecastPic);
        forecastDay = (TextView)findViewById(R.id.forecastDay);
        precipitation = (TextView)findViewById(R.id.forecastPrecip);
        min = (TextView)findViewById(R.id.min);
        max = (TextView)findViewById(R.id.max);
        Log.i("DEBUG","INSIDE EXTENDED FORECAST");
        Log.i("DEBUG", "POSITION IS "+position);

        WeatherInfo info = GUIops.getCurrentWeather();
        DayForecast day = info.forecast.get(position);
        if(sharedPreference.contains("system")){
            String system = sharedPreference.getString("system","imp");
            Log.i("DEBUG", "USER chose the "+system+" system");
            double high = day.amForecast.temperature;
            double low = day.pmForecast.temperature;
            DecimalFormat df = new DecimalFormat("###0.0");
            if(system.equals("imp")){
                min.setText(df.format(low) +" F");
                max.setText(df.format(high) +" F");
            }
            else{
                high = ((high - 32)*5) /9;
                low =  ((low - 32)*5) /9;
                min.setText(df.format(low) +" C");
                max.setText(df.format(high) +" C");
            }
        }
        name.setText(info.location.name);
        precipitation.setText(day.precipitation +" %");
        String arr[] = day.day.toString().split(" ");
        // weekday, month/day
        forecastDay.setText(arr[0]+" "+arr[1]+" "+arr[2]);
        //if there's no image, just download it
        if(GUIops.getCache().get(day.icon) == null){
            WeatherInfoIO.ImageListener listener = new WeatherInfoIO.ImageListener();
            Downloader<Bitmap> downloader = new Downloader<>(listener);
            downloader.execute(day.icon);
        }
        else{
            image.setImageBitmap(GUIops.getCache().get(day.icon));
        }
        //launch am fragment
        launchAM();
    }
    public void toMetric(){

    }
    public static void setIcon(Bitmap pic){
        image.setImageBitmap(pic);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_extended_forecast, menu);
        return true;
    }
    /*
    I wasn't having good luck with the example on K:Drive so I used this instead
    http://stackoverflow.com/questions/6645537/how-to-detect-the-swipe-left-or-right-in-android
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                //left to right -> = positive
                //right to left <- = negative
                Log.i("DIRECTION", "Change was "+deltaX);
                if (Math.abs(deltaX) > MIN_DISTANCE)
                {
                    swapFragments();
                }
                break;
        }
        return super.onTouchEvent(event);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
