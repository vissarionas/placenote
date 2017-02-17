package com.abubaca.viss.messeme;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by viss on 12/29/16.
 */

public class CustomAdapter extends BaseAdapter {

    private final static String TAG = "PLACENOTE_ADAPTER";


    private LayoutInflater layoutInflater;
    private TextView placeText , noteText;
    private List<PlaceNote> placeNotes;
    private Context context;

    CustomAdapter(Context context, List<PlaceNote> placeNotes){
        this.placeNotes = placeNotes;
        this.context = context;
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

    String getPlace(int position){
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
