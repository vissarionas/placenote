package com.abubaca.viss.messeme;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;
    protected Location lastLocation;
    protected SQLiteDatabase db;

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
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startMapActivity();
            }
        });
        db = openOrCreateDatabase("messeme", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS PLACES(NAME TEXT, LAT TEXT , LGN TEXT)");
        final Cursor cursor = db.rawQuery("SELECT NAME FROM PLACES", null);

        ListView list_view = (ListView) findViewById(R.id.list_view);
        ArrayList<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor.getPosition(), cursor.getString(0));
        }
        list_view.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, list));
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                Toast.makeText(getApplicationContext(), cursor.getString(0), Toast.LENGTH_LONG).show();
            }
        });

        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        db = openOrCreateDatabase("messeme", MODE_PRIVATE, null);
        final Cursor cursor = db.rawQuery("SELECT NAME FROM PLACES", null);
        ListView list_view = (ListView) findViewById(R.id.list_view);
        ArrayList<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor.getPosition(), cursor.getString(0));
        }
        list_view.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, list));
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                Toast.makeText(getApplicationContext(), cursor.getString(0), Toast.LENGTH_LONG).show();
            }
        });
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void startMapActivity() {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("lat" , lastLocation.getLatitude());
        intent.putExtra("lgn" , lastLocation.getLongitude());
        startActivity(intent);
    }

    private void startBackgroundListener(){
        Intent intent = new Intent(getBaseContext() , BackgroundListener.class);
        intent.putExtra("name" , "vissarionas");
        startService(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null){
            startBackgroundListener();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
