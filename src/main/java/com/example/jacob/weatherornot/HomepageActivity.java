package com.example.jacob.weatherornot;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import Information.JSON_Parser;
import Information.Weather_Client;
import Structure_Model.Weather_Hub_Model;
import Utility.GPS_Utility;
import Utility.Utilities;

public class HomepageActivity extends AppCompatActivity {

    TextView temptv, citytv, degreetv, conditiontv;
    ImageView ICON;
    Weather_Hub_Model weather = new Weather_Hub_Model();
    //
    SharedPreferences LocationSaver;
    //
    String Icon, Description;
    Spinner ActivityList;
    SwipeRefreshLayout SwipeDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        LocationSaver = getPreferences(MODE_PRIVATE);

        temptv = (TextView) findViewById(R.id.temptv);
        citytv = (TextView) findViewById(R.id.citytv);
        degreetv = (TextView) findViewById(R.id.degreetv);
        conditiontv = (TextView) findViewById(R.id.conditiontv);
        ICON = (ImageView) findViewById(R.id.ICON);
        SwipeDown = (SwipeRefreshLayout) findViewById(R.id.SwipeDown);
        ActivityList = (Spinner) findViewById(R.id.spinner);


        SwipeDown.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                receiveGpsLocation();
            }
        });

        receiveGpsLocation();
    }

    public void receiveGpsLocation() {

        ActivityCompat.requestPermissions(HomepageActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2018);
        GPS_Utility GPS = new GPS_Utility(getApplicationContext());
        Location GPS_location = GPS.getGPS_LOCATION();
        SharedPreferences.Editor NewSave = LocationSaver.edit();
        SwipeDown.setRefreshing(true);
        if (GPS_location != null) {


            double LATITUDE = GPS_location.getLatitude();
            double LONGITUDE = GPS_location.getLongitude();

//          Formats API URL
            String Coordinates = new String("lat=" + LATITUDE + "&lon=" + LONGITUDE);
            NewSave.putString("saved coordinates", Coordinates);
            NewSave.commit();
            receiveWeatherInformation(Coordinates);

        } else {
            String LastKnowCoordinates = LocationSaver.getString("saved coordinates", "");
            receiveWeatherInformation(LastKnowCoordinates);
        }

    }

    public void receiveWeatherInformation(String Coordinates) {
        WeatherTask weatherTask = new WeatherTask();
        weatherTask.execute(new String[]{Coordinates + "&unites=imperial"});
    }


    private class WeatherTask extends AsyncTask<String, Void, Weather_Hub_Model> {

        @Override
        protected Weather_Hub_Model doInBackground(String... params) {

            String data = ((new Weather_Client()).GetWeatherData(params[0]));
            weather = JSON_Parser.getWeather(data);
            return weather;
        }

        @Override
        protected void onPostExecute(Weather_Hub_Model weather_hub_model) {

            super.onPostExecute(weather_hub_model);

            if (weather.currentCondition.getCondtion() == null) {
                FailSafe();
            } else {

                applyIcon();
                formatDescription();

                Log.d("Message", weather.currentCondition.getDescription());

                ArrayAdapter<String> ArrayForActivityList = new ArrayAdapter<String>(HomepageActivity.this,
                        android.R.layout.simple_expandable_list_item_1, getResources().getStringArray(R.array.Activities));

                ArrayForActivityList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ActivityList.setAdapter(ArrayForActivityList);


                double KELVIN_TO_FAHRENHEIT = ((weather.currentCondition.getTemp() * 1.8) - 459.67);

                DecimalFormat decimalFormat = new DecimalFormat("#");
                String FormatTemperature = decimalFormat.format(KELVIN_TO_FAHRENHEIT);

                SwipeDown.setRefreshing(false);
                temptv.setText(FormatTemperature);
                citytv.setText(weather.location.getCity());
            }
        }

        public void applyIcon() {

            Icon = weather.currentCondition.getCondtion();

            if (Icon.equals("Rain")) {
                ICON.setImageResource(R.drawable.rain);
            }
            if (Icon.equals("Snow")) {
                ICON.setImageResource(R.drawable.snow);
            }
            if (Icon.equals("Mist")) {
                ICON.setImageResource(R.drawable.mist);
            }
            if (Icon.equals("Clear")) {
                ICON.setImageResource(R.drawable.clear_sky);
            }
            if (Icon.equals("Clouds")) {
                ICON.setImageResource(R.drawable.partly_cloudy);
            }

            return;

        }


        public void formatDescription() {

            Description = weather.currentCondition.getDescription();

            if (Description.equals("clear sky")) {
                conditiontv.setText("Clear Skys");
            }
            if (Description.equals("rain and drizzle")) {
                conditiontv.setText("Rain and Drizzle");
            }
            if (Description.equals("haze")) {
                conditiontv.setText("Haze");
                ICON.setImageResource(R.drawable.mist);
            }
            if (Description.equals("light rain and snow")) {
                conditiontv.setText("Light Rain and Snow");
            }
            if (Description.equals("scattered clouds")) {
                conditiontv.setText("Scattered Clouds");
                ICON.setImageResource(R.drawable.partly_cloudy);
            }
            if (Description.equals("overcast clouds")) {
                conditiontv.setText("Overcast Clouds");
                ICON.setImageResource(R.drawable.partly_cloudy);
            }
            if (Description.equals("broken clouds")) {
                conditiontv.setText("Broken Clouds");
                ICON.setImageResource(R.drawable.partly_cloudy);
            }
            if (Description.equals("moderate rain")) {
                conditiontv.setText("Moderate Rain");
            }
            if (Description.equals("light rain")) {
                conditiontv.setText("Light Rain");
            }
            if (Description.equals("few clouds")) {
                conditiontv.setText("Few Clouds");
                ICON.setImageResource(R.drawable.partly_cloudy);
            }
            if (Description.equals("mist")) {
                conditiontv.setText("Mist");
            }
            if (Description.equals("light snow")) {
                conditiontv.setText("Light Snow");
            }

            return;
        }

        public void FailSafe() {
            Toast.makeText(getApplicationContext(),"Error Connecting to Server", Toast.LENGTH_LONG);
        }
    }
}


