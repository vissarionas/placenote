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

    private static final String TAG = "EDIT_PLACE";
    private DBHandler dbHandler;
    private String placeName;
    private Double lat , lng;
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
        lat = location.getLatitude();
        lng = location.getLongitude();
        String subtitleLat, subtitleLng;
        subtitleLat = String.valueOf(lat);
        subtitleLng = String.valueOf(lng);
        if(subtitleLat.length()>10) subtitleLat = subtitleLat.substring(0,10);
        if(subtitleLng.length()>10) subtitleLng = subtitleLng.substring(0,10);
        getSupportActionBar().setTitle(placeName);
        getSupportActionBar().setSubtitle(subtitleLat+" - "+subtitleLng);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat , lng), 17.0f));
//        map.getUiSettings().setScrollGesturesEnabled(false);
        //remove previously placed Marker
        if (marker != null) marker.remove();

        //place marker where user just clicked
        marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
    }
}
