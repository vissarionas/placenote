package com.abubaca.viss.messeme;

/**
 * Created by viss on 2/21/17.
 */

public class LocationIntervalGenerator {

    long getInterval(float distance){
        long interval;
        if(distance<200){
            interval = 8000;
        }else if(distance<500) {
            interval = 20000;
        }else if(distance<1000){
            interval = 60000;
        }else if(distance<5000){
            interval = 120000;
        }else if(distance<10000){
            interval = 400000;
        }else{
            interval = 600000;
        }
    return interval;
    }
}
