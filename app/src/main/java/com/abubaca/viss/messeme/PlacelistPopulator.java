package com.abubaca.viss.messeme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by viss on 2/24/17.
 */

public class PlacelistPopulator {

    private Activity activity;

    private TextView noPlacesTV;
    private ListView placeLV;
    private DBHandler dbHandler;
    private View footerView;

    public PlacelistPopulator(final Activity activity){
        this.activity = activity;
        dbHandler = new DBHandler(activity);
        placeLV = (ListView)activity.findViewById(R.id.place_lv);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerView = inflater.inflate(R.layout.list_footer , null);
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapActivity();
            }
        });
        placeLV.addFooterView(footerView);
        noPlacesTV = (TextView)activity.findViewById(R.id.no_places_tv);
        noPlacesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapActivity();
            }
        });
    }

    void populate(){
        List<PlaceNote> list = dbHandler.getPlaceNotes();
        if(list.size()==0) {
            noPlacesTV.setVisibility(View.VISIBLE);
            placeLV.setVisibility(View.INVISIBLE);
            noPlacesTV.setText(R.string.no_places);
        }else{
            placeLV.setVisibility(View.VISIBLE);
            noPlacesTV.setVisibility(View.INVISIBLE);
        }
        CustomAdapter adapter = new CustomAdapter(activity , list);
        placeLV.removeFooterView(footerView);
        placeLV.setAdapter(adapter);
    }

    private void startMapActivity() {
        Intent intent = new Intent(activity, MapActivity.class);
        activity.startActivity(intent);
    }

}
