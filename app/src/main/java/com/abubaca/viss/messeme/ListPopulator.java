package com.abubaca.viss.messeme;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by viss on 3/12/17.
 */

public class ListPopulator {

    private final static String TAG = "LIST_POPULATOR";
    private Activity activity;
    private TextView noPlacesTV;
    private ListView placeLV;
    private DBHandler dbHandler;
    private List<PlaceNote> placeNotes;

    public ListPopulator(final Activity activity){
        this.activity = activity;
        dbHandler = new DBHandler(activity);
        placeLV = (ListView)activity.findViewById(R.id.place_gv);
        noPlacesTV = (TextView)activity.findViewById(R.id.no_places_tv);
        noPlacesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Starter(activity).startMapActivity();
            }
        });
    }

    public void populate(){
        placeNotes=dbHandler.getPlaceNotes();
        if(placeNotes.size()>0) {
            placeLV.setVisibility(View.VISIBLE);
            noPlacesTV.setVisibility(View.INVISIBLE);
            CustomAdapter adapter = new CustomAdapter(activity, placeNotes);
            placeLV.setAdapter(adapter);
        }else{
            noPlacesTV.setVisibility(View.VISIBLE);
            noPlacesTV.setText(R.string.no_places);
            CustomAdapter adapter = new CustomAdapter(activity , placeNotes);
            placeLV.setAdapter(adapter);
            placeLV.setVisibility(View.INVISIBLE);
        }
    }




}
