package com.abubaca.viss.placenote;

import android.location.Location;

/**
 * Created by viss on 1/3/17.
 */

class Placenote {

    private String place , note;
    private int state , proximity;
    private Location location;

    Placenote(String place , String note , int state){
        this.place = place;
        this.note = note;
        this.state = state;
    }

    Placenote(String place , Location location , int proximity){
        this.place = place;
        this.location = location;
        this.proximity = proximity;
    }

    public String getName(){
        return this.place;
    }

    public String getNote(){
        return this.note;
    }

    public Location getLocation(){
        return this.location;
    }

    public int getProximity(){
        return this.proximity;
    }

    int getState(){
        return this.state;
    }

}
