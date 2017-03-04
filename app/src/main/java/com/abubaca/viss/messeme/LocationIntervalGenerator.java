package com.abubaca.viss.messeme;

/**
 * Created by viss on 2/21/17.
 */

public class LocationIntervalGenerator {

    long getInterval(float distance){
        long interval;
        if(distance<200){
            interval = 5000;
        }else if(distance<500) {
            interval = 10000;
        }else if(distance<1000){
            interval = 20000;
        }else if(distance<5000){
            interval = 60000;
        }else{
            interval = 120000;
        }
    return interval;
    }
}
