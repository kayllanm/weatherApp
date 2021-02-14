package com.test.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.test.asyncTask.DownloadImageTask;
import com.test.asyncTask.DownloaderTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements LocationListener {
    protected LocationManager locationManager;
    TextView SuburbName, full_date, temperature, clouds, temp_max_min, feels_like, Error;
    Button refresh_button;
    ImageView weatherIcon;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private ProgressBar spinner;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, ''yy");
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SuburbName = (TextView) findViewById(R.id.SuburbName);
        full_date = (TextView) findViewById(R.id.full_date);
        refresh_button = (Button) findViewById(R.id.refresh_button);
        Error = (TextView) findViewById(R.id.Error);
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);

        requestLocation();
    }

    public void requestLocation(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("hasPermission", "app does not have permission");
            checkLocationPermission();
            return;
        }else{
            refresh_button.setText("Refresh");
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private void requestData(String url) {
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(url);

        AsyncTask<RequestPackage, String, String> execute = new DownloaderTask(
            temperature = (TextView) findViewById(R.id.temperature),
            weatherIcon = (ImageView) findViewById(R.id.weather_icon),
            clouds = (TextView) findViewById(R.id.clouds),
            temp_max_min = (TextView) findViewById(R.id.temp_max_min),
            feels_like = (TextView) findViewById(R.id.feels_like),
            spinner = (ProgressBar) findViewById(R.id.progressBar1))
        .execute(requestPackage);
    }

    public void updateLocation(View view) {
        spinner.setVisibility(View.VISIBLE);
        Log.d(">>>>>>>>>>>>", "update location");
        requestLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    refresh_button.setText("Refresh");
                    Error.setText("");
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
                    }

                } else {

                    spinner.setVisibility(View.GONE);
                    Error.setText(R.string.permission_denied_text);

                }
                return;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
    public void onLocationChanged(Location location) {
        requestData("data/2.5/weather?lat=" + location.getLatitude() + "&lon="+location.getLongitude()+"&appid=559150d9b90b9ea042a41cb1f20c96e3");
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            full_date.setText(R.string.no_locality_text);
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            String locality = addresses.get(0).getLocality();
            String subLocality = addresses.get(0).getSubLocality();
            SuburbName.setText(subLocality +", "+locality);
            String date = dateFormat.format(calendar.getTime());
            full_date.setText(date);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("Latitude","------------------- disable");
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("Latitude","------------------- enable");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("Latitude","------------------- status");
    }
}