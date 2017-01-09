package com.abubaca.viss.messeme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by viss on 12/29/16.
 */

public class PlaceNoteAdapter extends BaseAdapter {

    private static Context context;
    private static LayoutInflater layoutInflater;
    private static TextView placeText , noteText;
    private static List<PlaceNote> placeNotes;

    public PlaceNoteAdapter(Context context , List<PlaceNote> placeNotes){
        this.context = context;
        this.placeNotes = placeNotes;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return placeNotes.size();
    }

    @Override
    public Object getItem(int position) {
        return placeNotes.get(position);
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
            placeText.setText(placeNotes.get(position).getPlace());
            noteText.setText(placeNotes.get(position).getNote());
        }
        return convertView;
    }

    public String getPlace(int position){
        return placeNotes.get(position).getPlace();
    }
}
