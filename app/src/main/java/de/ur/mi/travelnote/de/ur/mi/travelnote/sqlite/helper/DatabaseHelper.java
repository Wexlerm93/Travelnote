package de.ur.mi.travelnote.de.ur.mi.travelnote.sqlite.helper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 9;
    private static final String DATABASE_NAME = "travelnoteDatabaseBasic";

    private static final String TABLE_MAP_COORDINATES = "map_marker";
    private static final String TABLE_DIARY_ENTRIES = "diary_entries_table";
    private static final String TABLE_GALLERY_BITMAPS = "gallery_table";

    // Related to all Tables
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String ORIGIN = "origin";


    //Related to Map Coordinates Table
    private static final String MAP_LAT = "lat";
    private static final String MAP_LNG = "lng";
    private static final double BIASED_LAT = -66.666666;
    private static final double BIASED_LNG = -145.678901;
    private static final String EXCLUDE_BIASED_COORDINATES =  "') AND (" + MAP_LAT + " != " + BIASED_LAT + " ) AND (" + MAP_LNG + " != " + BIASED_LNG + "))";
    private static final String CREATE_TABLE_MAP_MARKER =
            "CREATE TABLE " + TABLE_MAP_COORDINATES + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + MAP_LAT + " DOUBLE, " + MAP_LNG + " DOUBLE, " + USER_ID + " STRING, " + USER_NAME + " STRING, " + ORIGIN + " INTEGER)";

    //Related to Diary Contents
    private static final String DIARY_ENTRY_TITLE = "diary_title";
    private static final String DIARY_ENTRY_CONTENT = "diary_content";
    private static final String DIARY_ENTRY_LOC_TEXT = "diary_loc_text";
    private static final String DIARY_ENTRY_DATE = "diary_date";
    private static final String CREATE_TABLE_DIARY_CONTENT =
            "CREATE TABLE " + TABLE_DIARY_ENTRIES + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + DIARY_ENTRY_TITLE + " STRING, " + DIARY_ENTRY_CONTENT + " STRING, " + DIARY_ENTRY_LOC_TEXT + " STRING, "
                    + DIARY_ENTRY_DATE + " DATE," + USER_ID + " STRING, " + USER_NAME + " STRING)";

    //Related to Gallery Table
    private static final String GALLERY_IMAGES = "image";
    private static final String CREATE_TABLE_GALLERY = "CREATE TABLE " + TABLE_GALLERY_BITMAPS
            + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + GALLERY_IMAGES + " STRING)";



    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_MAP_MARKER);
        sqLiteDatabase.execSQL(CREATE_TABLE_DIARY_CONTENT);
        sqLiteDatabase.execSQL(CREATE_TABLE_GALLERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MAP_COORDINATES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY_ENTRIES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + TABLE_GALLERY_BITMAPS);
        onCreate(sqLiteDatabase);
    }


    public boolean addCoordinates(double lat, double lng, String userID, String userName, int originID){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MAP_LAT, lat);
        contentValues.put(MAP_LNG, lng);
        contentValues.put(USER_ID, userID);
        contentValues.put(USER_NAME, userName);
        contentValues.put(ORIGIN, originID);

        long result = sqLiteDatabase.insert(TABLE_MAP_COORDINATES,null,contentValues);
        if(result == -1){
            return false;
        }else {
            return true;
        }
    }

    public Cursor getMapCoordinates(String userID){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_MAP_COORDINATES + " WHERE(( " + USER_ID + "= '" + userID + "') AND (" + MAP_LAT + " != " + BIASED_LAT + " ) AND (" + MAP_LNG + " != " + BIASED_LNG + "))";
        return sqLiteDatabase.rawQuery(query, null);
    }

    public Cursor getMapCoordinatesAllUser(String userID){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_MAP_COORDINATES + " WHERE(( " + USER_ID + " != '" + userID + EXCLUDE_BIASED_COORDINATES;
        return sqLiteDatabase.rawQuery(query, null);
    }

    public void clearLocationEntryCurrentUser(String userID, long ident){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String clearDBEntryQuery = "DELETE FROM " + TABLE_MAP_COORDINATES + " WHERE(( " + USER_ID + "= '" + userID + "') AND (_id = " + ident +"))";
        sqLiteDatabase.execSQL(clearDBEntryQuery);
    }

    public void clearTableMapCoordinatesCurrentUser(String userID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM " + TABLE_MAP_COORDINATES + " WHERE " + USER_ID + "= '" + userID + "'";
        sqLiteDatabase.execSQL(clearDBQuery);
    }

    public boolean addDiaryEntry(String title, String content, String location, double lat, double lng, String date, String userID, String userName){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DIARY_ENTRY_TITLE, title);
        contentValues.put(DIARY_ENTRY_CONTENT, content);
        contentValues.put(DIARY_ENTRY_LOC_TEXT, location);
        contentValues.put(DIARY_ENTRY_DATE, date);
        contentValues.put(USER_ID, userID);
        contentValues.put(USER_NAME, userName);

        addCoordinates(lat, lng, userID, userName, 0);

        long result = sqLiteDatabase.insert(TABLE_DIARY_ENTRIES, null,contentValues);

        //return true, if insert was successfull (result is greater than -1)
        return (result > -1);
    }

    public Cursor getDiaryEntriesCurrentUser(String userID){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_DIARY_ENTRIES + " WHERE " + USER_ID + " = '" + userID + "'";
        return sqLiteDatabase.rawQuery(query, null);
    }


    public boolean clearTableDiaryEntriesCurrentUser(String userID, int originNo) {
        boolean result;
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String clearDBQuery = "DELETE FROM " + TABLE_DIARY_ENTRIES + " WHERE " + USER_ID + "= '" + userID + "'";
            String clearDBQueryMap = "DELETE FROM " + TABLE_MAP_COORDINATES + " WHERE(( " + USER_ID + "= '" + userID + "') AND (" + ORIGIN + "= "  + originNo + "))";
            sqLiteDatabase.execSQL(clearDBQuery);
            sqLiteDatabase.execSQL(clearDBQueryMap);

            result = true;
        }catch (SQLiteAbortException e){
            result = false;
        }
        return result;

    }

    public boolean clearDiaryEntryCurrentUser(String userID, int id) {
        boolean result;
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String clearDBQuery = "DELETE FROM " + TABLE_DIARY_ENTRIES + " WHERE(( " + USER_ID + "= '" + userID + "') AND (_id = " + id +"))" ;
            sqLiteDatabase.execSQL(clearDBQuery);
            result = true;
        }catch (SQLiteAbortException e){
            result = false;
        }
        return result;

    }

    public Cursor getSelectedDiaryEntry(long id){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_DIARY_ENTRIES + " WHERE _id = " + id;
        return sqLiteDatabase.rawQuery(query, null);
    }


    public boolean addImage(Bitmap image) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GALLERY_IMAGES, bitmapToString(image));

        long result = sqLiteDatabase.insert(TABLE_GALLERY_BITMAPS, null, contentValues);

        return (result > -1);
    }

    public Cursor getImagesCurrentUser(String userID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_GALLERY_BITMAPS + " WHERE " + USER_ID + " = '" + userID + "'";
        return sqLiteDatabase.rawQuery(query, null);
    }


    public final static String bitmapToString(Bitmap in){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        in.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        return Base64.encodeToString(bytes.toByteArray(),Base64.DEFAULT);
    }





}
