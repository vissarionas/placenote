package com.abubaca.viss.messeme;

import java.lang.ref.SoftReference;

/**
 * Created by viss on 1/3/17.
 */

public class PlaceNote {

    private String place , note;

    public PlaceNote(String place , String note){
        this.place = place;
        this.note = note;
    }

    public String getPlaceNote(){
        return this.place+" "+this.note;
    }

    public String getPlace(){
        return this.place;
    }
    public String getNote(){
        return this.note;
    }

}
