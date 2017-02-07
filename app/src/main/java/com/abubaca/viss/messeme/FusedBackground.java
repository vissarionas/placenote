package com.abubaca.viss.messeme;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
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
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "FUSED_BACKGROUND";

    GoogleApiClient googleApiClient;
    Location lastKnownLocation;
    LocationRequest locationRequest;
    List<Location> locations;
    Long interval;
    DBHandler dbHandler;
    Location lastLocation;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dbHandler = new DBHandler(this);
        locations = dbHandler.getNotesLocations();
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
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG , "googleapiclient connected");
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(5000);
        locationRequest.setExpirationDuration(0);
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
        interval = lastLocation!=null ? new IntervalGenerator().getInterval(location , locations):120000;
        Log.i(TAG, "*******Location changed: " + location);
        if(location.getAccuracy()<1000){
            for(int i=0 ; i<locations.size() ; i++){
                float distance = locations.get(i).distanceTo(location);
                if(distance<50){
                    showNotification(dbHandler.getPlaceFromLocation(locations.get(i)));
                    break;
                }
            }
        }
    }

    private void showNotification(String place){
        dbHandler.updateNote(place , null , 3 , 1);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setContentTitle(place);
        builder.setContentText(dbHandler.getPlaceNote(place));
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.raw.notification_icon);
        builder.setLights(Color.GREEN , 2000 , 3000);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0 , notification);
        restartSelf();
    }

    private void restartSelf(){
        Log.e(TAG , "Service restarted");
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 3000,
                restartPendingIntent);
    }
}
