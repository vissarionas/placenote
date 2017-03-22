package com.abubaca.viss.messeme;

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
            Toast.makeText(this, "Address unavailable.\nSlow Internet connection might be the issue" , Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        if(addresses.size()>0) {
            android.location.Address address = addresses.get(0);
            if (address != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex()-1; i++) {
                    sb.append(address.getAddressLine(i));
                }
                return sb.toString();
            }
        }
     return null;
    }

    private void sendBroadcast(String address){
        Intent intent = new Intent();
        intent.setAction("GET_ADDRESS");
        intent.putExtra("ADDRESS" , address);
        sendBroadcast(intent);
        stopSelf();
    }
}
