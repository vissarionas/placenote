package com.abubaca.viss.messeme;

import android.location.Location;

import java.util.List;

/**
 * Created by viss on 2/21/17.
 */

public class IntervalGenerator2 {

    private static long interval;

    public long getInterval(float distance){
        if(distance<200){
            interval = 2000;
        }else if(distance<1000){
            interval = 6000;
        }else if(distance<5000){
            interval = 200000;
        }else{
            interval = 500000;
        }
    return interval;
    }
}
