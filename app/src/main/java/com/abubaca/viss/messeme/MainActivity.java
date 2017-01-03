package com.abubaca.viss.messeme;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

public class MainActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    static final String TAG = "MAIN_ACTIVITY";
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 0x2;

    private static GoogleApiClient mGoogleApiClient;
    static Location currentLocation;
    private LocationRequest mLocationRequest;
    private float accuracy;
    private IntervalGenerator intervalGenerator;

    public long interval;

    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHandler = new DBHandler(getApplicationContext());

//        dbHandler.insertToDb(dbHandler , "home" , "27.35" , "34.28" , "this is a note");
//        dbHandler.insertToDb(dbHandler , "buddy" , "27.35" , "34.28" , "this is another note");
//        dbHandler.insertToDb(dbHandler , "work" , "27.35" , "34.28" , "this is a third note");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMapActivity();
            }
        });
        intervalGenerator = new IntervalGenerator();
//        Log.i(TAG , String.valueOf(Build.VERSION.SDK_INT));
//        if(Build.VERSION.SDK_INT>22){
//            checkPermission();
//        }
    }

    private void populateList() {
//        final Cursor cursor = db.rawQuery("SELECT PLACE,NOTE FROM PLACENOTES", null);
//        places = new String[cursor.getCount()];
//        notes = new String[cursor.getCount()];
//        while (cursor.moveToNext()) {
//            places[cursor.getPosition()] = cursor.getString(0);
//            notes[cursor.getPosition()] = cursor.getString(1);
//        }
        ListView list_view = (ListView) findViewById(R.id.list_view);
        final PlaceNoteAdapter adapter = new PlaceNoteAdapter(getApplicationContext() , dbHandler.getPlaceNotes(dbHandler));
        list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                confirmDropPlace(adapter.getPlace(position));
                populateList();
                return true;
            }
        });

        list_view.setAdapter(adapter);

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editNote(adapter.getPlace(position).toString());
                populateList();
            }
        });
    }

    @Override
    protected void onResume() {
        dbHandler.deleteDb(dbHandler);
        dbHandler.insertToDb(dbHandler , "home" , "27.35" , "34.28" , "this is a note");
        dbHandler.insertToDb(dbHandler , "buddy" , "27.35" , "34.28" , "");
        dbHandler.insertToDb(dbHandler , "work" , "27.35" , "34.28" , "this is a third note");
        dbHandler.insertToDb(dbHandler , "coffee shop" , "27.24" , "34.28" , "");
        dbHandler.insertToDb(dbHandler , "mountain" , "27.35" , "34.28" , "this is another note");

        dbHandler.noteCounter(dbHandler);
        intervalGenerator = new IntervalGenerator();
        Log.i(TAG , "onResume");
        populateList();
        createGoogleApiClient();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_drop_db) {
            confirmDropDb();
            return true;
        }
        if (id == R.id.action_drop_notes) {
            confirmDropNotes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "granted permission for coarse location");
                    // permission was granted, yay! Do the
                    // location-related task you need to do.

                } else {
                    Log.e(TAG, "permission for location denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(dbHandler.noteCounter(dbHandler)==0){
            if(mGoogleApiClient.isConnected()){
                mGoogleApiClient.disconnect();
                Log.i(TAG , "onPause() google api client disconected");
            }
        }
    }

    private void createGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        Log.i(TAG , "created google api client");
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.reconnect();
        }else{
            mGoogleApiClient.connect();
        }
    }

    protected void createLocationRequest() {
        Log.i(TAG , "created location requests with interval "+String.valueOf(interval));
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(4000);
        mLocationRequest.setExpirationDuration(1000 * 60 * 60 * 24);
        mLocationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.i(TAG, "location settings ok");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;

                }
            }
        });
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
            return;
        }
        Log.i(TAG , "started location updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void startMapActivity() {
        if (currentLocation != null) {
            Log.i(TAG , "current location = "+currentLocation.getLatitude()+" "+currentLocation.getLongitude());
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("lat", currentLocation.getLatitude());
            intent.putExtra("lgn", currentLocation.getLongitude());
            intent.putExtra("accuracy", accuracy);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "merry christmas", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDropDb() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all places?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.deleteDb(dbHandler);
                        populateList();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void confirmDropNotes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all notes?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.deleteNotes(dbHandler);
                        populateList();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void confirmDropNote(final String placeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all notes?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.updateNote(dbHandler , placeName , "");
                        populateList();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void confirmDropPlace(final String place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this place?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.deletePlace(dbHandler , place);
                        populateList();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
        Log.i(TAG, "google api connected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG , "permission needed");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
            return;
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(dbHandler.noteCounter(dbHandler)>0) {
            startLocationUpdates();
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
        currentLocation = location;
        interval = intervalGenerator.getInterval(currentLocation , dbHandler.getNotesLocations(dbHandler));
        if(interval<3000){
            interval = 3000;
        }
        accuracy = location.getAccuracy();
        Log.i(TAG, "location changed. Accuracy = " + accuracy);
        if(dbHandler.noteCounter(dbHandler)>0) {
            //findNearbyLocations(location);
        }

    }

//    private void findNearbyLocations(Location currentLocation) {
//        Location noteLocation = new Location("");
//        Cursor cursor = db.rawQuery("SELECT * FROM PLACENOTES WHERE NOTE NOT LIKE ''", null);
//        cursor.moveToFirst();
//        if (cursor.getCount()>0) {
//            while(cursor.moveToNext()){
//                noteLocation.setLatitude(Double.valueOf(cursor.getString(1)));
//                noteLocation.setLongitude(Double.valueOf(cursor.getString(2)));
//                float distance = noteLocation.distanceTo(currentLocation);
//                if (distance < 100 && currentLocation.getAccuracy()<300) {
//                    showNotification(cursor.getString(0), cursor.getString(3));
//                }
//            }
//        }
//        cursor.close();
//    }

    private void showNotification(String place, String note) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(place)
                        .setContentText(note)
                        .setOnlyAlertOnce(true)
                        .setTicker("this is a ticker");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("placeName", place);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

//        Sets an ID for the notification
        int mNotificationId = 001;
//        Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private void editNote(final String placeName){
        LayoutInflater inflater = getLayoutInflater();
        View editView = inflater.inflate(R.layout.edit_note , null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setView(editView);

        Button btnEdit , btnDelete , btnOk;
        final TextView placeTextView, noteTextView;
        btnEdit = (Button)editView.findViewById(R.id.btn_edit);
        btnDelete = (Button)editView.findViewById(R.id.btn_delete);
        btnOk = (Button)editView.findViewById(R.id.btn_ok);
        placeTextView = (TextView)editView.findViewById(R.id.place_text_view);
        noteTextView = (TextView)editView.findViewById(R.id.note_text_view);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDropNote(placeName);
                populateList();
                alertDialog.dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        placeTextView.setText(placeName);
        noteTextView.setText(dbHandler.getPlaceNote(dbHandler , placeName));

        alertDialog.show();

    }
}


