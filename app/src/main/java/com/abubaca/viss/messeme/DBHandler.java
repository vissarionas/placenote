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
 *
 * PLACENOTES NOTIFIED: 0=NOT_NOTIFIED , 1=NOTIFIED
 */

class DBHandler extends SQLiteOpenHelper {

    private static int databaseVersion = 1;
    private final static String createTableQuery = "CREATE TABLE IF NOT EXISTS PLACENOTES(PLACE TEXT , " +
            "LAT TEXT , LNG TEXT , NOTE TEXT ," +
            " STATE INTEGER DEFAULT 0 , NOTIFIED INTEGER DEFAULT 0 ," +
            " PROXIMITY INTEGER)";
    private final static String selectAllFromTable = "SELECT * FROM PLACENOTES ORDER BY STATE DESC , PLACE ASC";
    private final static String TAG = "DBHANDLER";

    private static SQLiteDatabase db;
    private Cursor cursor;
    private Context context;

    DBHandler(Context context) {
        super(context, "messeme" , null , databaseVersion);
        this.context = context;
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
            db.execSQL("DELETE FROM PLACENOTES");
        }
        dbClose();
    }

    void clearNotes(){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("NOTE" , "");
        values.put("STATE" , NoteState.EMPTY);
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
        if(lat.length()>10)lat = lat.substring(0,10);
        if(lng.length()>10)lng = lng.substring(0,10);
        ContentValues values = new ContentValues();
        values.put("PLACE",place);
        values.put("LAT",lat);
        values.put("LNG",lng);
        values.put("NOTE",note);
        values.put("PROXIMITY",proximity);
        db.insert("PLACENOTES" , null , values);
        dbClose();
    }

    void updateNote(String place , String newNote , int state , Integer notified){
        dbInit();
        ContentValues values = new ContentValues();
        if(newNote != null) values.put("NOTE" , newNote);
        values.put("STATE" , state);
        if(notified != null) values.put("NOTIFIED" , notified);
        db.update("PLACENOTES" , values , "PLACE=?", new String[]{place});
        dbClose();
    }

    void updatePlaceName(String place, String newName){
        dbInit();
        ContentValues values = new ContentValues();
        values.put("PLACE" , newName);
        db.update("PLACENOTES" , values , "PLACE=?" , new String[]{place});
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
            do{
                if(!cursor.getString(3).isEmpty() && cursor.getInt(4) == NoteState.ACTIVE && cursor.getInt(5) == 0){
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

    String getPlaceNote(String place){
        dbInit();
        if(cursor.getCount()>0){
            do {
                if(cursor.getString(0).equals(place)){
                    break;
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getString(3);
    }

    String getPlaceFromLocation(Location location){
        dbInit();
        String lat = String.valueOf(location.getLatitude());
        String lng = String.valueOf(location.getLongitude());
        if(cursor.getCount()>0){
            do {
                if (cursor.getString(1).equals(lat) && cursor.getString(2).equals(lng)){
                    break;
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getString(0);
    }

    int getPlaceProximity(String place){
        dbInit();
        if(cursor.getCount()>0){
            do{
                if(cursor.getString(0).equals(place)){
                    break;
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getInt(6);
    }

    Boolean isNotified(String place){
        dbInit();
        if(cursor.getCount()>0){
            do {
                if(cursor.getString(0).equals(place)){
                     break;
                }
            }while(cursor.moveToNext());
        }
        dbClose();
        return cursor.getInt(5) == 1;
    }

    Cursor getFullCursor(){
        dbInit();
        dbClose();
        return this.cursor;
    }

}
