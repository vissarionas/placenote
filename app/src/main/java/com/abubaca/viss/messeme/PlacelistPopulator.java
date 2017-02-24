package com.abubaca.viss.messeme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by viss on 2/24/17.
 */

public class PlacelistPopulator {

    private Activity activity;

    private TextView noPlacesTV;
    private ListView placeLV;
    private CustomAdapter adapter;
    private PlaceNoteUtils placeNoteUtils;

    public PlacelistPopulator(final Activity activity){
        this.activity = activity;
        placeLV = (ListView)activity.findViewById(R.id.place_lv);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View footerView = inflater.inflate(R.layout.list_footer , null);
        placeLV.addFooterView(footerView);

        noPlacesTV = (TextView)activity.findViewById(R.id.no_places_tv);
        noPlacesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapActivity();
            }
        });
        placeNoteUtils = new PlaceNoteUtils(activity);
    }

    void populate(List<PlaceNote> list){
        if(list.size()==0) {
            noPlacesTV.setVisibility(View.VISIBLE);
            placeLV.setVisibility(View.INVISIBLE);
            noPlacesTV.setText(R.string.no_places);
        }else{
            placeLV.setVisibility(View.VISIBLE);
            noPlacesTV.setVisibility(View.INVISIBLE);
        }
        adapter = new CustomAdapter(activity , list);
        placeLV.setAdapter(adapter);
        placeLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    placeNoteUtils.viewNote(adapter.getPlace(position));
                } catch (IndexOutOfBoundsException e) {
                    startMapActivity();
                }
            }
        });
    }

    private void startMapActivity() {
        Intent intent = new Intent(activity, MapActivity.class);
        activity.startActivity(intent);
    }

}
