package com.example.JihadIntel;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class WorkInBackground extends AsyncTask<Context, Void, Void> {

    private static final String TAG = "WorkInBackground";
    Context context;
    LocationManager locationGPS;
    Location loc;
    double latitude, longitude;
    String location_coordinates;

    @Override
    protected Void doInBackground(Context... contexts) {

        // work on not calling below line on coming back or reloading verify if it is already created or not
        if (Looper.myLooper() == null)
            Looper.prepare();


        context = contexts[0];
        locationGPS = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        Log.d(TAG, "doInBackground: " + context);
        get_gps();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        int waitTime = 30000;
        Log.d(TAG, "onPostExecute: calling");
        Runnable r = new Runnable() {
            @Override
            public void run(){
                if(GlobalData.userData.getId()!=null)
                    new WorkInBackground().execute(context);
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, waitTime);
    }

    public void get_gps() {
        Log.d(TAG, "get_gps:  hello");

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationGPS.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "get_gps: " + location);
            if (location!=null)
                GeneralMethods.upload_gps(location);
        } else {
            Log.d(TAG, "get_gps:  else");
            //ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
}
