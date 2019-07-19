package com.abubaca.viss.placenote;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Objects;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

/**
 * Created by viss on 1/5/17.
 */

public class LocationService
        extends Service
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long WIFI_INTERVAL = 300000;
    private static final long DATA_INTERVAL = 60000;
    private static final long BATTERY_SAVER_DATA_INTERVAL = 90000;

    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<Placenote> placenotes;
    private DBHandler dbHandler;
    private Boolean wifiConnected = false;
    private Boolean batterySaver = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                wifiConnected = info.isConnected();
                requestLocationUpdates();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        batterySaver = new Preferences().getBatterySaverState(getApplicationContext());
        dbHandler = new DBHandler(this);
        placenotes = dbHandler.getPlacenotesLocationAndProximity();
        if (placenotes.size() > 0) {
            startGoogleApiClient();
            registerNetworkStateReceiver();
        } else {
            removeLocationUpdates();
        }

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();
    }

    private void startGoogleApiClient() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            requestLocationUpdates();
            return;
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    void registerNetworkStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private Long getLocationRequestInterval() {
        return wifiConnected ? WIFI_INTERVAL : batterySaver ? BATTERY_SAVER_DATA_INTERVAL : DATA_INTERVAL;
    }

    private LocationCallback getLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    checkPlacenotesProximity(location);
                }
            }
        };
    }

    private void removeLocationUpdates() {
        LocationCallback locationCallback = getLocationCallback();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private void requestLocationUpdates() {
        removeLocationUpdates();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationRequest locationRequest = getLocationRequestObject();
        LocationCallback locationCallback = getLocationCallback();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private LocationRequest getLocationRequestObject() {
        Long interval = getLocationRequestInterval();
        return new LocationRequest()
                .setInterval(interval)
                .setSmallestDisplacement(3.0f)
                .setExpirationDuration(60000)
                .setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void checkPlacenotesProximity(Location location) {
        if (location.getAccuracy()>500) return;

        Float distance;
        Integer proximity;

        for (Placenote placenote : placenotes) {
            proximity = placenote.getProximity();
            distance = placenote.getLocation().distanceTo(location);
            if (distance < proximity) {
                notifyUser(placenote.getName());
            }
        }
    }

    private void notifyUser(String place) {
        dbHandler.updatePlaceNote(place, null, Constants.NOTE_STATE_ALERTED, 1, null);
        placenotes = dbHandler.getPlacenotesLocationAndProximity();
        if(placenotes.isEmpty()) removeLocationUpdates();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("NOTIFICATION");
        intent.putExtra("PLACE" , place);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationChannel channel;
        channel = new NotificationChannel(
                "placenote",
                "placenote channel",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Placenote notification channel");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "placenote");
        notificationBuilder.setSound(Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification));
        notificationBuilder.setContentTitle(place.toUpperCase());
        notificationBuilder.setContentText(dbHandler.getPlaceNote(place));
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSmallIcon(R.drawable.notification);
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources() , R.mipmap.notification_large_icon));
        notificationBuilder.setLights(Color.RED, 500, 1000);
        notificationBuilder.setVibrate(new long[]{300, 600, 300, 600});
        notificationBuilder.setContentIntent(pendingIntent);

        Notification notification = notificationBuilder.build();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(0, notification);
    }
}

