package com.abubaca.viss.placenote;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by viss on 2/24/17.
 */

public class Starter {

    private static final String TAG = "STARTER";

    private static final int FINE_LOCATION_REQUEST = 0x1;
    private Activity activity;

    Starter(Activity activity){
        this.activity = activity;
    }

    void startLocationService(){
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST );
            return;
        }
        Intent i = new Intent(activity , LocationService.class);
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

    void startHelpActivity(){
        Intent intent = new Intent(activity , HelpActivity.class);
        activity.startActivity(intent);
    }

    void startPrivacyPolicyWeb(){
        String url = "http://vissariontsitoglou.com/notepin.html";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        activity.startActivity(intent);
    }

    void startEmailClient(){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", activity.getResources().getString(R.string.email), null));
        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
        activity.startActivity(Intent.createChooser(intent, "Contact via email"));
    }




}