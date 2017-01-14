package com.abubaca.viss.messeme;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MAIN_ACTIVITY";
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 0x1;
    private LocationManager locationManager;
    private Location lastLocation;

    private DBHandler dbHandler;

    private TextView noPlacesTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHandler = new DBHandler(getApplicationContext());
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        lastLocation = getLastKnownLocation();

        noPlacesTextview = (TextView)findViewById(R.id.no_places_textview);
        noPlacesTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noPlacesTextview.setVisibility(View.INVISIBLE);
                startMapActivity();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noPlacesTextview.setVisibility(View.INVISIBLE);
                startMapActivity();
            }
        });
    }

    private Location getLastKnownLocation(){
        Location location;
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = locationManager.getBestProvider(criteria , false);
        location = locationManager.getLastKnownLocation(provider);
        Log.i(TAG , "Last known location: "+location);
        return location;
    }

    private void populateList() {
        List<PlaceNote> placeNotes = dbHandler.getPlaceNotes();
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
                viewNote(adapter.getPlace(position) , false);
                populateList();
            }
        });
    }

    @Override
    protected void onResume() {
        Log.i(TAG , "onResume()");
        String place = getIntent().getStringExtra("place");
        if (place != null) {
            viewNote(place , true);
        }
        populateList();
        startStopService();
        super.onResume();
    }

    private void startStopService(){
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

    private void startMapActivity() {
        if (lastLocation != null) {
            Log.i(TAG , "Last known location = "+lastLocation.getLatitude()+" "+lastLocation.getLongitude());
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("lat", lastLocation.getLatitude());
            intent.putExtra("lng", lastLocation.getLongitude());
            intent.putExtra("accuracy" , lastLocation.getAccuracy());
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
                        dbHandler.deleteDb();
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
                        dbHandler.updateNote(placeName , "");
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

    private void viewNote(final String placeName , final Boolean fromNotification){
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
        if(dbHandler.getPlaceNote(placeName).isEmpty()){
            btnDelete.setVisibility(View.INVISIBLE);
        }
        btnOk = (Button)editView.findViewById(R.id.btn_ok);
        placeTextView = (TextView)editView.findViewById(R.id.place_text_view);
        noteTextView = (TextView)editView.findViewById(R.id.note_text_view);
        placeTextView.setText(placeName);
        String note = dbHandler.getPlaceNote(placeName);
        if(!note.isEmpty()) {
            noteTextView.setText(note);
            if(fromNotification) {
                dbHandler.setState(placeName, 0);
            }
        }

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
                alertDialog.dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void editNoteDialog(final String place){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Write a note.");

        final EditText noteEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        noteEditText.setLayoutParams(params);
        noteEditText.setText(dbHandler.getPlaceNote(place));
        dialogBuilder.setView(noteEditText);
        dialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String note = noteEditText.getText().toString();
                        dbHandler.updateNote(place , note);
                        if(!note.isEmpty()){
                            dbHandler.setState(place , 1);
                        }
                        MainActivity.this.onResume();
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void startEditPlaceActivity(String placeName){
        Intent intent = new Intent(MainActivity.this, EditPlace.class);
        intent.putExtra("placeName" , placeName);
        startActivity(intent);
    }
}


