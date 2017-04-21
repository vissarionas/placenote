package com.abubaca.viss.notepin;

/**
 * Created by viss on 2/21/17.
 */

public class IntervalGenerator {

    long getInterval(float distance){
        long interval;
        if(isBetween(distance,0,200)){
            interval = 5000;
        }else if(isBetween(distance,200,500)){
            interval = 8000;
        }else if(isBetween(distance,500,1000)){
            interval = 15000;
        }else if(isBetween(distance,1000,2500)){
            interval = 25000;
        }else if(isBetween(distance,2500,5000)){
            interval = 50000;
    }   else if(isBetween(distance,5000,10000)) {
            interval = 80000;
        }else{
            interval = 100000;
        }
        return interval;
    }

    private Boolean isBetween(float value , float min , float max){
        return (value>min && value<=max);
    }
}
