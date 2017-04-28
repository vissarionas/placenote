package com.abubaca.viss.notepin;

/**
 * Created by viss on 2/21/17.
 */

public class IntervalGenerator {

    long getInterval(float distance){
        long interval;
        if(isBetween(distance,0,300)){
            interval = 15000;
        }else if(isBetween(distance,300,600)){
            interval = 30000;
        }else if(isBetween(distance,600,1000)){
            interval = 150000;
        }else if(isBetween(distance,1000,2500)){
            interval = 220000;
        }else if(isBetween(distance,2500,5000)){
            interval = 300000;
    }   else if(isBetween(distance,5000,10000)) {
            interval = 400000;
        }else{
            interval = 600000;
        }
        return interval;
    }

    private Boolean isBetween(float value , float min , float max){
        return (value>min && value<=max);
    }
}
