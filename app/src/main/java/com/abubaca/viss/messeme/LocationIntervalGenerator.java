package com.abubaca.viss.messeme;

/**
 * Created by viss on 2/21/17.
 */

public class LocationIntervalGenerator {

    public long getInterval(float distance){
        long interval;
        if(distance<200){
            interval = 4000;
        }else if(distance<500) {
            interval = 6000;
        }else if(distance<1000){
            interval = 10000;
        }else if(distance<5000){
            interval = 50000;
        }else{
            interval = 100000;
        }
    return interval;
    }
}
