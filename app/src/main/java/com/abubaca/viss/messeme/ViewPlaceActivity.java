package com.abubaca.viss.messeme;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
    private Cursor cursor;
    private String placeName;
    private Double lat , lng;
    private GoogleMap map;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbHandler = new DBHandler(getApplicationContext());
        cursor = dbHandler.getFullCursor();
        placeName = getIntent().getStringExtra("placeName");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            do{
                if(cursor.getString(0).contentEquals(placeName)){
                    lat = cursor.getDouble(1);
                    lng = cursor.getDouble(2);
                    String subtitleLat, subtitleLng;
                    subtitleLat = String.valueOf(lat);
                    subtitleLng = String.valueOf(lng);
                    if(subtitleLat.length()>10) subtitleLat = subtitleLat.substring(0,10);
                    if(subtitleLng.length()>10) subtitleLng = subtitleLng.substring(0,10);
                    getSupportActionBar().setTitle(placeName);
                    getSupportActionBar().setSubtitle(subtitleLat+" - "+subtitleLng);
                    break;
                }
            }while(cursor.moveToNext());
        }
        super.onResume();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat , lng), 17.0f));
//        map.getUiSettings().setScrollGesturesEnabled(false);
        //remove previously placed Marker
        if (marker != null) {
            marker.remove();
        }

        //place marker where user just clicked
        marker = map.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }
}
