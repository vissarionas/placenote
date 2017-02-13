//package com.abubaca.viss.messeme;
//
//import android.app.AlarmManager;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.media.RingtoneManager;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.os.SystemClock;
//import android.support.annotation.Nullable;
//import android.support.v7.app.NotificationCompat;
//import android.util.Log;
//
//import java.util.List;
//
///**
// * Created by viss on 1/11/17.
// */
//
//public class LocationBackground extends Service implements LocationListener {
//
//    private final static String TAG = "LOCATION_HANDLER";
//    LocationManager locationManager;
//    String provider;
//    List<Location> locations;
//    Long interval;
//    DBHandler dbHandler;
//    Location lastLocation;
//
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        dbHandler = new DBHandler(this);
//        locations = dbHandler.getNotesLocations();
//        Log.i(TAG, "Service started");
//
//        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
//        provider = locationManager.getBestProvider(criteria, false);
//
//        lastLocation = locationManager.getLastKnownLocation(provider);
//        interval = lastLocation!=null ? new IntervalGenerator().getInterval(lastLocation , locations):120000;
//
//        if(locations.size()>0) {
//            Log.i(TAG , "started location updates with interval: "+interval);
//            locationManager.requestLocationUpdates(provider, interval , 0 , this);
//        }else{
//            locationManager.removeUpdates(this);
//            Log.i(TAG , "Location requests removed");
//        }
//        super.onStartCommand(intent, flags, startId);
//        return START_STICKY;
//    }
//
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        Log.e(TAG , "onTaskRemoved()");
//        restartSelf();
//        super.onTaskRemoved(rootIntent);
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        long newInterval = new IntervalGenerator().getInterval(location , locations);
//        if(newInterval<interval){
//            Log.e(TAG , "restarted location updates");
//            locationManager.removeUpdates(this);
//            locationManager.requestLocationUpdates(provider , newInterval , 0 , this);
//            return;
//        }
//        if(location.getAccuracy()<1000){
//            for(int i=0 ; i<locations.size() ; i++){
//                int radius = dbHandler.getPlaceProximity(dbHandler.getPlaceFromLocation(locations.get(i)));
//                float distance = locations.get(i).distanceTo(location) - radius;
//                Log.i(TAG, "*******Location changed: " + location+"\nPlaceRadius: "+radius+"\nDistance: "+distance+"\nInterval: "+interval);
//                if(distance<50+radius){
//                    showNotification(dbHandler.getPlaceFromLocation(locations.get(i)));
//                    break;
//                }
//            }
//        }
//
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//        Log.i(TAG, "Status changed: " + status);
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//        Log.i(TAG , provider+" enabled");
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//        Log.e(TAG , provider+" disabled");
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//
//    private void showNotification(String place){
//        dbHandler.updateNote(place , null , 3 , 1);
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//        builder.setContentTitle(place);
//        builder.setContentText(dbHandler.getPlaceNote(place));
//        builder.setAutoCancel(true);
//        builder.setSmallIcon(R.raw.notification_icon);
//        builder.setLights(Color.GREEN , 2000 , 3000);
//        builder.setVibrate(new long[] { 500, 1000, 500, 1000});
//        builder.setContentIntent(pendingIntent);
//
//        Notification notification = builder.build();
//        NotificationManager notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(0 , notification);
//        restartSelf();
//    }
//
//    private void restartSelf(){
//        Log.e(TAG , "Service restarted");
//        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
//        restartServiceTask.setPackage(getPackageName());
//        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        myAlarmService.set(
//                AlarmManager.ELAPSED_REALTIME,
//                SystemClock.elapsedRealtime() + 2000,
//                restartPendingIntent);
//    }
//
//}
