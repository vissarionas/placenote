package com.abubaca.viss.messeme;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by viss on 1/11/17.
 */

public class LocationBackground extends Service implements LocationListener {

    private final static String TAG = "LOCATION_HANDLER";
    LocationManager locationManager;
    Location lastLocation;
    String provider;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Boolean notesExist = intent.getBooleanExtra("notesExist" , false);
        Log.e(TAG , "Notes exist: "+notesExist.toString());
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        provider = locationManager.getBestProvider(criteria, false);
        lastLocation = locationManager.getLastKnownLocation(provider);
        Log.i(TAG, "Last know location: " + lastLocation);
        if(notesExist) {
            locationManager.requestLocationUpdates(provider, 1000, 0, this);
        }else{
            locationManager.removeUpdates(this);
            Log.i(TAG , "Location requests removed");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "***Location changed: " + location);
//        final MediaPlayer mediaPlayer =MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//        mediaPlayer.start();
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                mediaPlayer.release();
//            }
//        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, "Status changed: " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stopService(){
        stopSelf();
    }
}
