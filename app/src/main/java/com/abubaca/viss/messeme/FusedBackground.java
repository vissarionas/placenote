package com.abubaca.viss.messeme;

import android.app.AlarmManager;
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
    List<Location> locations;
    Long interval;
    DBHandler dbHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG , "Fused service started");
        dbHandler = new DBHandler(this);
        locations = dbHandler.getNotesLocations();
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if(locations.size()>0) {
            googleApiClient.connect();
        }else{
            if(googleApiClient.isConnected() || googleApiClient.isConnecting()) googleApiClient.disconnect();
            Log.i(TAG , "Google api client disconected");
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e(TAG , "onTaskRemoved()");
        restartSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        interval = lastKnownLocation!=null?new IntervalGenerator().getInterval(lastKnownLocation , locations):2000;
        requestLocationUpdates(interval);
    }

    private void requestLocationUpdates(long interval){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(3000);
        locationRequest.setMaxWaitTime(5000);
        locationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient , locationRequest , FusedBackground.this);
        Log.i(TAG , "Location updates requested with "+interval+" interval");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG , "Google api client connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG , "Google api client connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG , "location changed");
        long newInterval = new IntervalGenerator().getInterval(location , locations);
        if(newInterval < interval/2){
            Log.e(TAG , "restarted fused location updates");
            interval = newInterval;
            requestLocationUpdates(interval);
            return;
        }
        String place;
        if(location.getAccuracy()<1000){
            for(int i=0 ; i<locations.size() ; i++){
                place = dbHandler.getPlaceFromLocation(location);
                int radius = dbHandler.getPlaceProximity(place);
                float distance = locations.get(i).distanceTo(location) - radius;
//                Log.i(TAG, "*******Location changed: " + location+"\nPlaceRadius: "+radius+"\nDistance: "+distance+"\nInterval: "+interval);
                if(distance<30+radius){
                    dbHandler.updateNote(place , null , 3 , 1);
                    showNotification(place);
                    restartSelf();
                    break;
                }
            }
        }
    }

    private void showNotification(String place){
//        dbHandler.updateNote(place , null , 3 , 1);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setContentTitle(place);
        builder.setContentText(dbHandler.getPlaceNote(place));
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.raw.notification_icon);
        builder.setLights(Color.GREEN , 2000 , 3000);
        builder.setVibrate(new long[] { 500, 500});
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0 , notification);
//        restartSelf();
    }

    private void restartSelf(){
        Log.e(TAG , "Service restarted");
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 2000,
                restartPendingIntent);
    }
}
