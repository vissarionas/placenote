package com.abubaca.viss.messeme;

import android.location.Location;
import android.util.Log;

import java.util.List;

/**
 * Created by viss on 31/12/2016.
 */

public class IntervalGenerator {

    private static long interval;
    private static float smallestDistance;

    private final static String TAG = "INTERVAL_GENERATOR";

    public IntervalGenerator() {
        Log.i(TAG , "IntervalGenerator object created");
    }

    public long getInterval(Location currentLocation , List<Location> locations){
        if(locations.size()>0){
            smallestDistance = currentLocation.distanceTo(locations.get(0));
            Log.e(TAG , "First place distance = "+String.valueOf(smallestDistance));
            for(int i = 0; i < locations.size() ; i++){
                Log.e(TAG , i+" "+locations.get(i).toString());
                if(currentLocation.distanceTo(locations.get(i))<smallestDistance){
                    smallestDistance = currentLocation.distanceTo(locations.get(i));
                    Log.e(TAG , "Distance = "+String.valueOf(smallestDistance));
                }
            }
            interval = (int)(smallestDistance*40);
        }

        Log.i(TAG , "interval set to "+String.valueOf(interval)+" - Closest place was "+String.valueOf(smallestDistance)+" far");
        return interval;
    }
}
