package com.abubaca.viss.placenote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viss on 12/29/16.
 */

public class PlaceListAdapter extends BaseAdapter {

    private final static String TAG = "PLACENOTE_ADAPTER";


    private LayoutInflater layoutInflater;
    private ImageView stateIV;
    private List<Placenote> placenotes;
    private Context context;
    private Activity activity;
    private PlacenoteUtils placenoteUtils;
    private List<String> selectedPlaces;
    private LinearLayout listItemSurface;
    private Boolean multipleSelected = false;

    PlaceListAdapter(Activity activity , List<Placenote> placenotes){
        this.placenotes = placenotes;
        this.context = activity;
        this.activity = activity;
        selectedPlaces = new ArrayList<>();
        placenoteUtils = new PlacenoteUtils(activity);
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return placenotes.size();
    }

    @Override
    public Object getItem(int position) {
        return placenotes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(placenotes.size()>0){
            convertView = layoutInflater.inflate(R.layout.place_note_item , parent , false);
            TextView placeText = (TextView)convertView.findViewById(R.id.placeText);
            TextView noteText = (TextView)convertView.findViewById(R.id.noteText);
            stateIV = (ImageView)convertView.findViewById(R.id.stateIV);

            final ImageButton listItemMenuButton = (ImageButton)convertView.findViewById(R.id.list_item_menu);
            listItemSurface = (LinearLayout)convertView.findViewById(R.id.list_item);
            setStateColor(placenotes.get(position).getState());
            String place = placenotes.get(position).getName();
            String subPlace = place.length()>20 ? place.substring(0,18)+".." : place;
            placeText.setText(subPlace);
            placeText.setTypeface(Typeface.SANS_SERIF);
            placeText.setAllCaps(true);
            String note = placenotes.get(position).getNote();
            String subNote = note.length()>20 ? note.substring(0,18)+"..":note;
            noteText.setText(subNote);
            listItemMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMenu(v , position);
                }
            });
            listItemSurface.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!multipleSelected) placenoteUtils.viewNote(getPlace(position));
                }
            });
            listItemSurface.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String place = getPlace(position);
                        if(!selectedPlaces.contains(place)){
                            selectedPlaces.add(place);
                            v.setBackgroundResource(R.drawable.background_selected);
                        }else{
                            selectedPlaces.remove(place);
                            v.setBackgroundResource(R.drawable.background);
                        }

                    Log.i(TAG , "selected places: "+selectedPlaces);
                    multipleSelected = selectedPlaces.size()>0;
                    return true;
                }
            });
        }
        return convertView;
    }

    private void showMenu(View view , final int position){
        PopupMenu popupMenu = new PopupMenu(context , view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.place_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String place = getPlace(position);
                switch (item.getItemId()){
                    case R.id.edit_name:
                        placenoteUtils.editPlace(place);
                        return true;
                    case R.id.view_on_map:
                        new Starter(activity).startViewPlaceActivity(place);
                        return true;
                    case R.id.clear_note:
                        placenoteUtils.clearNote(place);
                        return true;
                    case R.id.delete_place:
                        placenoteUtils.deletePlace(place);
                        return true;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private String getPlace(int position){
        return placenotes.get(position).getName();
    }

    private void setStateColor(int state){
        switch (state){
            case Constants.NOTE_STATE_EMPTY:
                stateIV.setVisibility(View.INVISIBLE);
                break;
            case Constants.NOTE_STATE_ACTIVE:
                stateIV.setImageResource(R.drawable.note_active);
                break;
            case Constants.NOTE_STATE_INACTIVE:
                stateIV.setImageResource(R.drawable.note_inactive);
                break;
            case Constants.NOTE_STATE_ALERTED:
                stateIV.setImageResource(R.drawable.note_alert);
                break;
        }
    }

}
