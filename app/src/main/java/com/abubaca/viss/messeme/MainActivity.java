package com.abubaca.viss.messeme;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    static final String TAG = "MAIN_ACTIVITY";
    private static final int FINE_LOCATION_REQUEST = 0x1;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;

    private DBHandler dbHandler;

    private TextView noPlacesTextview;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHandler = new DBHandler(getApplicationContext());

        noPlacesTextview = (TextView)findViewById(R.id.no_places_textview);
        noPlacesTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noPlacesTextview.setVisibility(View.INVISIBLE);
                startMapActivity();
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noPlacesTextview.setVisibility(View.INVISIBLE);
                startMapActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        Log.i(TAG , "onResume()");
        populateList();
        createGoogleApiClient();
        startStopService();
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();
        stopLocationUpdates();
    }

    private void createGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        Log.i(TAG , "created google api client");
        if(googleApiClient.isConnected()){
            googleApiClient.reconnect();
        }else{
            googleApiClient.connect();
        }
    }

    protected void createLocationRequest() {
        Log.i(TAG , "created location requests");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setExpirationDuration(60000);
        locationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST);
            return;
        }
        Log.i(TAG , "Started location updates");
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        Log.i(TAG , "Stopped location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient , this);
    }

    private void startStopService(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST );
            return;
        }
        Intent i = new Intent(this , LocationBackground.class);
        startService(i);
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
            case FINE_LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "granted permission for coarse location");
                } else {
                    Log.e(TAG, "permission for location denied");
                }
                return;
            }
        }
    }

    private void populateList() {
        List<PlaceNote> placeNotes = dbHandler.getPlaceNotes();
        final ListView list_view = (ListView) findViewById(R.id.list_view);
        if(placeNotes.size()==0) {
            noPlacesTextview.setVisibility(View.VISIBLE);
            noPlacesTextview.setText("You have no places in your placelist.\n\nClick here or the compass button and set your first place");
        }
        final PlaceNoteAdapter adapter = new PlaceNoteAdapter(getApplicationContext(), placeNotes);
        list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu menu = new PopupMenu(MainActivity.this, view);
                menu.getMenuInflater().inflate(R.menu.edit_place_menu , menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch ((String)item.getTitle()){
                            case "Edit name":
                                editPlaceDialog(adapter.getPlace(position));
                                break;
                            case "View on map":
                                showPlaceMap(adapter.getPlace(position));
                                break;
                            case "Clear note":
                                confirmDropNote(adapter.getPlace(position));
                                break;
                            case "Delete place":
                                confirmDropPlace(adapter.getPlace(position));
                                break;
                        }
                        return true;
                    }
                });
                menu.show();
                return true;
            }
        });

        list_view.setAdapter(adapter);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewNote(adapter.getPlace(position));
            }
        });
    }

    private void startMapActivity() {
        if (lastLocation != null) {
            Log.i(TAG , "Last known location = "+lastLocation.getLatitude()+" "+lastLocation.getLongitude());
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("lat", lastLocation.getLatitude());
            intent.putExtra("lng", lastLocation.getLongitude());
            intent.putExtra("accuracy" , lastLocation.getAccuracy());
            startActivity(intent);
        }
    }

    private void confirmDropDb() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all places?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.clearDb();
                        onResume();
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
                        dbHandler.deleteNotes();
                        onResume();
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
                        dbHandler.updateNote(placeName , "" , 0 , 0);
                        onResume();
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

    private void confirmDropPlace(final String placeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to remove "+placeName+"")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.deletePlace(placeName);
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

    private void editPlaceDialog(final String place){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Change place name.");

        final EditText nameEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        nameEditText.setLayoutParams(params);
        nameEditText.setText(place);
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(20)});
        dialogBuilder.setView(nameEditText);
        dialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHandler.updatePlaceName(place , nameEditText.getText().toString());
                        populateList();
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().getAttributes().verticalMargin = -0.2F;
        dialog.show();
    }

    private void viewNote(final String place){
        if(dbHandler.isNotified(place))dbHandler.updateNote(place , null , 1 , null);
        LayoutInflater inflater = getLayoutInflater();
        View editView = inflater.inflate(R.layout.edit_note , null);
        editView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        Button btnDelete;
        final TextView noteTextView;
        btnDelete = (Button)editView.findViewById(R.id.btn_delete);
        if(dbHandler.getPlaceNote(place).isEmpty()){
            btnDelete.setVisibility(View.INVISIBLE);
        }
//        placeTextView = (TextView)editView.findViewById(R.id.place_text_view);
        noteTextView = (TextView)editView.findViewById(R.id.note_text_view);
//        placeTextView.setText(place);
        String note = dbHandler.getPlaceNote(place);
        if(!note.isEmpty()) {
            noteTextView.setText(note);
        }

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(editView);
        dialogBuilder.setTitle(place);
        final Dialog dialog = dialogBuilder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                populateList();
            }
        });
        dialog.show();

        noteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNoteDialog(place);
                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDropNote(place);
                dialog.dismiss();
            }
        });
    }

    private void editNoteDialog(final String place){
        String prevNote = dbHandler.getPlaceNote(place);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Note");

        final EditText noteEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        noteEditText.setLayoutParams(params);
        noteEditText.setText(prevNote);
        noteEditText.setSelection(prevNote.length());
        noteEditText.setInputType(InputType.TYPE_CLASS_TEXT);

        dialogBuilder.setView(noteEditText);
        dialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String note = noteEditText.getText().toString();
                        if(!note.contentEquals("")){
                            dbHandler.updateNote(place , note , 2 , 0);
                        }else{
                            dbHandler.updateNote(place , note , 0 , 0);
                        }
                        MainActivity.this.onResume();
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().getAttributes().verticalMargin = -0.2F;
        dialog.show();
    }

    private void showPlaceMap(String placeName){
        Intent intent = new Intent(MainActivity.this, ViewPlaceMap.class);
        intent.putExtra("placeName" , placeName);
        startActivity(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG , "GoogleAPIClient connected");
        createLocationRequest();
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
        lastLocation = location;
        fab.setVisibility(View.VISIBLE);
    }

}


