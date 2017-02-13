package com.abubaca.viss.messeme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by viss on 1/2/17.
 */

public class DBHandler extends SQLiteOpenHelper {

    private static int databaseVersion = 1;
    private final static String createTableQuery = "CREATE TABLE IF NOT EXISTS PLACENOTES(PLACE TEXT , " +
            "LAT TEXT , LNG TEXT , NOTE TEXT ," +
            " STATE INTEGER DEFAULT 0 , NOTIFIED INTEGER DEFAULT 0 ," +
            " PROXIMITY INTEGER)";

    private static SQLiteDatabase db;
    private final static String TAG = "DBHANDLER";
    public Cursor cursor;

    public DBHandler(Context context) {
        super(context, "messeme" , null , databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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

    public void clearDb(){
        dbInit();
        if(cursor.getCount()>0) {
            db.execSQL("DELETE FROM PLACENOTES");
        }
        dbClose();
    }

    public void deleteNotes(){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("NOTE" , "");
        values.put("STATE" , 0);
        values.put("NOTIFIED" , 0);
        db.update("PLACENOTES" , values , null , null );
        dbClose();
    }

    public void deletePlace(String place){
        dbInit();
        db.delete("PLACENOTES", "PLACE='" + place + "'", null);
        dbClose();
    }

    public void insertToDb(String place, String lat, String lng , String note , int proximity){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("PLACE",place);
        values.put("LAT",lat.substring(0,10));
        values.put("LNG",lng.substring(0,10));
        values.put("NOTE",note);
        values.put("PROXIMITY",proximity);
        db.insert("PLACENOTES" , null , values);
        dbClose();
    }

    public void updateNote(String place , String newNote , int state , Integer notified){
        dbInit();
        ContentValues values = new ContentValues();
        if(newNote != null) values.put("NOTE" , newNote);
        values.put("STATE" , state);
        if(notified != null) values.put("NOTIFIED" , notified);
        db.update("PLACENOTES" , values , "PLACE=?", new String[]{place});
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
                placeNotes.add(placeNote);
            }while(cursor.moveToNext());
        }
        dbClose();
        return placeNotes;
    }

    public List<Location> getNotesLocations(){
        dbInit();
        Double lat , lng;
        List<Location> placeLocations = new ArrayList<>();

        if(cursor.getCount()>0){
            do{
                if(!cursor.getString(3).isEmpty() && cursor.getInt(4) == 2 && cursor.getInt(5) == 0){
                    Location singlePlaceLocation = new Location("");
                    lat = Double.valueOf(cursor.getString(1));
                    lng = Double.valueOf(cursor.getString(2));
                    singlePlaceLocation.setLatitude(lat);
                    singlePlaceLocation.setLongitude(lng);
                    placeLocations.add(singlePlaceLocation);
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return placeLocations;
    }

    public String getPlaceNote(String place){
        dbInit();
        if(cursor.getCount()>0){
            do {
                if(cursor.getString(0).contentEquals(place)){
                    break;
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getString(3);
    }

    public String getPlaceFromLocation(Location location){
        dbInit();
        String lat = String.valueOf(location.getLatitude());
        String lng = String.valueOf(location.getLongitude());
        if(cursor.getCount()>0){
            do {
                if (cursor.getString(1).contentEquals(lat) && cursor.getString(2).contentEquals(lng)){
                    break;
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getString(0);
    }

    public int getPlaceProximity(String place){
        dbInit();
        if(cursor.getCount()>0){
            do{
                if(cursor.getString(0).contentEquals(place)){
                    break;
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getInt(6);
    }

    public Boolean isNotified(String place){
        dbInit();
        if(cursor.getCount()>0){
            do {
                if(cursor.getString(0).contentEquals(place)){
                     break;
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getInt(5) == 1;
    }

    public Cursor getFullCursor(){
        dbInit();
        dbClose();
        return this.cursor;
    }

}
