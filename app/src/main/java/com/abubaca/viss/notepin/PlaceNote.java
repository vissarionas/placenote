package com.abubaca.viss.notepin;

/**
 * Created by viss on 1/3/17.
 */

public class PlaceNote {

    private String place , note;
    private int state;

    public PlaceNote(String place , String note , int state){
        this.place = place;
        this.note = note;
        this.state = state;
    }

    public String getPlace(){
        return this.place;
    }

    public String getNote(){
        return this.note;
    }

    public int getState(){
        return this.state;
    }

}
