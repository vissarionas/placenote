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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class MainActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    static final String TAG = "MAIN_ACTIVITY";
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 0x2;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0x3;

    private static GoogleApiClient mGoogleApiClient;
    private Location currentLocation;
    private LocationRequest mLocationRequest;
    private float accuracy;

    protected SQLiteDatabase db;

    private int[] priority = {PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_HIGH_ACCURACY};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMapActivity();
            }
        });

        db = openOrCreateDatabase("messeme", MODE_PRIVATE, null);
        populateList();
        createGoogleApiClient();
//        Log.i(TAG , String.valueOf(Build.VERSION.SDK_INT));
//        if(Build.VERSION.SDK_INT>22){
//            checkPermission();
//        }
    }

    private void populateList() {
        db.execSQL("CREATE TABLE IF NOT EXISTS PLACENOTES(PLACE TEXT , LAT TEXT , LGN TEXT , NOTE TEXT)");
        final Cursor cursor = db.rawQuery("SELECT * FROM PLACENOTES", null);
        ListView list_view = (ListView) findViewById(R.id.list_view);
        String[] places = new String[cursor.getCount()];
        String[] notes = new String[cursor.getCount()];
        while (cursor.moveToNext()) {
            places[cursor.getPosition()] = cursor.getString(0);
            notes[cursor.getPosition()] = cursor.getString(3);
        }
        PlaceNoteAdapter adapter = new PlaceNoteAdapter(getApplicationContext() , places, notes);
        list_view.setAdapter(adapter);

        list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                confirmDropPlace(cursor.getString(0));
                return true;
            }
        });

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                showEditNoteDialog(cursor.getString(0));
            }
        });


    }

    private void viewReport() {
        String report = "PLACE : NOTE\r\n\n";
        Cursor cursor = db.rawQuery("SELECT * FROM PLACENOTES WHERE NOTE NOT LIKE ''", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                String row = cursor.getString(0) + " : " + cursor.getString(3);
                report = report.concat(row + "\r\n\n");
            } while (cursor.moveToNext());
            Toast.makeText(getApplicationContext(), report, Toast.LENGTH_LONG).show();
        }
    }

    private Boolean notesExist() {
        Cursor cursor = db.rawQuery("SELECT NOTE FROM PLACENOTES WHERE NOTE NOT LIKE ''", null);
        return (cursor.getCount() > 0) ? true : false;
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();
        populateList();
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
        if (id == R.id.action_view_report) {
            viewReport();
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
                    Log.i(TAG, "permission for location denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "granted permission for fine location");
                    // permission was granted, yay! Do the
                    // location-related task you need to do.

                } else {
                    Log.i(TAG, "permission for location denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
                // other 'case' lines to check for other
                // permissions this app might request
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
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setExpirationDuration(1000 * 60 * 60 * 24);
        mLocationRequest.setPriority(priority[0]);
        Log.e(TAG, "priority = " + priority[0]);

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_COARSE_LOCATION);

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    private void startMapActivity() {
        if (currentLocation != null) {
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
//                        db.execSQL("DROP TABLE IF EXISTS PLACES");
                        db.execSQL("DROP TABLE IF EXISTS PLACENOTES");
                        populateList();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
//                        db.execSQL("DELETE FROM NOTES");
                        db.execSQL("UPDATE PLACENOTES SET NOTE=''");
                        populateList();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                        db.delete("PLACENOTES", "PLACE='" + place + "'", null);
                        populateList();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "connected with google api");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_COARSE_LOCATION);

                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
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
        accuracy = location.getAccuracy();
        Log.i(TAG, "location changed. Accuracy = " + accuracy);
        if (notesExist()) {
            findNearbyLocations(location);
        }

    }

    private void findNearbyLocations(Location currentLocation) {
        Location noteLocation = new Location("");
        Cursor cursor = db.rawQuery("SELECT * FROM PLACENOTES WHERE NOTE NOT LIKE ''", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            noteLocation.setLatitude(Double.valueOf(cursor.getString(1)));
            noteLocation.setLongitude(Double.valueOf(cursor.getString(2)));
            float distance = noteLocation.distanceTo(currentLocation);
            if (distance < 100 && currentLocation.getAccuracy()<300) {
                showNotification(cursor.getString(0), cursor.getString(3));
            }
        }
    }

    private void showNotification(String title, String note) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(title)
                        .setContentText(note);
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("placeName", title);

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

    private void showEditNoteDialog(final String placeName) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Edit your place note");
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        final Cursor cursor = db.rawQuery("SELECT NOTE FROM PLACENOTES WHERE PLACE='"+placeName+"'" ,null);
        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            input.setText(cursor.getString(0));
        }
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        db.execSQL("UPDATE PLACENOTES SET NOTE='"+input.getText().toString()+"' WHERE PLACE='"+placeName+"'");
                        if(!notesExist()){
                            if(mGoogleApiClient.isConnected()){
                                mGoogleApiClient.disconnect();                            }
                        }
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

}


