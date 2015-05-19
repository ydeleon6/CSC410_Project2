package edu.noctrl.ydeleon.project2;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import edu.noctrl.ydeleon.WeatherXmlParser.WeatherInfoIO;


public class MainActivity extends ActionBarActivity {
    public static Activity mainActivity;
    public static Context mainContext;
    SharedPreferences sharedPref;
    MessageMaker makeMessage = new MessageMaker();
    List<String> savedZips = new ArrayList<String>();
    //SharedPreferences sp = getPreferences(Context.MODE_PRIVATE); //get shared pref
    LruCache<String, Bitmap> cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //init vars.
        mainActivity = this;
        mainContext = this.getApplicationContext();
        new GUIops(MainActivity.this);
        Log.i("DEBUG", "initialized variables");
    }

    public void onRadioButtonClicked(View view) {
        GUIops.onRadioButtonClicked(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecast_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_zip).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        if (searchView != null) {
            final Menu menu_block = menu;
            searchView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                }
            });
            /*searchView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //call dialog stuff
                    makeZipDialog();
                    return true;
                }
            });*/
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // collapse the view ?
                JSONArray zipCodes;

                if(isNetworkConnected()) {
                    boolean hasNonAlpha = false;
                    try {
                        Double.parseDouble(query);
                    } catch (Exception ex) {
                        hasNonAlpha = true;
                    }

                    if (!hasNonAlpha && query.length() == 5) {
                        sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);

                        try {
                            getJSONZips(query);
                            savedZips = getZipList();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        MessageMaker.toastMaker(savedZips.toString());

                        String url = "http://craiginsdev.com/zipcodes/findzip.php?zip=" + query;
                        WeatherInfoIO.WeatherListener listener = new WeatherInfoIO.WeatherListener();
                        WeatherInfoIO.loadFromUrl(url, listener);
                        menu_block.findItem(R.id.search_zip).collapseActionView();
                    } else {
                        //toast saying no luck.
                        //Also, maybe do another
                        makeMessage.toastMaker("Invalid Zip Code");

                    }
                    return false;
                }
                else{
                    makeMessage.toastMaker("No Internet Connection");
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //idk, show recent 5 zips?
                return false;
            }
            });
            Log.i("DEBUG", "SearchView OK");
        } else
            Log.i("DEBUG", "SearchView is null");
        return super.onCreateOptionsMenu(menu);
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.week:
                //launch forecast
                Intent myIntent = new Intent(MainActivity.this, ForecastActivity.class);
                MainActivity.this.startActivity(myIntent);
                return true;
            case R.id.today:
                //nothing.
                geoIntent();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //From: http://stackoverflow.com/questions/9570237/android-check-internet-connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public void getJSONZips(String query) throws JSONException {
        JSONArray zipCodes;
        int oldest;
        List<String> zipList = getZipList();

        zipCodes = new JSONArray(sharedPref.getString("JSON", "[]"));
        //oldest = sharedPref.getInt("oldest", 0);

        if(!zipList.contains(query)){
            for(int i = zipCodes.length() - 1; i > 0; i--){
                zipCodes.put(i, zipCodes.get(i-1));
            }

            zipCodes.put(0, query);

        }
        else if (zipList.contains(query)){
            for(int i = zipList.indexOf(query); i > 0; i--){
                zipCodes.put(i, zipCodes.get(i-1));
            }
            zipCodes.put(0, query);
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("JSON", zipCodes.toString());
        editor.commit();
    }

    public List<String> getZipList() throws JSONException {
        List<String> savedZips = new ArrayList<String>();
        String zip = "";
        JSONArray zips = null;
        sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);

        zips = new JSONArray(sharedPref.getString("JSON", "[]"));

        for (int i = 0; i < zips.length(); i++) {
            savedZips.add(zips.getString(i));
        }

        return savedZips;
    }
    public void geoIntent(){
        LocationManager lm = (LocationManager)MainActivity.mainContext.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + latitude + "," + longitude));
            MainActivity.this.startActivity(in);
        }
        else{
            MessageMaker.toastMaker("Location Unavailable");
        }
    }
}