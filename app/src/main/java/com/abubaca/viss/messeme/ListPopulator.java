package com.abubaca.viss.messeme;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by viss on 3/12/17.
 */

public class ListPopulator extends AsyncTask<Void, Void , List<PlaceNote>> {

    private final static String TAG = "LIST_POPULATOR";
    private Activity activity;
    private TextView noPlacesTV;
    private ListView placeLV;
    private DBHandler dbHandler;

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

    @Override
    protected List<PlaceNote> doInBackground(Void... params) {
        return dbHandler.getPlaceNotes();
    }

    @Override
    protected void onPostExecute(List<PlaceNote> placeNotes) {
        super.onPostExecute(placeNotes);
        if(placeNotes.size()==0) {
            noPlacesTV.setVisibility(View.VISIBLE);
            noPlacesTV.setText(R.string.no_places);
            CustomAdapter adapter = new CustomAdapter(activity , placeNotes);
            placeLV.setAdapter(adapter);
            placeLV.setVisibility(View.INVISIBLE);
        }else{
            placeLV.setVisibility(View.VISIBLE);
            noPlacesTV.setVisibility(View.INVISIBLE);
            CustomAdapter adapter = new CustomAdapter(activity, placeNotes);
            placeLV.setAdapter(adapter);
        }
    }
}
