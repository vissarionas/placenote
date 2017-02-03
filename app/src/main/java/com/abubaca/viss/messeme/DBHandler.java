package com.abubaca.viss.messeme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viss on 1/2/17.
 */

public class DBHandler extends SQLiteOpenHelper {

    private static int databaseVersion = 1;
    private final static String createTableQuery = "CREATE TABLE IF NOT EXISTS PLACENOTES(PLACE TEXT , LAT TEXT , LGN TEXT , NOTE TEXT , STATE INTEGER DEFAULT 0)";

    private static SQLiteDatabase db;
    private final static String TAG = "DBHandler";
    public Cursor cursor;

    public DBHandler(Context context) {
        super(context, "messeme" , null , databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(createTableQuery);
    }

    private final void dbInit(){
        db = this.getWritableDatabase();
        cursor = db.rawQuery("SELECT * FROM PLACENOTES" , null);
        cursor.moveToFirst();
    }

    private final void dbClose(){
        if(db.isOpen()){
            db.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteDb(){
        dbInit();
        if(cursor.getCount()>0) {
            db.execSQL("DELETE FROM PLACENOTES");
            Log.i(TAG, "database cleared");
        }
        dbClose();
    }

    public void deleteNotes(){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("NOTE" , "");
        values.put("STATE" , 0);
        db.update("PLACENOTES" , values , null , null );
//        db.execSQL("UPDATE PLACENOTES SET NOTE=''");
        Log.i(TAG , "notes cleared");
        dbClose();
    }

    public void deletePlace(String place){
        dbInit();
        db.delete("PLACENOTES", "PLACE='" + place + "'", null);
        Log.i(TAG , "deleted place: "+place);
        dbClose();
    }

    public void insertToDb(String place, String lat, String lgn , String note){
        dbInit();
        db.execSQL("INSERT INTO PLACENOTES (PLACE,LAT,LGN,NOTE) VALUES ('"+place+"','"+lat+"','"+lgn+"','"+note+"')");
        dbClose();
    }

    public void updateNote(String place , String newNote , int state){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("NOTE" , newNote);
        values.put("STATE" , state);
        db.update("PLACENOTES" , values , "PLACE=?", new String[]{place});
        dbClose();
    }

    public void setState(String place , int active){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("STATE" , active);
        db.update("PLACENOTES" , values , "PLACE=?" , new String[]{place});
        dbClose();
    }

    public void updatePlaceName(String place, String newName){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("PLACE" , newName);
        db.update("PLACENOTES" , values , "PLACE=?" , new String[]{place});
        dbClose();
    }

    public List<PlaceNote> getPlaceNotes(){
        dbInit();
        List<PlaceNote> placeNotes = new ArrayList<>();
        if(cursor.getCount()>0) {
            do {
                PlaceNote placeNote = new PlaceNote(cursor.getString(0), cursor.getString(3) , cursor.getInt(4));
                Log.e(TAG , "placenote state: "+cursor.getInt(4));
                placeNotes.add(placeNote);
            }while(cursor.moveToNext());
        }
        dbClose();
        return placeNotes;
    }

    public List<Location> getNotesLocations(){
        dbInit();
        Double lat , lgn;
        List<Location> placeLocations = new ArrayList<>();

        if(cursor.getCount()>0){
            do{
                if(!cursor.getString(3).isEmpty() && cursor.getInt(4) == 1) {
                    Location singlePlaceLocation = new Location("");
                    lat = Double.valueOf(cursor.getString(1));
                    lgn = Double.valueOf(cursor.getString(2));
                    singlePlaceLocation.setLatitude(lat);
                    singlePlaceLocation.setLongitude(lgn);
                    placeLocations.add(singlePlaceLocation);
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return placeLocations;
    }

    public String getPlaceNote(String place){
        dbInit();
        String note = "";
        if(cursor.getCount()>0){
            do {
                if(cursor.getString(0).contentEquals(place)){
                    note = cursor.getString(3);
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return note;
    }

    public String getPlaceFromLocation(Location location){
        dbInit();
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
        dbClose();
        return place;
    }

    public Cursor getFullCursor(){
        dbInit();
        dbClose();
        return this.cursor;
    }

}
