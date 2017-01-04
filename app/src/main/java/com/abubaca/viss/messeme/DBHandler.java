package com.abubaca.viss.messeme;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by viss on 1/2/17.
 */

public class DBHandler extends SQLiteOpenHelper {

    private static int databaseVersion = 1;
    private final static String createTableQuery = "CREATE TABLE IF NOT EXISTS PLACENOTES(PLACE TEXT , LAT TEXT , LGN TEXT , NOTE TEXT)";

    private static SQLiteDatabase db;
    private final static String TAG = "DBHandler";
    public Cursor cursor;

    public DBHandler(Context context) {
        super(context, "messeme" , null , databaseVersion);
        Log.i(TAG , "created database!!!");
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(createTableQuery);
    }

    private final void dbInit(DBHandler dbHandler){
        db = dbHandler.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM PLACENOTES" , null);
        cursor.moveToFirst();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteDb(DBHandler dbHandler){
        dbInit(dbHandler);
        if(cursor.getCount()>0) {
            db.execSQL("DELETE FROM PLACENOTES");
            Log.i(TAG, "database cleared");
        }
    }

    public void deleteNotes(DBHandler dbHandler){
        dbInit(dbHandler);
        db.execSQL("UPDATE PLACENOTES SET NOTE=''");
        Log.i(TAG , "notes cleared");
    }

    public void deletePlace(DBHandler dbHandler , String place){
        dbInit(dbHandler);
        db.delete("PLACENOTES", "PLACE='" + place + "'", null);
        Log.i(TAG , "deleted place: "+place);
    }

    public void insertToDb(DBHandler dbHnadler , String place, String lat, String lgn , String note){
        dbInit(dbHnadler);
        db.execSQL("INSERT INTO PLACENOTES (PLACE,LAT,LGN,NOTE) VALUES ('"+place+"','"+lat+"','"+lgn+"','"+note+"')");
    }

    public void updateNote(DBHandler dbHandler , String place , String newNote){
        dbInit(dbHandler);
        db.execSQL("UPDATE PLACENOTES SET NOTE='"+newNote+"' WHERE PLACE='"+place+"'");
    }

    public void updatePlaceName(DBHandler dbHandler , String place, String newName){
        dbInit(dbHandler);
        db.execSQL("UPDATE PLACENOTES SET PLACE='"+newName+"' WHERE PLACE='"+place+"'");
    }

    public List<PlaceNote> getPlaceNotes(DBHandler dbHandler){
        dbInit(dbHandler);
        List<PlaceNote> placeNotes = new ArrayList<>();
        if(cursor.getCount()>0) {
            do {
                PlaceNote placeNote = new PlaceNote(cursor.getString(0), cursor.getString(3));
                placeNotes.add(placeNote);
            }while(cursor.moveToNext());
        }
        return placeNotes;
    }

    public List<Location> getNotesLocations(DBHandler dbHandler){
        dbInit(dbHandler);
        Double lat , lgn;
        List<Location> placeLocations = new ArrayList<>();

        if(cursor.getCount()>0){
            do{
                if(!cursor.getString(3).isEmpty()) {
                    Location singlePlaceLocation = new Location("");
                    lat = Double.valueOf(cursor.getString(1));
                    lgn = Double.valueOf(cursor.getString(2));
                    singlePlaceLocation.setLatitude(lat);
                    singlePlaceLocation.setLongitude(lgn);

                    placeLocations.add(singlePlaceLocation);
                    Log.i(TAG, "location added to array " + cursor.getPosition() + " " + singlePlaceLocation);
                }
            }while(cursor.moveToNext());
        }
        return placeLocations;
    }

    public String getPlaceNote(DBHandler dbHandler , String place){
        dbInit(dbHandler);
        String note = "";
        if(cursor.getCount()>0){
            do {
                if(cursor.getString(0).contentEquals(place)){
                    note = cursor.getString(3);
                }
            }while(cursor.moveToNext());
        }
        return note;
    }

    public String getPlaceFromLocation(DBHandler dbHandler , Location location){
        dbInit(dbHandler);
        String place = "";
        //DecimalFormat df = new DecimalFormat("#.####");
        //then use with df.format(Double goes here)
        String lat = String.valueOf(location.getLatitude());
        String lgn = String.valueOf(location.getLongitude());
        Log.i(TAG , String.valueOf(location.getLongitude()));
        Log.i(TAG , String.valueOf(location.getLatitude()));
        if(cursor.getCount()>0){
            do {
                Log.i(TAG , "cursor lat / lgn"+cursor.getString(1)+ " - "+ cursor.getString(2));
                if (cursor.getString(1).contentEquals(lat) && cursor.getString(2).contentEquals(lgn)){
                    place = cursor.getString(0);
                }
            }while(cursor.moveToNext());
        }
        return place;
    }

    public Cursor getFullCursor(DBHandler dbHandler){
        dbInit(dbHandler);
        return this.cursor;
    }

    public int noteCounter(DBHandler dbHandler){
        dbInit(dbHandler);
        int counter = 0;
        if(cursor.getCount()>0){
            do {
                if(!cursor.getString(3).toString().isEmpty()) {
                    counter++;
                }
            }while (cursor.moveToNext()) ;
        }
        Log.i(TAG , "Notes counter = "+counter);
        return counter;
    }
}
