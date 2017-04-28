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
    private static final long WIFI_INTERVAL = 500000;
    private static final long DATA_INTERVAL = 2000;

    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    List<Placenote> placenotes;
    long interval;
    Integer placeProximity;
    float noteCurrentDistance, smallestDistance;
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
        batterySaver = new Preferences().getBatterySaverState(getApplicationContext());
        dbHandler = new DBHandler(this);
        placenotes = dbHandler.getPlacenotesLocationProximity();
        if(placenotes.size()>0){
            interval = wifiConnected ? WIFI_INTERVAL : DATA_INTERVAL;
            startGoogleApiClient();
            registerNetworkStateReciever();
        }else{
            removeLocationUpdates();
        }

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void startGoogleApiClient(){
        if (googleApiClient != null && googleApiClient.isConnected()) {
            requestLocationUpdates(interval);
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
        requestLocationUpdates(interval);
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
                interval = wifiConnected ? WIFI_INTERVAL : DATA_INTERVAL;
                requestLocationUpdates(interval);
            }
        }
    };

    private void requestLocationUpdates(long interval) {
        removeLocationUpdates();
        float smallestDisplacement = interval > 30000 ? 20.0f : interval > 15000 ? 10.0f : 1.0f;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        locationRequest.setSmallestDisplacement(smallestDisplacement);
        locationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, LocationService.this);
        }
    }

    private void removeLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
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
        if (location.getAccuracy() > 500) return;
        smallestDistance = 20000;

        for (Placenote placenote : placenotes) {
            Location tempLocation = placenote.getLocation();
            placeProximity = placenote.getProximity();
            noteCurrentDistance = tempLocation.distanceTo(location);
            if (noteCurrentDistance < placeProximity) {
                showNotification(placenote.getName());
                break;
            }
            float tempDistance = tempLocation.distanceTo(location);
            smallestDistance = tempDistance<smallestDistance ? tempDistance : smallestDistance;
        }

        interval = wifiConnected ? WIFI_INTERVAL
                : batterySaver ? (long) (new IntervalGenerator().getInterval(smallestDistance) * 1.5)
                : new IntervalGenerator().getInterval(smallestDistance);

        if (interval != locationRequest.getInterval()) requestLocationUpdates(interval);
    }

    private void showNotification(String place) {
        dbHandler.updatePlaceNote(place, null, Constants.NOTE_STATE_ALERTED, 1, null);
        placenotes = dbHandler.getPlacenotesLocationProximity();
        if(placenotes.isEmpty()) removeLocationUpdates();

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
        builder.setLights(Color.RED, 500, 1000);
        builder.setVibrate(new long[]{300, 600, 300, 600});
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}

