package com.abubaca.viss.placenote;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ViewPlaceActivity extends AppCompatActivity implements OnMapReadyCallback {

  private DBHandler dbHandler;
  private String placeName;
  private Double latitude, longitude;
  private Marker marker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_place_map);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    dbHandler = new DBHandler(getApplicationContext());
    placeName = getIntent().getStringExtra("PLACENAME");

    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(item.getItemId()==android.R.id.home) onBackPressed();
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Location location = dbHandler.getPlaceLocation(placeName);
    latitude = location.getLatitude();
    longitude = location.getLongitude();
    String subtitleLatitude, subtitleLongitude;
    subtitleLatitude = String.valueOf(latitude);
    subtitleLongitude = String.valueOf(longitude);
    if(subtitleLatitude.length()>10) subtitleLatitude = subtitleLatitude.substring(0,10);
    if(subtitleLongitude.length()>10) subtitleLongitude = subtitleLongitude.substring(0,10);
    getSupportActionBar().setTitle(placeName);
    getSupportActionBar().setSubtitle(subtitleLatitude+" - "+subtitleLongitude);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));
    if (marker != null) marker.remove();
    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
  }
}
