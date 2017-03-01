package com.abubaca.viss.messeme;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MAIN_ACTIVITY";
    private static final int FINE_LOCATION_REQUEST = 0x1;

    private PlaceNoteUtils placeNoteUtils;
    private PlacelistPopulator placelistPopulator;
    private FloatingActionButton addPlaceFloatingActionButton;
    private Boolean notified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setSubtitle(R.string.main_subtite);
        placeNoteUtils = new PlaceNoteUtils(MainActivity.this);
        placelistPopulator = new PlacelistPopulator(this);
        addPlaceFloatingActionButton = (FloatingActionButton)findViewById(R.id.add_place_floating_action_button);
    }

    @Override
    protected void onResume() {
        if(!notified && getIntent().getAction().equals("NOTIFICATION")){
            String place = getIntent().getStringExtra("PLACE");
            placeNoteUtils.viewNote(place);
            notified = true;
        }
        placelistPopulator.populateListview();
        addPlaceFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Starter(MainActivity.this).startMapActivity();
            }
        });
        new Starter(this).startStopFusedLocationService();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_drop_db:
                placeNoteUtils.clearDB();
                break;
            case R.id.action_drop_notes:
                placeNoteUtils.clearNotes();
                break;
            case R.id.action_about:
                new Starter(this).startTextViewer("ABOUT");
                break;
            case R.id.action_help:
                new Starter(this).startTextViewer("HELP");
                break;
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

}


