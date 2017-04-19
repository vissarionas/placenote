package com.abubaca.viss.notepin;

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

    private final static String TAG = "DBHANDLER";

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

    void clearNotes(){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("NOTE" , "");
        values.put("STATE" , Constants.NOTE_STATE_EMPTY);
        values.put("NOTIFIED" , 0);
        db.update("PLACENOTES" , values , null , null );
        dbClose();
    }

    void deletePlace(String place){
        dbInit();
        db.delete("PLACENOTES", "PLACE='" + place + "'", null);
        dbClose();
    }

    void insertToDb(String place, String lat, String lng , String note , int proximity){
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

    List<PlaceNote> getPlaceNotes(){
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

    List<Location> getNotesLocations(){
        dbInit();
        Double lat , lng;
        List<Location> placeLocations = new ArrayList<>();

        if(cursor.getCount()>0){
            do {
                if (!cursor.getString(3).isEmpty() && cursor.getInt(4) == Constants.NOTE_STATE_ACTIVE
                        && cursor.getInt(5) == 0) {
                    Location singlePlaceLocation = new Location("");
                    lat = Double.valueOf(cursor.getString(1));
                    lng = Double.valueOf(cursor.getString(2));
                    singlePlaceLocation.setLatitude(lat);
                    singlePlaceLocation.setLongitude(lng);
                    placeLocations.add(singlePlaceLocation);
                }
            } while (cursor.moveToNext());
        }
        dbClose();
        return placeLocations;
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
        placeLocation.setLatitude(Double.valueOf(cursor.getString(1)));
        placeLocation.setLongitude(Double.valueOf(cursor.getString(2)));
        return placeLocation;
    }

    String getPlaceByLocation(Location location){
        dbInit();
        String lat = String.valueOf(location.getLatitude());
        String lng = String.valueOf(location.getLongitude());
        if(cursor.getCount()>0){
            do {
                if (cursor.getString(1).contentEquals(lat) && cursor.getString(2).contentEquals(lng)) break;
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getString(0);
    }

    int getPlaceProximity(String place){
        dbInit();
        if(cursor.getCount()>0){
            do{
                if(cursor.getString(0).contentEquals(place)) break;
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getInt(6);
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

    Cursor getFullCursor(){
        dbInit();
        dbClose();
        return this.cursor;
    }

}
