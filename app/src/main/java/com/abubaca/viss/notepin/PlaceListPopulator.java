package com.abubaca.viss.notepin;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by viss on 3/12/17.
 */

public class PlaceListPopulator {

    private final static String TAG = "LIST_POPULATOR";
    private Activity activity;
    private TextView noPlacesTV;
    private ListView placeLV;
    private DBHandler dbHandler;

    PlaceListPopulator(final Activity activity){
        this.activity = activity;
        dbHandler = new DBHandler(activity);
        placeLV = (ListView)activity.findViewById(R.id.place_lv);
        noPlacesTV = (TextView)activity.findViewById(R.id.no_places_tv);
        noPlacesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noPlacesTV.setClickable(false);
                noPlacesTV.setTextColor(R.color.handsOnGrey);
                new Starter(activity).startMapActivity();
            }
        });
    }

    void populate(){
        List<Placenote> placenotes = dbHandler.getPlaceNotes();
        if(placenotes.size()>0) {
            placeLV.setVisibility(View.VISIBLE);
            noPlacesTV.setVisibility(View.INVISIBLE);
            PlaceListAdapter adapter = new PlaceListAdapter(activity, placenotes);
            placeLV.setAdapter(adapter);
        }else{
            noPlacesTV.setClickable(true);
            noPlacesTV.setVisibility(View.VISIBLE);
            noPlacesTV.setText(R.string.no_places);
            PlaceListAdapter adapter = new PlaceListAdapter(activity , placenotes);
            placeLV.setAdapter(adapter);
            placeLV.setVisibility(View.INVISIBLE);
        }
    }
}
