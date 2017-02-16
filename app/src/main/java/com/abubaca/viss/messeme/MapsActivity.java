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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap map;
    private LatLng latlng;
    private Marker marker;
    public String placeAddress;
    private Double lat , lng;
    private int proximity;
    private Button addPlaceButton;
    private DBHandler dbHandler;

    private GoogleApiClient googleApiClient;
    private Location lastKnownLocation;
    private static final int FINE_LOCATION_REQUEST = 0x1;

    protected static final String TAG = "MAP_ACTIVITY";

    private IntentFilter filter = new IntentFilter("GET_ADDRESS");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setSubtitle(R.string.maps_subtitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbHandler = new DBHandler(getApplicationContext());
        addPlaceButton = (Button)findViewById(R.id.add_place_button);
        connectGoogleApiClient();
    }

    @Override
    protected void onResume() {
        this.registerReceiver(broadcastReceiver , filter);
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                removeLocationUpdates();
                if(place.getViewport()!=null) {
                    Location northeastBound = new Location("");
                    northeastBound.setLatitude(place.getViewport().northeast.latitude);
                    northeastBound.setLongitude(place.getViewport().northeast.longitude);
                    Location placeLocation = new Location("");
                    placeLocation.setLatitude(place.getLatLng().latitude);
                    placeLocation.setLongitude(place.getLatLng().longitude);
                    proximity = Math.round(northeastBound.distanceTo(placeLocation));
                }

                lat = place.getLatLng().latitude;
                lng = place.getLatLng().longitude;
                Log.i(TAG , place.getName()+" lat: "+lat+" lng: "+lng);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 19.0f));
                if (marker != null) {
                    marker.remove();
                }
                marker = map.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                addPlaceButton.setVisibility(View.VISIBLE);
                addPlaceButton.setText("Add "+ place.getName()+" to your placelist");
                addPlaceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addPlaceDialog(lat, lng , proximity);
                    }
                });
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        connectGoogleApiClient();
        super.onResume();
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            placeAddress = intent.getStringExtra("address");
            addPlaceButton.setVisibility(View.VISIBLE);
            addPlaceButton.setText("Add "+ placeAddress+" to your placelist");
        }
    };

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

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient , locationRequest , MapsActivity.this);
    }

    private void removeLocationUpdates(){
        Log.i(TAG , "Removed location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient , this);
    }

    private void requestAddress(Double lat , Double lng){
        Intent intent = new Intent(MapsActivity.this, AddressGenerator.class);
        intent.putExtra("lat" , lat);
        intent.putExtra("lng" , lng);
        startService(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.0f));
        marker = map.addMarker(new MarkerOptions().position(latlng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        lat = latlng.latitude;
        lng = latlng.longitude;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                lat = point.latitude;
                lng = point.longitude;
                requestAddress(lat , lng);

                //remove previously placed Marker
                if (marker != null) {
                    marker.remove();
                }

                //place marker where user just clicked
                marker = map.addMarker(new MarkerOptions().position(point)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
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

        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaceDialog(lat, lng, proximity);
            }
        });
    }

    private void addPlaceDialog(final Double lat , final Double lng , final int proximity) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Name your place");

        final EditText nameEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        nameEditText.setLayoutParams(params);
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(20)});
        dialogBuilder.setView(nameEditText);
        dialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!nameEditText.getText().toString().isEmpty()) {
                            dbHandler.insertToDb(nameEditText.getText().toString(), String.valueOf(lat), String.valueOf(lng), "" , proximity);
                            MapsActivity.this.finish();
                        }
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().getAttributes().verticalMargin = -0.2F;
        dialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastKnownLocation();
        if(!locationRecent(lastKnownLocation)){
            requestLocationUpdates();
        }else{
            requestAddress(lastKnownLocation.getLatitude() , lastKnownLocation.getLongitude());
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
        lastKnownLocation = location;
        latlng = new LatLng(location.getLatitude() , location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18.0f));
        marker.setPosition(new LatLng(location.getLatitude() , location.getLongitude()));
        requestAddress(location.getLatitude() , location.getLongitude());
    }

    @NonNull
    private Boolean locationRecent(Location location){
        long time= System.currentTimeMillis();
        long lastKnownLocationTime = location.getTime();
        return (time - lastKnownLocationTime < 20000);
    }


    protected void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST);
            return;
        }
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        latlng = new LatLng(lastKnownLocation.getLatitude() , lastKnownLocation.getLongitude());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
}
