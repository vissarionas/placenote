package com.abubaca.viss.messeme;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng latlng;
    private Marker marker;
    public String placeAddress;
    private Double lat , lng;
    private float accuracy;
    private float mapZoom;
    private Button addPlaceButton;
    private DBHandler dbHandler;

    protected static final String TAG = "MAP_ACTIVITY";

    private IntentFilter filter = new IntentFilter("GET_ADDRESS");

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG , intent.getStringExtra("address"));
            placeAddress = intent.getStringExtra("address");
            addPlaceButton.setVisibility(View.VISIBLE);
            addPlaceButton.setText("Add "+ placeAddress+" to your placelist");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        dbHandler = new DBHandler(getApplicationContext());

        addPlaceButton = (Button)findViewById(R.id.add_place_button);

        Bundle extras = getIntent().getExtras();
        latlng = new LatLng(extras.getDouble("lat") , extras.getDouble("lng"));
        accuracy = 20;
//        accuracy = extras.getFloat("accuracy");
        mapZoom = accuracy < 100 ? 18.0f : 16.0f;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        this.registerReceiver(broadcastReceiver , filter);
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                lat = place.getLatLng().latitude;
                lng = place.getLatLng().longitude;
//                Location location = new Location("");
//                location.setLatitude(lat);
//                location.setLongitude(lng);
//                Log.e(TAG ,"Accuracy: "+location.getAccuracy());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 19.0f));
                if (marker != null) {
                    marker.remove();
                }
                marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                mMap.addCircle(new CircleOptions()
                        .center(place.getLatLng())
                        .radius(200)
                        .strokeColor(Color.RED)
                        .fillColor(Color.BLUE));
                addPlaceButton.setVisibility(View.VISIBLE);
                addPlaceButton.setText("Add "+ place.getName()+" to your placelist");
                addPlaceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addPlaceDialog(lat, lng);
                    }
                });
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        super.onResume();
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, mapZoom));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                lat = point.latitude;
                lng = point.longitude;
                Intent intent = new Intent(MapsActivity.this, AddressGenerator.class);
                intent.putExtra("lat" , lat);
                intent.putExtra("lng" , lng);
                startService(intent);

                //remove previously placed Marker
                if (marker != null) {
                    marker.remove();
                }

                //place marker where user just clicked
                marker = mMap.addMarker(new MarkerOptions().position(point)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
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
                addPlaceDialog(lat, lng);
            }
        });
    }

    private void addPlaceDialog(final Double lat , final Double lng) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Name?");

        final EditText nameEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        nameEditText.setLayoutParams(params);
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
        dialogBuilder.setView(nameEditText);
        dialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!nameEditText.getText().toString().isEmpty()) {
                            dbHandler.insertToDb(nameEditText.getText().toString(), String.valueOf(lat), String.valueOf(lng), "");
                            MapsActivity.this.finish();
                        }
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }
}
