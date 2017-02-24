package com.abubaca.viss.messeme;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

/**
 * Created by viss on 12/29/16.
 */

public class CustomAdapter extends BaseAdapter {

    private final static String TAG = "PLACENOTE_ADAPTER";


    private LayoutInflater layoutInflater;
    private TextView placeText , noteText;
    private ImageButton listItemMenuButton;
    private LinearLayout listItemSurface;
    private List<PlaceNote> placeNotes;
    private Context context;
    private DBHandler dbHandler;
    private PlaceNoteUtils placeNoteUtils;

    CustomAdapter(Activity activity , List<PlaceNote> placeNotes){
        this.placeNotes = placeNotes;
        this.context = activity;
        dbHandler = new DBHandler(context);
        placeNoteUtils = new PlaceNoteUtils(activity);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.place_note_item , parent , false);
        placeText = (TextView)convertView.findViewById(R.id.placeText);
        noteText = (TextView)convertView.findViewById(R.id.noteText);
        listItemMenuButton = (ImageButton)convertView.findViewById(R.id.list_item_menu);
        listItemSurface = (LinearLayout)convertView.findViewById(R.id.list_item_surface);
        setFlagColor(placeNotes.get(position).getState());
        placeText.setText(placeNotes.get(position).getPlace());
        String note = placeNotes.get(position).getNote();
        String subNote = note.length()>20 ? note.substring(0,20):note;
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
            }
        });
        return convertView;
    }

    private void showMenu(View view , final int position){
        PopupMenu popupMenu = new PopupMenu(context , view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.edit_place_menu , popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String place = getPlace(position);
                switch (item.getItemId()){
                    case R.id.edit_name:
                        placeNoteUtils.editPlace(place);
                        return true;
                    case R.id.view_on_map:
                        placeNoteUtils.showPlaceOnMap(place);
                        return true;
                    case R.id.clear_note:
                        placeNoteUtils.clearNote(place);
                        return true;
                    case R.id.delete_place:
                        placeNoteUtils.deletePlace(place);
                        return true;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    String getPlace(int position){
        return placeNotes.get(position).getPlace();
    }

    private void setFlagColor(int state){
        switch (state){
            case NoteState.EMPTY:
                placeText.setTextColor(ContextCompat.getColor(context , R.color.flagEmpty));
                noteText.setTextColor(ContextCompat.getColor(context , R.color.flagEmpty));
                break;
            case NoteState.INACTIVE:
                placeText.setTextColor(ContextCompat.getColor(context , R.color.flagInactive));
//                placeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
                noteText.setTextColor(ContextCompat.getColor(context , R.color.flagInactive));
                break;
            case NoteState.ACTIVE:
                placeText.setTextColor(ContextCompat.getColor(context , R.color.flagActive));
//                placeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
                noteText.setTextColor(ContextCompat.getColor(context , R.color.flagActive));
                break;
            case NoteState.ALERTED:
                placeText.setTextColor(ContextCompat.getColor(context , R.color.flagAlerted));
//                placeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,26);
                noteText.setTextColor(ContextCompat.getColor(context , R.color.flagAlerted));
                break;
        }
    }
}
