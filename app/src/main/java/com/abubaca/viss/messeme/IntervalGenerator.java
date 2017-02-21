package com.abubaca.viss.messeme;

import android.location.Location;

import java.util.List;

/**
 * Created by viss on 31/12/2016.
 */

public class IntervalGenerator {

    private static long interval;

    public long getInterval(Location currentLocation , List<Location> locations){
        if(locations.size()>0){
            float smallestDistance = currentLocation.distanceTo(locations.get(0));
            for(Location location : locations){
                if(currentLocation.distanceTo(location)<smallestDistance){
                    smallestDistance = currentLocation.distanceTo(location);
                }
            }
            if(smallestDistance<200){
                interval = 2000;
            }else if(smallestDistance<1000){
                interval = 8000;
            }else if(smallestDistance<5000){
                interval = 300000;
            }else{
                interval = 600000;
            }
        }
        return interval;
    }
}
