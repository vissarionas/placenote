package com.abubaca.viss.messeme;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
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

    private static final String TAG = "EDIT_PLACE";
    private DBHandler dbHandler;
    private Cursor cursor;
    private String placeName;
    private TextView placeView , coordinatesView;
    private Double lat , lgn;
    private GoogleMap map;
    private Marker marker;
    private ImageView deleteButton , editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);

        dbHandler = new DBHandler(getApplicationContext());
        cursor = dbHandler.getFullCursor();
        placeName = getIntent().getStringExtra("placeName");
        Log.i(TAG , "Placename: "+placeName);
        placeView = (TextView)findViewById(R.id.place_view);
        coordinatesView = (TextView)findViewById(R.id.coordinates_view);
        deleteButton = (ImageView)findViewById(R.id.delete_button);
        editButton = (ImageView)findViewById(R.id.edit_button);
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
                confirmDropPlace(placeName);
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPlaceDialog(placeName);
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
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Change place name.");

        final EditText nameEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        nameEditText.setLayoutParams(params);
        nameEditText.setText(place);
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
        dialogBuilder.setView(nameEditText);
        dialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHandler.updatePlaceName(place , nameEditText.getText().toString());
                        placeView.setText(nameEditText.getText().toString());
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().getAttributes().verticalMargin = -0.2F;
        dialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat , lgn), 17.0f));
        map.getUiSettings().setScrollGesturesEnabled(false);
        //remove previously placed Marker
        if (marker != null) {
            marker.remove();
        }

        //place marker where user just clicked
        marker = map.addMarker(new MarkerOptions().position(new LatLng(lat, lgn))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }

    private void confirmDropPlace(final String placeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to remove "+placeName+"")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.deletePlace(placeName);
                        EditPlace.this.finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
