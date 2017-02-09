package com.abubaca.viss.messeme;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ViewPlaceMap extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "EDIT_PLACE";
    private DBHandler dbHandler;
    private Cursor cursor;
    private String placeName;
    private TextView placeView , coordinatesView;
    private Double lat , lgn;
    private GoogleMap map;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place_map);

        dbHandler = new DBHandler(getApplicationContext());
        cursor = dbHandler.getFullCursor();
        placeName = getIntent().getStringExtra("placeName");
        Log.i(TAG , "Placename: "+placeName);
        placeView = (TextView)findViewById(R.id.place_view);
        coordinatesView = (TextView)findViewById(R.id.coordinates_view);

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
                    placeView.setText(cursor.getString(0));
                    coordinatesView.setText("Lat: "+cursor.getString(1)+"\n"+"Lng: "+cursor.getString(2));
                    lat = cursor.getDouble(1);
                    lgn = cursor.getDouble(2);
                }
            }while(cursor.moveToNext());
        }
        super.onResume();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat , lgn), 17.0f));
//        map.getUiSettings().setScrollGesturesEnabled(false);
        //remove previously placed Marker
        if (marker != null) {
            marker.remove();
        }

        //place marker where user just clicked
        marker = map.addMarker(new MarkerOptions().position(new LatLng(lat, lgn))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }
}
