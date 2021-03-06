package com.abubaca.viss.placenote;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by viss on 1/13/17.
 */

public class AddressGenerator extends Service {

    private Geocoder geocoder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        geocoder = new Geocoder(this, Locale.getDefault());
        Double latitude = intent.getDoubleExtra("lat" , 0);
        Double longitude = intent.getDoubleExtra("lng" , 0);
        String address = getAddress(latitude, longitude);
        sendBroadcast(address);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getAddress(Double latitude , Double longitude){
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocation(latitude, longitude , 1);
        } catch (IOException e) {
            Toast.makeText(this, R.string.address_generator_error_message , Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        if(addresses.size()>0) {
            android.location.Address address = addresses.get(0);
            if (address != null) {
                String[] addressArray = address.getAddressLine(0).split(",");
                StringBuilder sb = new StringBuilder();
                sb.append(addressArray[0]);
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
