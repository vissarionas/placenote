package com.abubaca.viss.messeme;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class BackgroundListener extends Service {

    private LocationManager mLocationManager;
    public LocationListener mLocationListener;
    private static double latitude =0;
    private static double longitude =0;
    private Float accuracy;
    protected SQLiteDatabase db;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = openOrCreateDatabase("messeme", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS PLACES(NAME TEXT, LAT TEXT , LGN TEXT)");
        latitude = intent.getDoubleExtra("lat" , 0.0);
        longitude = intent.getDoubleExtra("lgn" , 0.0);
        Log.i("LATITUDE - LONGITUDE" , latitude +" "+longitude);
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500 , 10 , mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();
//                db.execSQL("INSERT INTO PLACES (NAME,LAT,LGN) VALUES ('place' , '"+latitude+"' , '"+longitude+"')");
//                Intent intent = new Intent(getBaseContext(), MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });

        return flags;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}