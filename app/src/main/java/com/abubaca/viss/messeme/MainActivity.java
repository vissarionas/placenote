package com.abubaca.viss.messeme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MAIN_ACTIVITY";
    private static final int FINE_LOCATION_REQUEST = 0x1;

    private DBHandler dbHandler;
    private PlaceNoteUtils placeNoteUtils;
    private PlacelistPopulator placelistPopulator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG , "onCreate()");
        setContentView(R.layout.activity_main);
        getSupportActionBar().setSubtitle(R.string.main_subtite);
        dbHandler = new DBHandler(getApplicationContext());
        placeNoteUtils = new PlaceNoteUtils(MainActivity.this);
        placelistPopulator = new PlacelistPopulator(this);
    }

    @Override
    protected void onResume() {
        placelistPopulator.populate();
        startStopService();
        super.onResume();
    }

    private void startStopService(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST );
            return;
        }
        Intent i = new Intent(this , FusedBackground.class);
        startService(i);
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


