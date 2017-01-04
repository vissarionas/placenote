package com.abubaca.viss.messeme;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import org.w3c.dom.Text;

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

    private TextView noPlacesTextview;
    private Boolean notificationSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        noPlacesTextview = (TextView)findViewById(R.id.no_places_textview);
        noPlacesTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noPlacesTextview.setVisibility(View.INVISIBLE);
                startMapActivity();
            }
        });
        dbHandler = new DBHandler(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noPlacesTextview.setVisibility(View.INVISIBLE);
                startMapActivity();
            }
        });
        intervalGenerator = new IntervalGenerator();
    }

    private void populateList() {
        List<PlaceNote> placeNotes= dbHandler.getPlaceNotes(dbHandler);
        ListView list_view = (ListView) findViewById(R.id.list_view);
        if(placeNotes.size()==0) {
            noPlacesTextview.setVisibility(View.VISIBLE);
            noPlacesTextview.setText("You have no places in your placelist.\n\nClick this or the compass button and set your first place");
        }
        final PlaceNoteAdapter adapter = new PlaceNoteAdapter(getApplicationContext(), placeNotes);
        list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                startEditPlaceActivity(adapter.getPlace(position));
                populateList();
                return true;
            }
        });

        list_view.setAdapter(adapter);

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewNote(adapter.getPlace(position));
                populateList();
            }
        });
    }

    @Override
    protected void onResume() {
        String placeName = getIntent().getStringExtra("placeName");
        if(placeName!=null){
            viewNote(placeName);
        }
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
        builder.setMessage("Are you sure you want to delete "+placeName+" note?")
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
        Log.i(TAG, "google api connected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            findNearbyLocations(location);
        }

    }

    private void findNearbyLocations(Location currentLocation) {
        List<Location> noteLocations = dbHandler.getNotesLocations(dbHandler);
        if(noteLocations.size()>0){
            for(int i =0 ; i < noteLocations.size() ; i++){
                float distance = noteLocations.get(i).distanceTo(currentLocation);
                Log.i(TAG , "DISTANCE: "+distance);
                if (distance<50) {
                    if(!notificationSuccess) {
                        showNotification(dbHandler.getPlaceFromLocation(dbHandler, noteLocations.get(i)));
                    }
                }
            }
        }



    }

    private void showNotification(String place) {
        notificationSuccess = true;
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(place)
                        .setContentText(dbHandler.getPlaceNote(dbHandler , place))
                        .setAutoCancel(true)
                        .setContentInfo("messeme")
                        .setLights(Color.GREEN , 1000 , 3000)
                        .setSound(sound);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.putExtra("placeName", place);
        resultIntent.putExtra("NotificationSuccess" , true);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private void viewNote(final String placeName){
        LayoutInflater inflater = getLayoutInflater();
        View editView = inflater.inflate(R.layout.edit_note , null);
        editView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setView(editView);

        Button btnDelete , btnOk;
        final TextView placeTextView, noteTextView;
        btnDelete = (Button)editView.findViewById(R.id.btn_delete);
        if(dbHandler.getPlaceNote(dbHandler, placeName).isEmpty()){
            btnDelete.setVisibility(View.INVISIBLE);
        }
        btnOk = (Button)editView.findViewById(R.id.btn_ok);
        placeTextView = (TextView)editView.findViewById(R.id.place_text_view);
        noteTextView = (TextView)editView.findViewById(R.id.note_text_view);

        noteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNoteDialog(placeName);
                alertDialog.dismiss();
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
                onResume();
                alertDialog.dismiss();
            }
        });

        placeTextView.setText(placeName);
        String note = dbHandler.getPlaceNote(dbHandler , placeName);
        if(!note.isEmpty()) {
            noteTextView.setText(note);
        }

        alertDialog.show();

    }

    private void editNoteDialog(final String place){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Write a note.");

        final EditText noteEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        noteEditText.setLayoutParams(params);
        noteEditText.setText(dbHandler.getPlaceNote(dbHandler, place));
        alertDialog.setView(noteEditText);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHandler.updateNote(dbHandler, place , noteEditText.getText().toString());
                        populateList();
                        MainActivity.this.onResume();
                    }
                });
        alertDialog.show();
    }

    private void startEditPlaceActivity(String placeName){
        Intent intent = new Intent(MainActivity.this, EditPlace.class);
        intent.putExtra("placeName" , placeName);
        startActivity(intent);
    }
}


