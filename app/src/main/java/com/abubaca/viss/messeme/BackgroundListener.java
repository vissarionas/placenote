package com.abubaca.viss.messeme;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

public class BackgroundListener extends Service {

    private LocationManager mLocationManager;
    public LocationListener mLocationListener;
    private static double latitude =0;
    private static double longitude =0;
    private Float accuracy;
    protected SQLiteDatabase db;
    private Location placeLocation , currentLocation;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = openOrCreateDatabase("messeme", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS PLACES(NAME TEXT, LAT TEXT , LGN TEXT)");
        latitude = intent.getDoubleExtra("lat" , 0.0);
        longitude = intent.getDoubleExtra("lgn" , 0.0);
        placeLocation = new Location("");
        Log.i("LATITUDE - LONGITUDE" , latitude +" "+longitude);
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500 , 10 , mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();
                findNearbyLocations();
                Log.i("POSITION: " , latitude+" "+longitude+" "+accuracy);
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

    private void showNotification(String note) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Place Note")
                        .setContentText(note);
        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


//        Sets an ID for the notification
        int mNotificationId = 001;
//        Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());


    }

    private void findNearbyLocations(){
        Cursor cursor = db.rawQuery("SELECT NAME,LAT,LGN FROM PLACES" ,null);
        cursor.moveToFirst();
        Log.i("CURSOR LENGTH " ,  String.valueOf(cursor.getCount()));
        if(cursor.getCount()>0){
            do {
                placeLocation.setLatitude(Double.valueOf(cursor.getString(1)));
                placeLocation.setLongitude(Double.valueOf(cursor.getDouble(2)));
                Log.i("CURSOR LOCATION ", cursor.getString(0) + "  " + cursor.getString(1));
                float distanceInMeters = placeLocation.distanceTo(currentLocation);
                Log.i("DISTANCE ", String.valueOf(distanceInMeters));
                if (distanceInMeters < 100) {
                    Log.i("PLACE NAME " , cursor.getString(0));
                    showNotification(getPlaceNote(cursor.getString(0)));
                }
            }while(cursor.moveToNext());
        }
    }

    private String getPlaceNote(String placeName){
        String note = "";
        Cursor cursor = db.rawQuery("SELECT NOTE FROM NOTES WHERE PLACE = '"+placeName+"'" , null);
        cursor.moveToFirst();
        if(cursor.getCount()>0){
         note =  cursor.getString(0);
        }
        return note;
    }


}