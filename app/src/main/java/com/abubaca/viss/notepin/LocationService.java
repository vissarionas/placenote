package com.abubaca.viss.notepin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

/**
 * Created by viss on 1/5/17.
 */

public class LocationService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LOCATION_SERVICE";
    private static final long wifiInterval = 400000;
    private static final long dataInterval = 2000;

    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    List<PlacenoteLocProx> placenoteLocProxList;
    long interval;
    Integer placeProximity;
    float noteCurrentDistance , alertDistance, smallestDistance;
    DBHandler dbHandler;
    Boolean wifiConnected = false;
    Boolean batterySaver = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG , "service started");
        batterySaver = new Preferences().getBatterySaverState(getApplicationContext());
        dbHandler = new DBHandler(this);
        placenoteLocProxList = dbHandler.getNotesNameLocProx();
        if(placenoteLocProxList.size()>0){
            interval = wifiConnected ? wifiInterval : dataInterval;
            startGoogleApiClient();
            registerNetworkStateReciever();
        }else{
            removeLocationUpdates();
        }

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void startGoogleApiClient(){
        Log.i(TAG , "starting google api client");
        if (googleApiClient != null && googleApiClient.isConnected()) {
            requestLocationUpdates(interval , null);
        } else {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG , "google api client connected");
//        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        requestLocationUpdates(interval , null);
    }

    void registerNetworkStateReciever(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                wifiConnected = info.isConnected();
                if(info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    requestLocationUpdates(interval , 1);
                    return;
                }
                if(info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    requestLocationUpdates(interval , null);
                    return;
                }
            }
        }
    };

    private void requestLocationUpdates(long interval , Integer times) {
        removeLocationUpdates();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        if(times!=null) locationRequest.setNumUpdates(times);
        locationRequest.setSmallestDisplacement(5.0f);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, LocationService.this);
            Log.i(TAG , "requested location updates with interval "+interval);
        }
    }

    private void removeLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            Log.i(TAG , "removed location updates");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        float accuracy = location.getAccuracy();
        Log.i(TAG , "location changed. acc: "+accuracy);
        if (accuracy>500) return;

        smallestDistance = 20000;
        alertDistance = accuracy > 100 ? 100 : 30;

        for (PlacenoteLocProx placenoteLocProx : placenoteLocProxList) {
            Location tempLocation = placenoteLocProx.getLocation();
            int tempProximity = placenoteLocProx.getProximity();
            placeProximity = (tempProximity > 200) ? tempProximity : 0;
            noteCurrentDistance = tempLocation.distanceTo(location);
            if (noteCurrentDistance < alertDistance + placeProximity) {
                showNotification(placenoteLocProx.getName());
                break;
            }
            float tempDistance = tempLocation.distanceTo(location);
            smallestDistance = tempDistance<smallestDistance ? tempDistance : smallestDistance;
        }

        interval = wifiConnected ? wifiInterval
                : batterySaver ? (long) (new IntervalGenerator().getInterval(smallestDistance) * 1.5)
                : new IntervalGenerator().getInterval(smallestDistance);
//        if(wifiConnected){
//            interval = 300000;
//        }else{
//            interval = batterySaver ? (long) (new IntervalGenerator().getInterval(smallestDistance) * 1.5)
//                    :new IntervalGenerator().getInterval(smallestDistance);
//        }
        if (interval != locationRequest.getInterval()) requestLocationUpdates(interval , null);
    }

    private void showNotification(String place) {
        dbHandler.updatePlaceNote(place, null, Constants.NOTE_STATE_ALERTED, 1, null);
        placenoteLocProxList = dbHandler.getNotesNameLocProx();
        if(placenoteLocProxList.isEmpty()) removeLocationUpdates();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("NOTIFICATION");
        intent.putExtra("PLACE" , place);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification));
        builder.setContentTitle(place);
        builder.setContentText(dbHandler.getPlaceNote(place));
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.notification);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources() , R.mipmap.notification_large_icon));
        builder.setLights(Color.RED, 1000, 2000);
        builder.setVibrate(new long[]{300, 600, 300, 600});
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}

