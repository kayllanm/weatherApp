package com.test.asyncTask;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.test.weatherapp.HttpManager;
import com.test.weatherapp.R;
import com.test.weatherapp.RequestPackage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloaderTask extends AsyncTask<RequestPackage, String, String> {
    TextView temperature, clouds, temp_max_min, feels_like;
    ImageView weatherIcon;
    ProgressBar spinner;

    public DownloaderTask(TextView dtTemperature, ImageView dtWeatherIcon, TextView dtClouds, TextView dtTemp_max_min, TextView dtFeels_like, ProgressBar dtSpinner) {
        this.temperature = dtTemperature;
        this.weatherIcon = dtWeatherIcon;
        this.clouds = dtClouds;
        this.temp_max_min = dtTemp_max_min;
        this.feels_like = dtFeels_like;
        this.spinner = dtSpinner;
    }

    @Override
    protected String doInBackground(RequestPackage... params) {
        return HttpManager.getData(params[0]);
    }

    //The String that is returned in the doInBackground() method is sent to the
    // onPostExecute() method below. The String should contain JSON data.
    @Override
    protected void onPostExecute(String result) {
        try {
            //We need to convert the string in result to a JSONObject
            JSONObject jsonObject = new JSONObject(result);

            //The “ask” value below is a field in the JSON Object that was
            //retrieved from the BitcoinAverage API. It contains the current
            //bitcoin price
            JSONArray weather = jsonObject.getJSONArray("weather");
            JSONObject main = jsonObject.getJSONObject("main");
            String icon = null, description = null;
            double feel_like, temp_max, temp_min;
            feel_like = main.getInt("feels_like") - 273.15F;
            temp_max = main.getInt("temp_max") - 273.15F;
            temp_min = main.getInt("temp_min") - 273.15F;
            for(int i=0; i<weather.length(); i++){
                JSONObject row = weather.getJSONObject(i);
                icon = row.getString("icon");
                description = row.getString("description");
            }
            try{
                AsyncTask<String, Void, Bitmap> execute = new DownloadImageTask((ImageView) weatherIcon)
                        .execute("https://openweathermap.org/img/wn/"+icon+"@2x.png");
                // R.id.imageView  -> Here imageView is id of your ImageView
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }

            //Now we can use the value in the mPriceTextView
            temperature.setText((Math.round(feel_like))+ " \u2103");
            clouds.setText(description);
            temp_max_min.setText((Math.round(temp_max))+ " \u2103 / "+ (Math.round(temp_min))+ " \u2103" );
            feels_like.setText("Feels like "+(Math.round(feel_like))+ " \u2103");
            spinner.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
