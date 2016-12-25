package com.abubaca.viss.messeme;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NoteActivity extends AppCompatActivity {

    final String TAG = "NOTE_ACTIVITY";
    private String placeName, note, previousNote;
    SQLiteDatabase db;
    EditText editNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editNote = (EditText) findViewById(R.id.edit_note);
        db = openOrCreateDatabase("messeme", MODE_PRIVATE, null);
        placeName = getIntent().getStringExtra("placeName");

        populateNote(placeName);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                note = editNote.getText().toString();
                if (!TextUtils.isEmpty(note)) {
                    Toast.makeText(getApplicationContext() , "note "+note+" inserted" , Toast.LENGTH_SHORT).show();
                    insertNoteIntoDb(placeName, note);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Note is empty. Nothing to store", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_drop_note) {
            confirmDropNote();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDropNote(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this note?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        db.execSQL("DELETE FROM NOTES WHERE PLACE ='"+placeName+"'");
                        db.execSQL("UPDATE PLACENOTES SET NOTE='' WHERE PLACE='"+placeName+"'");
                        Log.i(TAG , "note deleted - null");
                        NoteActivity.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void populateNote(String placeName) {
        final Cursor cursor = db.rawQuery("SELECT NOTE FROM PLACENOTES WHERE PLACE = '" + placeName + "'", null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            previousNote = cursor.getString(0);
            editNote.setText(previousNote);
        }
    }

    private void insertNoteIntoDb(String placeName, String noteText) {
        db.execSQL("UPDATE PLACENOTES SET NOTE='"+noteText+"' WHERE PLACE='"+placeName+"'");

    }


}
