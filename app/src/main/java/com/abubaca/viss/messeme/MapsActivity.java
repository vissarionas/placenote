package com.abubaca.viss.messeme;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng latlng;
    private Geocoder geocoder;
    private Marker marker;
    public String placeAddress;
    private Double lat , lgn;
    private float accuracy;
    private float mapZoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Bundle extras = getIntent().getExtras();

        latlng = new LatLng(extras.getDouble("lat") , extras.getDouble("lgn"));
        accuracy = extras.getFloat("accuracy");
        mapZoom = accuracy < 100 ? 18.0f : 15.0f;
        geocoder = new Geocoder(this, Locale.getDefault());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng , mapZoom));
        mMap.addMarker(new MarkerOptions()
                .position(latlng)
                .title("You are here"));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                latlng = point;
                lat = latlng.latitude;
                lgn = latlng.longitude;
                List<Address> addresses = new ArrayList<>();
                try {
                    addresses = geocoder.getFromLocation(point.latitude, point.longitude,1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                android.location.Address address = addresses.get(0);

                if (address != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++){
                        sb.append(address.getAddressLine(i) + "\n");
                    }

//                    Toast.makeText(MapsActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
                    placeAddress = sb.toString();
                    startEditPlaceActivity(lat , lgn , placeAddress);
                }

                //remove previously placed Marker
                if (marker != null) {
                    marker.remove();
                }

                //place marker where user just clicked
                marker = mMap.addMarker(new MarkerOptions().position(point).title("Marker")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

            }
        });
    }

    private void startEditPlaceActivity(Double lat, Double lgn , String address){
        Intent intent = new Intent(MapsActivity.this, EditPlace.class);
        intent.putExtra("lat" , lat);
        intent.putExtra("lgn" , lgn);
        intent.putExtra("address" , address);
        startActivity(intent);
        this.finish();
    }
}
