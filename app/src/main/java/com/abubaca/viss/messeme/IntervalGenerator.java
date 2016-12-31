package com.abubaca.viss.messeme;

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

/**
 * Created by viss on 31/12/2016.
 */

public class IntervalGenerator {

    private final String TAG = "INTERVAL_GENERATOR";

    public IntervalGenerator() {
        Log.i(TAG , "IntervalGenerator object created");
    }

    public long getInterval(Location currentLocation , Location[] locations){
        long interval = 20000;
        Float distance;
        if(locations.length>0){
            distance = currentLocation.distanceTo(locations[0]);
            for(int i = 1; i < locations.length ; i++){
                if(currentLocation.distanceTo(locations[i])<distance){
                    distance = currentLocation.distanceTo(locations[i]);
                }
            }
            interval = Math.round((long)(distance*30));
        }

        Log.i(TAG , "interval set to "+String.valueOf(interval));
        return interval;
    }
}
