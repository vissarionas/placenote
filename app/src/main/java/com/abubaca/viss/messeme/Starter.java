package com.abubaca.viss.messeme;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by viss on 2/24/17.
 */

public class Starter {

    private static final String TAG = "STARTER";
    private static final int FINE_LOCATION_REQUEST = 0x1;
    private Activity activity;

    public Starter(Activity activity){
        this.activity = activity;
    }

    void startStopFusedLocationService(){
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST );
            return;
        }
        Intent i = new Intent(activity , FusedBackground.class);
        activity.startService(i);
    }

    void startMapActivity() {
        Intent intent = new Intent(activity, MapActivity.class);
        activity.startActivity(intent);
    }

    void startViewPlaceActivity(String placeName){
        Intent intent = new Intent(activity, ViewPlaceActivity.class);
        intent.putExtra("PLACENAME" , placeName);
        activity.startActivity(intent);
    }

    void startTextViewer(String action){
        Intent intent = new Intent(activity , AboutHelpViewer.class);
        intent.setAction(action);
        activity.startActivity(intent);
    }
}
