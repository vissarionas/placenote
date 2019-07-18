package com.abubaca.viss.placenote;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by viss on 3/12/17.
 */

public class PlaceListPopulator {

    private Activity activity;
    private TextView noPlacesTV;
    private ListView placeLV;
    private DBHandler dbHandler;

    PlaceListPopulator(final Activity activity){
        this.activity = activity;
        dbHandler = new DBHandler(activity);
        placeLV = activity.findViewById(R.id.place_lv);
        noPlacesTV = activity.findViewById(R.id.no_places_tv);
    }

    void populate(){
        List<Placenote> placenotes = dbHandler.getPlaceNotes();
        if(placenotes.size()>0) {
            placeLV.setVisibility(View.VISIBLE);
            noPlacesTV.setVisibility(View.INVISIBLE);
            PlaceListAdapter adapter = new PlaceListAdapter(activity, placenotes);
            placeLV.setAdapter(adapter);
        }else{
            noPlacesTV.setVisibility(View.VISIBLE);
            noPlacesTV.setText(R.string.no_places);
            PlaceListAdapter adapter = new PlaceListAdapter(activity , placenotes);
            placeLV.setAdapter(adapter);
            placeLV.setVisibility(View.INVISIBLE);
        }
    }
}
