package com.abubaca.viss.messeme;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

/**
 * Created by viss on 1/5/17.
 */

public class FusedBackground extends IntentService implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "FUSED_BACKGROUND";

    GoogleApiClient googleApiClient;
    Location lastKnownLocation;
    LocationRequest locationRequest;

    public FusedBackground() {
        super(null);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG , "service started");
        // Gets data from the incoming Intent
        String dataString = intent.getDataString();
        Log.i(TAG , dataString);

        // Do work here, based on the contents of dataString
        googleApiClient=new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        googleApiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG , "googleapiclient connected");
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setExpirationDuration(8000000);
        locationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient , locationRequest , this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG , "location changed");
    }
}
