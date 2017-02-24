package com.abubaca.viss.messeme;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MAIN_ACTIVITY";
    private static final int FINE_LOCATION_REQUEST = 0x1;

    private PlaceNoteUtils placeNoteUtils;
    private PlacelistPopulator placelistPopulator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setSubtitle(R.string.main_subtite);
        placeNoteUtils = new PlaceNoteUtils(MainActivity.this);
        placelistPopulator = new PlacelistPopulator(this);
    }

    @Override
    protected void onResume() {
        placelistPopulator.populateListview();
        new Starter(this).startStopFusedLocationService();
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) new Starter(this).startStopFusedLocationService();
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


