package com.abubaca.viss.messeme;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class EditPlace extends FragmentActivity implements OnMapReadyCallback {

    private DBHandler dbHandler;
    private Cursor cursor;
    private String placeName;
    private TextView placeView , coordinatesView;
    private Double lat , lgn;
    private GoogleMap mMap;
    private Marker marker;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);

        dbHandler = new DBHandler(getApplicationContext());
        cursor = dbHandler.getFullCursor();
        placeName = getIntent().getStringExtra("placeName");
        placeView = (TextView)findViewById(R.id.place_view);
        coordinatesView = (TextView)findViewById(R.id.coordinates_view);
        deleteButton = (Button)findViewById(R.id.delete_button);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        placeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPlaceDialog(placeName);
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.deletePlace(placeName);
                EditPlace.this.finish();
            }
        });
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            do{
                if(cursor.getString(0).contentEquals(placeName)){
                    placeView.setText(cursor.getString(0));
                    coordinatesView.setText(cursor.getString(1)+" - "+cursor.getString(2));
                    lat = cursor.getDouble(1);
                    lgn = cursor.getDouble(2);
                }
            }while(cursor.moveToNext());
        }
        super.onResume();

    }

    private void showEditPlaceDialog(final String place){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Change place name.");

        final EditText nameEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        nameEditText.setLayoutParams(params);
        nameEditText.setText(place);
        alertDialog.setView(nameEditText);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHandler.updatePlaceName(place , nameEditText.getText().toString());
                        placeView.setText(nameEditText.getText().toString());
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat , lgn), 17.0f));
        //remove previously placed Marker
        if (marker != null) {
            marker.remove();
        }

        //place marker where user just clicked
        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lgn))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }
}
