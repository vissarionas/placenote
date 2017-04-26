package com.abubaca.viss.notepin;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by viss on 2/24/17.
 */

public class PlacenoteUtils {

    private final static String TAG = "PLACENOTE_UTILS";
    private Activity activity;
    private DBHandler dbHandler;

    PlacenoteUtils(Activity activity){
        this.activity = activity;
        dbHandler = new DBHandler(activity);
    }

    void addNewPlace(String nameSuggestion , final Double lat , final Double lng  , final int proximity){
        LayoutInflater inflater = activity.getLayoutInflater();
        View addPlaceView = inflater.inflate(R.layout.add_place , null);
        addPlaceView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        final EditText addPlaceET = (EditText)addPlaceView.findViewById(R.id.placeNameET);

        addPlaceET.setText(nameSuggestion);
        addPlaceET.setSelection(addPlaceET.getText().length());

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setMessage(R.string.type_place_name);
        dialogBuilder.setView(addPlaceView);
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!addPlaceET.getText().toString().isEmpty()){
                    String place = addPlaceET.getText().toString();
                    if(!dbHandler.placeExists(place)){
                        dbHandler.insertToDb(place, String.valueOf(lat), String.valueOf(lng), "" , proximity);
                        activity.finish();
                    }else{
                        new CustomToast().makeToast(activity , Constants.WARNING_TOAST , activity.getString(R.string.place_exists));
                    }
                }
            }
        });

        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    void viewNote(final String place){
        String note = dbHandler.getPlaceNote(place);
        if(note.contentEquals("")){
            editNote(place);
            return;
        }
        if(dbHandler.isNotified(place))dbHandler.updatePlaceNote(place , null , Constants.NOTE_STATE_INACTIVE, null , null);
        LayoutInflater inflater = activity.getLayoutInflater();
        View editView = inflater.inflate(R.layout.edit_note , null);
        editView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        ImageButton btnDelete;
        final TextView noteTextView;
        noteTextView = (TextView)editView.findViewById(R.id.note_tv);
        btnDelete = (ImageButton)editView.findViewById(R.id.btn_delete);
        if(note.isEmpty()){
            btnDelete.setVisibility(View.INVISIBLE);
        }else{
            noteTextView.setText(note);
        }

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setView(editView);
        dialogBuilder.setTitle(place);
        final Dialog dialog = dialogBuilder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                new PlaceListPopulator(activity).populate();
            }
        });
        dialog.show();
        noteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNote(place);
                dialog.dismiss();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearNote(place);
                dialog.dismiss();
            }
        });
    }

    private void editNote(final String place){
        String prevNote = dbHandler.getPlaceNote(place);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        final EditText noteEditText = new EditText(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        noteEditText.setLayoutParams(params);
        if(!prevNote.isEmpty()){
            noteEditText.setText(prevNote);
        }else{
            noteEditText.setHint(R.string.type_a_note_here);
        }
        noteEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        noteEditText.setSelection(noteEditText.getText().length());

        dialogBuilder.setView(noteEditText);
        dialogBuilder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String note = noteEditText.getText().toString();
                        if(!note.contentEquals("")){
                            dbHandler.updatePlaceNote(place , note , Constants.NOTE_STATE_ACTIVE, 0 , null);
                            new Starter(activity).startStopLocationService();
                        }else{
                            dbHandler.updatePlaceNote(place , note , Constants.NOTE_STATE_EMPTY, 0 , null);
                        }
                        new PlaceListPopulator(activity).populate();
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setTitle(R.string.note);
        dialog.show();
    }

    void clearNote(final String placeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.confirm_delete_note)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.updatePlaceNote(placeName , "" , Constants.NOTE_STATE_EMPTY, 0 , null);
                        new PlaceListPopulator(activity).populate();
                        new Starter(activity).startStopLocationService();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(placeName);
        alert.setCancelable(true);
        alert.show();
    }

    void editPlace(final String place){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

        final EditText nameEditText = new EditText(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        nameEditText.setLayoutParams(params);
        nameEditText.setText(place);
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(25)});
        nameEditText.setSelection(nameEditText.getText().length());
        dialogBuilder.setView(nameEditText);
        dialogBuilder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newPlaceName = nameEditText.getText().toString();
                        if(!dbHandler.placeExists(newPlaceName)){
                            dbHandler.updatePlaceNote(place , null , null , null , newPlaceName);
                            new PlaceListPopulator(activity).populate();
                        }else if(!newPlaceName.contentEquals(place)){
                            new CustomToast().makeToast(activity , Constants.WARNING_TOAST , activity.getString(R.string.place_exists));
                        }
                    }

                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setTitle(R.string.place_name);
        dialog.show();
    }

    void deletePlace(final String placeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String confirm = activity.getResources().getString(R.string.confirm_delete_place);
        builder.setMessage(String.format(confirm , placeName))
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.deletePlace(placeName);
                        new PlaceListPopulator(activity).populate();
                        new Starter(activity).startStopLocationService();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(placeName);
        alert.setCancelable(true);
        alert.show();
    }

    void clearNotes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.confirm_delete_all_notes)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.clearNotes();
                        new PlaceListPopulator(activity).populate();
                        new Starter(activity).startStopLocationService();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCancelable(true);
        alert.show();
    }

    void clearDB() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.confirm_delete_places)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.clearDb();
                        new PlaceListPopulator(activity).populate();
                        new Starter(activity).startStopLocationService();
                    }
                })
                .setNegativeButton(R.string.cancel , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCancelable(true);
        alert.show();
    }
}
