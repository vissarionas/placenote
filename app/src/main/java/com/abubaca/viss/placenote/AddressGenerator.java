package com.abubaca.viss.placenote;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by viss on 1/13/17.
 */

public class AddressGenerator extends Service {

    private final static String TAG = "ADDRESS_GENERATOR";
    private Geocoder geocoder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        geocoder = new Geocoder(this, Locale.getDefault());
        Double lat = intent.getDoubleExtra("lat" , 0);
        Double lng = intent.getDoubleExtra("lng" , 0);
        String address = getAddress(lat, lng);
        sendBroadcast(address);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getAddress(Double lat , Double lng){
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocation(lat, lng , 1);
        } catch (IOException e) {
            Toast.makeText(this, R.string.address_generator_error_message , Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        if(addresses.size()>0) {
            android.location.Address address = addresses.get(0);
            if (address != null) {
                String[] addressArray = address.getAddressLine(0).split(",");
                StringBuilder sb = new StringBuilder();
                sb.append(addressArray[0].toString());
                return sb.toString();
            }
        }
     return "this place";
    }

    private void sendBroadcast(String address){
        Intent intent = new Intent();
        intent.setAction("GET_ADDRESS");
        intent.putExtra("ADDRESS" , address);
        sendBroadcast(intent);
        stopSelf();
    }
}
