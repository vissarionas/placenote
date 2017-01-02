package com.abubaca.viss.messeme;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by viss on 12/29/16.
 */

public class PlaceNoteAdapter extends BaseAdapter {

    private static Context context;
    private static String[] place , note;
    private static LayoutInflater layoutInflater;
    private static TextView placeText , noteText;

    public PlaceNoteAdapter(Context context , String[] place , String[] note){
        this.context = context;
        this.place = place;
        this.note =note;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return place.length;
    }

    @Override
    public Object getItem(int position) {
        return note[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.place_note_item , parent , false);
            placeText = (TextView)convertView.findViewById(R.id.placeText);
            noteText = (TextView)convertView.findViewById(R.id.noteText);
            placeText.setText(place[position]);
            noteText.setText(note[position]);
        }
        return convertView;
    }
}
