package com.abubaca.viss.messeme;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by viss on 2/24/17.
 */

public class PlacelistPopulator {

    private final static String TAG = "PLACELIST_POPULATOR";

    private Activity activity;
    private TextView noPlacesTV;
    private GridView placeGV;
    private DBHandler dbHandler;
    List<PlaceNote> placeList;

    public PlacelistPopulator(final Activity activity){
        this.activity = activity;
        dbHandler = new DBHandler(activity);
        placeGV = (GridView)activity.findViewById(R.id.place_gv);
        noPlacesTV = (TextView)activity.findViewById(R.id.no_places_tv);
        noPlacesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Starter(activity).startMapActivity();
            }
        });

    }

    void populateListview(){
        placeList = dbHandler.getPlaceNotes();
        if(placeList.size()==0) {
            noPlacesTV.setVisibility(View.VISIBLE);
            placeGV.setVisibility(View.INVISIBLE);
            noPlacesTV.setText(R.string.no_places);
            return;
        }
        placeGV.setVisibility(View.VISIBLE);
        noPlacesTV.setVisibility(View.INVISIBLE);
        CustomAdapter adapter = new CustomAdapter(activity , placeList);
        placeGV.setAdapter(adapter);
    }

//    private void addListviewFooter(ListView listView){
//        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View footerView = inflater.inflate(R.layout.list_footer , null);
//        footerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new Starter(activity).startMapActivity();
//            }
//        });
//        listView.addFooterView(footerView);
//    }
//
//    private void removeListviewFooters(ListView listView){
//        int footerCount = listView.getFooterViewsCount();
//        int listItemsCount = listView.getCount();
//        if(footerCount>1) {
//            View footerView = listView.getAdapter().getView(listItemsCount - footerCount, null, listView);
//            listView.removeFooterView(footerView);
//        }
//    }
}
