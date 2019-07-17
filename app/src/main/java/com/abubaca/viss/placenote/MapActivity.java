package com.abubaca.viss.placenote;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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

import java.util.List;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private static final String TAG = "MAP_ACTIVITY";
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 0x1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0x2;
    private static final int REQUEST_CHECK_SETTINGS = 0x3;
    private GoogleMap map;
    private Marker marker;
    private String placeAddress = null;
    private Double lat, lng;
    private int proximity = 100;
    private Button addPlaceButton;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private IntentFilter filter = new IntentFilter("GET_ADDRESS");
    private LinearLayout pbLayout;
    private PlacenoteUtils placenoteUtils;
    private LocationRequest locationRequest;
    private Boolean locationEnableCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        placenoteUtils = new PlacenoteUtils(this);
        addPlaceButton = (Button) findViewById(R.id.add_place_btn);
        pbLayout = (LinearLayout) findViewById(R.id.pb_layout);
    }

    @Override
    protected void onResume() {
        //Load google map fragment
        getGoogleMap();
        //start google api client
        startGoogleApiClient();
        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            placenoteUtils.addNewPlace(placeAddress, lat, lng, proximity);
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            }
        });
        registerReceiver(broadcastReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
            if (lastLocation != null) {
                pbLayout.setVisibility(View.VISIBLE);
                lastLocation.setLatitude(point.latitude);
                lastLocation.setLongitude(point.longitude);
                presentUserLocation(lastLocation);
            }
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
            if (marker != null) {
                marker.remove();
            }
            return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                if (isLocationEnabled()) searchIntent();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getGoogleMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void startGoogleApiClient() {
        if (googleApiClient != null && googleApiClient.isConnected()) return;
        googleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationUpdateObject();
        if (!locationEnableCanceled) checkLocationService(locationRequest);
    }

    private void createLocationUpdateObject() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(0);
        locationRequest.setExpirationDuration(60000);
        locationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void removeLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    protected void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getAccuracy() > 1000) return;
        if (location.getAccuracy() > 300)
            Toast.makeText(getApplicationContext(), R.string.bad_accuracy, Toast.LENGTH_LONG).show();
        if (location.getAccuracy() < 100) removeLocationUpdates();
        lastLocation = location;
        presentUserLocation(lastLocation);
    }

    private void searchIntent() {
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
                pbLayout.setVisibility(View.INVISIBLE);
                removeLocationUpdates();
                Place place = PlaceAutocomplete.getPlace(this, data);
                Location placeLocation = new Location("");
                placeLocation.setLatitude(place.getLatLng().latitude);
                placeLocation.setLongitude(place.getLatLng().longitude);
                if (place.getViewport() != null) {
                    Location northeastBound = new Location("");
                    northeastBound.setLatitude(place.getViewport().northeast.latitude);
                    northeastBound.setLongitude(place.getViewport().northeast.longitude);
                    int placeRadius = Math.round(northeastBound.distanceTo(placeLocation));
                    List<Integer> placeTypes = place.getPlaceTypes();
                    Log.i(TAG, placeTypes.toString());
                    proximity = placeTypes.contains(1021) ? 100 :
                            (placeTypes.contains(1011) ? placeRadius * 3 :
                                    placeTypes.contains(1009) ? placeRadius / 2 :
                                            placeRadius);
                }

                lat = placeLocation.getLatitude();
                lng = placeLocation.getLongitude();
                float zoom = (proximity < 200) ? 18.0f :
                        (proximity < 500) ? 17.0f :
                                (proximity < 1000) ? 16.0f :
                                        (proximity < 5000) ? 15.0f :
                                                (proximity < 10000) ? 14.0f : 13.0f;
                pointLocation(placeLocation, zoom);

                placeAddress = place.getName().toString();
                getSupportActionBar().setTitle(placeAddress);
                String addPlace = getResources().getString(R.string.add_place);
                addPlaceButton.setText(String.format(addPlace, placeAddress));

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                // TODO: Handle the error.
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                this.recreate();
            } else if (resultCode == RESULT_CANCELED) {
                locationEnableCanceled = true;
            }
        }
    }

    private void getAddress(Double lat, Double lng) {
        Intent intent = new Intent(MapActivity.this, AddressGenerator.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        startService(intent);
    }

    private void pointLocation(Location location, float zoom) {
        LatLng tempLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(tempLatLng, zoom));
        if (marker != null) marker.remove();
        marker = map.addMarker(new MarkerOptions().position(tempLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
    }

    private void presentUserLocation(Location location) {
        this.lat = location.getLatitude();
        this.lng = location.getLongitude();
        getAddress(location.getLatitude(), location.getLongitude());
        pointLocation(location, 18.0f);
        pbLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @NonNull
    private Boolean locationIsFresh(Location location) {
        long time = System.currentTimeMillis();
        long lastKnownLocationTime = location.getTime();
        return (time - lastKnownLocationTime < 120000);
    }

    private boolean isLocationEnabled() {
        LocationManager service = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER) || service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void checkLocationService(final LocationRequest locationRequest) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        getLastLocation();
                        if (lastLocation != null && locationIsFresh(lastLocation)) {
                            presentUserLocation(lastLocation);
                            break;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, MapActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MapActivity.this , REQUEST_CHECK_SETTINGS);
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("GET_ADDRESS")){
                placeAddress = intent.getStringExtra("ADDRESS");
                pbLayout.setVisibility(View.INVISIBLE);
                String addPlace = getResources().getString(R.string.add_place);
                getSupportActionBar().setTitle(placeAddress);
                addPlaceButton.setText(String.format(addPlace , placeAddress));
            }
        }
    };
}
