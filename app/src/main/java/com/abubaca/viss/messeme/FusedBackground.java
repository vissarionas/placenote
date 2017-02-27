package com.abubaca.viss.messeme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
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

public class FusedBackground extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "FUSED_BACKGROUND";

    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    List<Location> locations;
    long interval = 5000;
    String place;
    Integer proximity;
    float distance, alertDistance, smallestDistance;
    DBHandler dbHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dbHandler = new DBHandler(this);
        locations = dbHandler.getNotesLocations();
        smallestDistance = 10000;

        if (locations.size() > 0) {
            if (googleApiClient != null) {
                requestLocationUpdates(interval);
            } else {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                googleApiClient.connect();
            }
        } else if (googleApiClient != null) {
            removeLocationUpdates();
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        requestLocationUpdates(interval);
    }

    private void requestLocationUpdates(long interval) {
        removeLocationUpdates();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, FusedBackground.this);
        Log.i(TAG, "Location updates requested with " + interval + " interval");
    }

    private void removeLocationUpdates() {
        Log.i(TAG, "Removed location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Google api client connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Google api client connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        alertDistance = location.getAccuracy() > 100 ? 100 : 20;
        if (locations.isEmpty()) {
            removeLocationUpdates();
            return;
        }

        if (location.getAccuracy()<1000) {
            for (Location noteLocation : locations) {
                place = dbHandler.getPlaceByLocation(noteLocation);
                proximity = (dbHandler.getPlaceProximity(place) > 200) ? dbHandler.getPlaceProximity(place) : 0;
                distance = noteLocation.distanceTo(location);
                if (distance < alertDistance + proximity) {
                    showNotification(place);
                }
                smallestDistance = distance < smallestDistance ? distance : smallestDistance;
            }
        }
        interval = new LocationIntervalGenerator().getInterval(smallestDistance);
        Log.i(TAG, "smallestLocation: " + smallestDistance + " Interval: " + interval + " LocationRequest.getInterval: " + locationRequest.getInterval());
        if (interval != locationRequest.getInterval()) requestLocationUpdates(interval);
    }

    private void showNotification(String place) {
        dbHandler.updatePlaceNote(place, null, NoteState.ALERTED, 1, null);
        locations = dbHandler.getNotesLocations();
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification));
        builder.setContentTitle(place);
        builder.setContentText(dbHandler.getPlaceNote(place));
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.raw.notification_icon);
        builder.setLights(Color.GREEN, 2000, 3000);
        builder.setVibrate(new long[]{300, 600, 300, 800});
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        Log.e(TAG , "onTaskRemoved()");
//        restartSelf();
//        super.onTaskRemoved(rootIntent);
//    }
//
//    private void restartSelf(){
//        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
//        restartServiceTask.setPackage(getPackageName());
//        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        myAlarmService.set(
//                AlarmManager.ELAPSED_REALTIME,
//                SystemClock.elapsedRealtime() + 2000,
//                restartPendingIntent);
//    }
