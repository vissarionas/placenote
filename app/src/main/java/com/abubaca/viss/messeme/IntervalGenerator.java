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

    public long getInterval(Location currentLocation , List<Location> locations){
        if(locations.size()>0){
            smallestDistance = currentLocation.distanceTo(locations.get(0));
            for(int i = 1; i < locations.size() ; i++){
                if(currentLocation.distanceTo(locations.get(i))<smallestDistance){
                    smallestDistance = currentLocation.distanceTo(locations.get(i));
                }
            }
            interval = Math.round((smallestDistance * 200)/10000)*10000;
        }
        return interval;
    }
}
