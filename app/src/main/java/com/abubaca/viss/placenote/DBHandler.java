package com.abubaca.viss.placenote;

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
 *
 * PLACENOTES NOTIFIED: 0=NOT_NOTIFIED , 1=NOTIFIED
 * PLACENOTES USES_WIFI: 0=NOT_USES , 1=USES;
 *
 */

class DBHandler extends SQLiteOpenHelper{

    private static int databaseVersion = 1;
    private final static String createTableQuery = "CREATE TABLE IF NOT EXISTS PLACENOTES(PLACE TEXT , " +
            "LAT TEXT , LNG TEXT , NOTE TEXT ," +
            " STATE INTEGER DEFAULT 0 , NOTIFIED INTEGER DEFAULT 0 ," +
            " PROXIMITY INTEGER)";
    private final static String selectAllFromTable = "SELECT * FROM PLACENOTES ORDER BY STATE DESC , PLACE ASC";

    private static SQLiteDatabase db;
    private Cursor cursor;

    DBHandler(Context context) {
        super(context, "notepin" , null , databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void dbInit(){
        db = this.getWritableDatabase();
        cursor = db.rawQuery(selectAllFromTable, null);
        cursor.moveToFirst();
    }

    private void dbClose(){
        if(db.isOpen()){
            db.close();
        }
    }

    void clearDb(){
        dbInit();
        if(cursor.getCount()>0) {
            db.delete("PLACENOTES" , null , null);
        }
        dbClose();
    }

    void clearAllNotes(){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("NOTE" , "");
        values.put("STATE" , Constants.NOTE_STATE_EMPTY);
        values.put("NOTIFIED" , 0);
        db.update("PLACENOTES" , values , null , null );
        dbClose();
    }

    void clearSelectedNotes(List<String> selectedPlaces){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("NOTE" , "");
        values.put("STATE" , Constants.NOTE_STATE_EMPTY);
        values.put("NOTIFIED" , 0);
        for(String place : selectedPlaces){
            db.update("PLACENOTES" , values , "PLACE IN (?)" , new String[]{place} );
        }
        dbClose();
    }

    void deletePlace(String place){
        dbInit();
        db.delete("PLACENOTES", "PLACE='" + place + "'", null);
        dbClose();
    }

    void insertToDb(String place, Double lat, Double lng , String note , int proximity){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("PLACE",place);
        values.put("LAT",lat);
        values.put("LNG",lng);
        values.put("NOTE",note);
        values.put("PROXIMITY",proximity);
        db.insert("PLACENOTES" , null , values);
        dbClose();
    }

    void updatePlaceNote(String place , String note , Integer state , Integer notified , String newPlace){
        dbInit();
        ContentValues values = new ContentValues();
        if(newPlace!=null) values.put("PLACE" , newPlace);
        if(note != null) values.put("NOTE" , note);
        if(state!=null) values.put("STATE" , state);
        if(notified != null) values.put("NOTIFIED" , notified);
        db.update("PLACENOTES" , values , "PLACE=?", new String[]{place});
        dbClose();
    }

    List<Placenote> getPlaceNotes(){
        dbInit();
        List<Placenote> placenotes = new ArrayList<>();
        if(cursor.getCount()>0) {
            do {
                Placenote placenote = new Placenote(cursor.getString(0), cursor.getString(3) , cursor.getInt(4));
                placenotes.add(placenote);
            }while(cursor.moveToNext());
        }
        dbClose();
        return placenotes;
    }

    List<Placenote> getPlacenotesLocationAndProximity(){
        dbInit();
        List<Placenote> placenotes = new ArrayList<>();
        Double latitude , longitude;

        if(cursor.getCount()>0){
            do {
                if (!cursor.getString(3).isEmpty() && cursor.getInt(4) == Constants.NOTE_STATE_ACTIVE
                        && cursor.getInt(5) == 0) {
                    Location placeLocation = new Location("");
                    latitude = cursor.getDouble(1);
                    longitude = cursor.getDouble(2);
                    placeLocation.setLatitude(latitude);
                    placeLocation.setLongitude(longitude);

                    Placenote placenote = new Placenote(cursor.getString(0) , placeLocation , cursor.getInt(6));
                    placenotes.add(placenote);
                }
            } while (cursor.moveToNext());
        }
        dbClose();
        return placenotes;
    }

    String getPlaceNote(String place){
        dbInit();
        if(cursor.getCount()>0){
            do {
                if(cursor.getString(0).contentEquals(place)) break;
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getString(3);
    }

    Location getPlaceLocation(String place){
        dbInit();
        if(cursor.getCount()>0){
            do{
                if(cursor.getString(0).contentEquals(place)) break;
            } while (cursor.moveToNext());
        }
        dbClose();
        Location placeLocation = new Location("");
        Double latitude = cursor.getDouble(1);
        Double longitude = cursor.getDouble(2);
        placeLocation.setLatitude(latitude);
        placeLocation.setLongitude(longitude);
        return placeLocation;
    }

    Boolean isNotified(String place){
        dbInit();
        if(cursor.getCount()>0){
            do {
                if(cursor.getString(0).contentEquals(place)) break;
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getInt(5) == 1;
    }

    Boolean placeExists(String place){
        List<String> places = new ArrayList<>();
        dbInit();
        if(cursor.getCount()>0){
            do {
                places.add(cursor.getString(0));
            } while(cursor.moveToNext());
        }
        dbClose();
        return places.contains(place);
    }
}
