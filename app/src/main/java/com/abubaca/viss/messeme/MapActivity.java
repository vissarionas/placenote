package com.abubaca.viss.messeme;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap map;
    private Marker marker;
    private String placeAddress = null;
    private Double lat , lng;
    private int proximity = 0;
    private Button addPlaceButton;
    private DBHandler dbHandler;
    private GoogleApiClient googleApiClient;
    private Location lastKnownLocation;
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 0x1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0x2;
    protected static final String TAG = "MAP_ACTIVITY";
    private IntentFilter filter = new IntentFilter("GET_ADDRESS");
    private LinearLayout mapLayout, pbLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setSubtitle(R.string.maps_subtitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbHandler = new DBHandler(getApplicationContext());
        addPlaceButton = (Button)findViewById(R.id.add_place_button);
        mapLayout = (LinearLayout)findViewById(R.id.map_layout);
        pbLayout = (LinearLayout)findViewById(R.id.pb_layout);
        connectGoogleApiClient();
    }

    @Override
    protected void onResume() {
        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaceDialog(placeAddress);
            }
        });
        registerReceiver(broadcastReceiver , filter);
        connectGoogleApiClient();
        super.onResume();
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu , menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_search:
                searchIntent();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectGoogleApiClient() {
        if(googleApiClient!=null) {
            googleApiClient.connect();
        }else{
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    private void requestLocationUpdates(){
        removeLocationUpdates();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient , locationRequest , MapActivity.this);
    }

    private void removeLocationUpdates(){
        Log.i(TAG , "Removed location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient , this);
    }

    protected void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
            return;
        }
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void searchIntent(){
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                removeLocationUpdates();
                Place place = PlaceAutocomplete.getPlace(this, data);
                Location placeLocation = new Location("");
                placeLocation.setLatitude(place.getLatLng().latitude);
                placeLocation.setLongitude(place.getLatLng().longitude);
                if(place.getViewport()!=null) {
                    Location northeastBound = new Location("");
                    northeastBound.setLatitude(place.getViewport().northeast.latitude);
                    northeastBound.setLongitude(place.getViewport().northeast.longitude);
                    proximity = Math.round(northeastBound.distanceTo(placeLocation));
                    Log.i(TAG , "Proximity: "+proximity);
                }

                lat = placeLocation.getLatitude();
                lng = placeLocation.getLongitude();
                float zoom = (proximity<200)?18.0f:
                                (proximity<500)?17.0f:
                                    (proximity<1000)?16.0f:
                                        (proximity<5000)?15.0f:
                                            (proximity<10000)?14.0f:13.0f;
                moveMapPlaceMarker(placeLocation , zoom);

                placeAddress = place.getName().toString();
                addPlaceButton.setVisibility(View.VISIBLE);
                String addPlace = getResources().getString(R.string.add_place);
                addPlaceButton.setText(String.format(addPlace , placeAddress));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            placeAddress = intent.getStringExtra("ADDRESS");
            addPlaceButton.setVisibility(View.VISIBLE);
            String addPlace = getResources().getString(R.string.add_place);
            addPlaceButton.setText(String.format(addPlace , placeAddress));
        }
    };

    private void moveMapPlaceMarker(Location location , float zoom){
        LatLng tempLatLng = new LatLng(location.getLatitude() , location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(tempLatLng , zoom));
        if(marker!=null)marker.remove();
        marker = map.addMarker(new MarkerOptions().position(tempLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    private void requestAddress(Double lat , Double lng){
        Intent intent = new Intent(MapActivity.this, AddressGenerator.class);
        intent.putExtra("lat" , lat);
        intent.putExtra("lng" , lng);
        startService(intent);
    }

    private void doTheJob(Location location){
        this.lat = location.getLatitude();
        this.lng = location.getLongitude();
        this.pbLayout.setVisibility(View.INVISIBLE);
        moveMapPlaceMarker(location , 18.0f);
        requestAddress(location.getLatitude() , location.getLongitude());
        Log.i(TAG , "JobDone: "+location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if(lastKnownLocation!=null && locationIsFresh(lastKnownLocation)){
            doTheJob(lastKnownLocation);
        }
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                lastKnownLocation.setLatitude(point.latitude);
                lastKnownLocation.setLongitude(point.longitude);
                doTheJob(lastKnownLocation);
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker!=null) {
                    marker.remove();
                }
                return false;
            }
        });
    }

    private void addPlaceDialog(String nameSuggestion) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.name_your_place);

        final EditText nameEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        nameEditText.setLayoutParams(params);
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(20)});
        nameEditText.setText(nameSuggestion);
        nameEditText.setSelection(nameEditText.getText().length());
        dialogBuilder.setView(nameEditText);
        dialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!nameEditText.getText().toString().isEmpty()) {
                            dbHandler.insertToDb(nameEditText.getText().toString(), String.valueOf(lat), String.valueOf(lng), "" , proximity);
                            MapActivity.this.finish();
                        }
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastKnownLocation();
        if(lastKnownLocation!=null && locationIsFresh(lastKnownLocation)) return;
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastKnownLocation = location;
        doTheJob(lastKnownLocation);
    }

    @NonNull
    private Boolean locationIsFresh(Location location){
        long time= System.currentTimeMillis();
        long lastKnownLocationTime = location.getTime();
        return (time - lastKnownLocationTime < 20000);
    }



}
