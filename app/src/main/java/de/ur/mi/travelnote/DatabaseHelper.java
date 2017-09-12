package de.ur.mi.travelnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper{

    public static final String TABLE_NAME = "coordinates_table";
    private static final String COL0 = "ID";
    private static final String COL1 = "lat";
    private static final String COL2 = "lng";
    private static final String COL3 = "user";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL1 + " DOUBLE, " + COL2 + " DOUBLE, " + COL3 + " STRING)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
       sqLiteDatabase.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);

    }

    public boolean addCoordinates(double lat, double lng, String user){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, lat);
        contentValues.put(COL2, lng);
        contentValues.put(COL3, user);

        long result = sqLiteDatabase.insert(TABLE_NAME,null,contentValues);
        if(result == -1){
            return false;
        }else {
            return true;
        }
    }

    public Cursor getData(String userID){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL3 + "= '" + userID + "'";
        Cursor data = sqLiteDatabase.rawQuery(query, null);
        return data;
    }

    public void clearDatabase() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM " + TABLE_NAME;
        sqLiteDatabase.execSQL(clearDBQuery);
    }
}
