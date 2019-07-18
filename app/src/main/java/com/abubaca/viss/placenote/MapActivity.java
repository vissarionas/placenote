package com.abubaca.viss.placenote;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

  private static final int FINE_LOCATION_PERMISSION_REQUEST = 0x1;
  private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 0x2;
  private static final int REQUEST_CHECK_SETTINGS = 0x3;
  private GoogleMap map;
  private Marker marker;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private Location userLocation;
  private String placeAddress = null;
  private Double lat, lng;
  private int proximity = 100;
  private Button addPlaceButton;
  private GoogleApiClient googleApiClient;
  private IntentFilter filter = new IntentFilter("GET_ADDRESS");
  private LinearLayout pbLayout;
  private PlacenoteUtils placenoteUtils;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    placenoteUtils = new PlacenoteUtils(this);
    addPlaceButton = findViewById(R.id.add_place_btn);
    pbLayout = findViewById(R.id.pb_layout);
  }

  @Override
  protected void onResume() {
    getGoogleMap();
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
        if (userLocation != null) {
          pbLayout.setVisibility(View.VISIBLE);
          userLocation.setLatitude(point.latitude);
          userLocation.setLongitude(point.longitude);
          showUserLocation(userLocation);
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
        searchIntent();
        break;
      case android.R.id.home:
        onBackPressed();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void getGoogleMap() {
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
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
    getUserLocation();
  }

  private LocationRequest getLocationRequestObject() {
    return new LocationRequest()
            .setInterval(0)
            .setExpirationDuration(60000)
            .setPriority(PRIORITY_BALANCED_POWER_ACCURACY);
  }

  private LocationCallback getLocationCallback() {
     return new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
          return;
        }
        for (Location location : locationResult.getLocations()) {
          Log.e("LOCATION", "Acc: " + location.getAccuracy());
          showUserLocation(location);
          removeLocationUpdates();
        }
      }
    };
  }
  private void removeLocationUpdates() {
    LocationCallback locationCallback = getLocationCallback();
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
  }

  private void startLocationUpdates() {
    if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      // TODO: Consider calling
      // ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return;
    }
    LocationRequest locationRequest = getLocationRequestObject();
    LocationCallback locationCallback = getLocationCallback();
    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
  }

  private void searchIntent() {
    try {
      Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
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
        placeMarkerOnLocation(placeLocation, zoom);

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
        // The user canceled the operation.
      }
    }
  }

  private void getAddressByLocation(Location location) {
    Double latitude = location.getLatitude();
    Double longitude = location.getLongitude();
    Intent intent = new Intent(MapActivity.this, AddressGenerator.class);
    intent.putExtra("lat", latitude);
    intent.putExtra("lng", longitude);
    startService(intent);
  }

  private void placeMarkerOnLocation(Location location, float zoom) {
    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    if (marker != null) marker.remove();
    marker = map
      .addMarker(new MarkerOptions()
        .position(latLng)
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)
      )
    );
  }

  private void showUserLocation(Location location) {
    getAddressByLocation(location);
    placeMarkerOnLocation(location, 18.0f);
    pbLayout.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onConnectionSuspended(int i) {

  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

  }

//  private boolean isLocationEnabled(Activity activity) {
//    LocationManager service = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
//    return service.isProviderEnabled(LocationManager.GPS_PROVIDER) || service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//  }

  private void getUserLocation() {
    LocationRequest locationRequest = getLocationRequestObject();
    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
    Task<LocationSettingsResponse> checkLocationSettingsTask = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

    checkLocationSettingsTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
      @Override
      public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
        try {
          LocationSettingsResponse response = task.getResult(ApiException.class);
          // All location settings are satisfied. The client can initialize location
          // requests here.
          startLocationUpdates();
        } catch (ApiException exception) {
          switch (exception.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
              // Location settings are not satisfied. But could be fixed by showing the
              // user a dialog.
              try {
                // Cast to a resolvable exception.
                ResolvableApiException resolvable = (ResolvableApiException) exception;
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                resolvable.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
              } catch (IntentSender.SendIntentException e) {
                // Ignore the error.
              } catch (ClassCastException e) {
                // Ignore, should be an impossible error.
              }
              break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
              // Location settings are not satisfied. However, we have no way to fix the
              // settings so we won't show the dialog.
              break;
          }
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
