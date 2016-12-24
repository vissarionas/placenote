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

public class BackgroundListener extends Service {

    final String TAG = "BACKGROUND_SERVICE";
    private LocationManager mLocationManager;
    public LocationListener mLocationListener;
    private static double latitude =0;
    private static double longitude =0;
    private Float accuracy;
    protected SQLiteDatabase db;
    private Location placeLocation , currentLocation;

    @Override
    public void onDestroy() {
        Log.i(TAG , "background service destroyed");
        mLocationManager.removeUpdates(mLocationListener);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG , "started background service");
        db = openOrCreateDatabase("messeme", MODE_PRIVATE, null);
        placeLocation = new Location("");
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000 , 10 , mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();
                findNearbyLocations();
                Log.i(TAG , "location changed");
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

    private void showNotification(String title, String note) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(title)
                        .setContentText(note);
        Intent resultIntent = new Intent(this, NoteActivity.class);
        resultIntent.putExtra("placeName" , title);

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
        Cursor cursor = db.rawQuery("SELECT * FROM PLACENOTES WHERE NOTE NOT LIKE ''" ,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            placeLocation.setLatitude(Double.valueOf(cursor.getString(1)));
            placeLocation.setLongitude(Double.valueOf(cursor.getString(2)));
            float distance = placeLocation.distanceTo(currentLocation);
            if(distance<200){
                showNotification(cursor.getString(0) , cursor.getString(3));
            }
        }
    }
}