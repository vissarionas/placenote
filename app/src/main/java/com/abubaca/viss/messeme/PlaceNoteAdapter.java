package com.abubaca.viss.messeme;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
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

    private final static String TAG = "PLACENOTE_ADAPTER";


    private static LayoutInflater layoutInflater;
    private static TextView placeText , noteText;
    private static List<PlaceNote> placeNotes;

    public PlaceNoteAdapter(Context context , List<PlaceNote> placeNotes){
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
            convertView = layoutInflater.inflate(R.layout.place_note_item , parent , false);
            placeText = (TextView)convertView.findViewById(R.id.placeText);
            noteText = (TextView)convertView.findViewById(R.id.noteText);
            setFlagColor(placeNotes.get(position).getState());
            placeText.setText(placeNotes.get(position).getPlace());
            String note = placeNotes.get(position).getNote();
            String subNote = note.length()>20 ? note.substring(0,20):note;
            noteText.setText(subNote);
        return convertView;
    }

    public String getPlace(int position){
        return placeNotes.get(position).getPlace();
    }

    private void setFlagColor(int state){
        switch (state){
            case 0:
                placeText.setTextColor(Color.parseColor("#444444"));
                noteText.setTextColor(Color.parseColor("#444444"));
                break;
            case 1:
                placeText.setTextColor(Color.parseColor("#444444"));
//                placeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
                noteText.setTextColor(Color.parseColor("#444444"));
                break;
            case 2:
                placeText.setTextColor(Color.parseColor("#40a347"));
//                placeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
                noteText.setTextColor(Color.parseColor("#40a347"));
                break;
            case 3:
                placeText.setTextColor(Color.parseColor("#d41243"));
//                placeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
                noteText.setTextColor(Color.parseColor("#d41243"));
                break;
        }
    }

}
