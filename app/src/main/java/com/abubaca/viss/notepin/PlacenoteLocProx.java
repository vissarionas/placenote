package com.abubaca.viss.notepin;

import android.location.Location;

/**
 * Created by viss on 4/25/17.
 */

class PlacenoteLocProx {

    String name;
    Location location;
    Integer proximity;

    PlacenoteLocProx(String name, Location location , Integer proximity) {
        this.name = name;
        this.location = location;
        this.proximity = proximity;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public Integer getProximity() {
        return proximity;
    }
}
