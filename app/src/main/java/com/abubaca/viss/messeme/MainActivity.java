package com.abubaca.viss.messeme;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MAIN_ACTIVITY";
    private static final int FINE_LOCATION_REQUEST = 0x1;

    private DBHandler dbHandler;

    private TextView noPlacesTextview;

    private ListView list_view;
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setSubtitle(R.string.main_subtite);
        dbHandler = new DBHandler(getApplicationContext());

        noPlacesTextview = (TextView)findViewById(R.id.no_places_textview);
        noPlacesTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noPlacesTextview.setVisibility(View.INVISIBLE);
                startMapActivity();
            }
        });
        list_view = (ListView) findViewById(R.id.list_view);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View footerView = inflater.inflate(R.layout.list_footer , null);
        list_view.addFooterView(footerView);
    }

    @Override
    protected void onResume() {
        populateList();
        startStopService();
        super.onResume();
    }

    private void startStopService(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST );
            return;
        }
        Intent i = new Intent(this , FusedBackground.class);
        startService(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_drop_db:
                confirmDropDb();
                break;
            case R.id.action_drop_notes:
                confirmDropNotes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "granted permission for coarse location");
                } else {
                    Log.e(TAG, "permission for location denied");
                }
                return;
            }
        }
    }

    private void populateList() {
        List<PlaceNote> placeNotes = dbHandler.getPlaceNotes();
        if(placeNotes.size()==0) {
            noPlacesTextview.setVisibility(View.VISIBLE);
            list_view.setVisibility(View.INVISIBLE);
            noPlacesTextview.setText("You have no places in your placelist.\n\nClick here and set your first place");
        }else{
            list_view.setVisibility(View.VISIBLE);
            noPlacesTextview.setVisibility(View.INVISIBLE);
        }
        adapter = new CustomAdapter(getApplicationContext(), placeNotes);
        list_view.setAdapter(adapter);
        registerForContextMenu(list_view);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    viewNote(adapter.getPlace(position));
                } catch (IndexOutOfBoundsException e) {
                    startMapActivity();
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.edit_place_menu , menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.edit_name:
                editPlaceDialog(adapter.getPlace(info.position));
                return true;
            case R.id.view_on_map:
                showPlaceMap(adapter.getPlace(info.position));
                return true;
            case R.id.clear_note:
                confirmDropNote(adapter.getPlace(info.position));
                return true;
            case R.id.delete_place:
                confirmDropPlace(adapter.getPlace(info.position));
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void startMapActivity() {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);
    }

    private void confirmDropDb() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all places?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.clearDb();
                        onResume();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCancelable(true);
        alert.show();
    }

    private void confirmDropNotes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all notes?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.deleteNotes();
                        onResume();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCancelable(true);
        alert.show();
    }

    private void confirmDropNote(final String placeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this note?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.updateNote(placeName , "" , 0 , 0);
                        onResume();
                        populateList();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(placeName);
        alert.setCancelable(true);
        alert.show();
    }

    private void confirmDropPlace(final String placeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to remove "+placeName+"")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHandler.deletePlace(placeName);
                        populateList();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(placeName);
        alert.setCancelable(true);
        alert.show();
    }

    private void editPlaceDialog(final String place){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        final EditText nameEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        nameEditText.setLayoutParams(params);
        nameEditText.setText(place);
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(20)});
        nameEditText.setSelection(nameEditText.getText().length());
        dialogBuilder.setView(nameEditText);
        dialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHandler.updatePlaceName(place , nameEditText.getText().toString());
                        populateList();
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().getAttributes().verticalMargin = -0.2F;
        dialog.setTitle("Place name");
        dialog.show();
    }

    private void viewNote(final String place){
        if(dbHandler.isNotified(place))dbHandler.updateNote(place , null , 1 , null);
        LayoutInflater inflater = getLayoutInflater();
        View editView = inflater.inflate(R.layout.edit_note , null);
        editView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        String note = dbHandler.getPlaceNote(place);
        ImageButton btnDelete;
        final TextView noteTextView;
        noteTextView = (TextView)editView.findViewById(R.id.note_text_view);
        btnDelete = (ImageButton)editView.findViewById(R.id.btn_delete);
        if(note.isEmpty()){
            btnDelete.setVisibility(View.INVISIBLE);
        }else{
            noteTextView.setText(note);
        }

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(editView);
        dialogBuilder.setTitle(place);
        final Dialog dialog = dialogBuilder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                populateList();
            }
        });
        dialog.show();
        noteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNoteDialog(place);
                dialog.dismiss();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDropNote(place);
                dialog.dismiss();
            }
        });
    }

    private void editNoteDialog(final String place){
        String prevNote = dbHandler.getPlaceNote(place);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        final EditText noteEditText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        noteEditText.setLayoutParams(params);
        if(!prevNote.isEmpty()){
            noteEditText.setText(prevNote);
        }else{
            noteEditText.setHint("type a note here");
        }
        noteEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        noteEditText.setSelection(noteEditText.getText().length());

        dialogBuilder.setView(noteEditText);
        dialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String note = noteEditText.getText().toString();
                        if(!note.contentEquals("")){
                            dbHandler.updateNote(place , note , 2 , 0);
                        }else{
                            dbHandler.updateNote(place , note , 0 , 0);
                        }
                        MainActivity.this.onResume();
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().getAttributes().verticalMargin = -0.2F;
        dialog.setTitle("Note");
        dialog.show();
    }

    private void showPlaceMap(String placeName){
        Intent intent = new Intent(MainActivity.this, ViewPlaceActivity.class);
        intent.putExtra("placeName" , placeName);
        startActivity(intent);
    }

}


