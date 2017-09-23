package de.ur.mi.travelnote;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;


public class ImageDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "customImages";
    private static final int DATABASE_VERSION = 7;
    private static final String TABLE_NAME = "imageview_table";
    private static final String USER_ID = "user_id";

    private static final String CREATE_TABLE_IMAGE_VIEW = "CREATE TABLE " + TABLE_NAME + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, title STRING, location STRING, image BLOB, " + USER_ID + " STRING )";


    public ImageDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_IMAGE_VIEW);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    public void insertData(String title, String location, byte[] image, String userID) {
        SQLiteDatabase database = this.getWritableDatabase();
        String sql = "INSERT INTO " + TABLE_NAME + " VALUES (NULL, ?, ?, ?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, title);
        statement.bindString(2, location);
        statement.bindBlob(3, image);
        statement.bindString(4, userID);
        statement.executeInsert();
    }


    public Cursor getImages(String userID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + USER_ID + " = '" + userID + "'";
        return sqLiteDatabase.rawQuery(query, null);
    }

    public boolean clearImage(long id, String userID) {
        boolean result;
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String clearDBQuery = "DELETE FROM " + TABLE_NAME + " WHERE(( _id = " + id + " ) AND ( " + USER_ID + "= '" + userID + "'))";
            sqLiteDatabase.execSQL(clearDBQuery);
            result = true;
        } catch (SQLiteAbortException e) {
            result = false;
        }
        return result;
    }

    public boolean clearImagesCurrentUser(String userID) {
        boolean result;
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            String clearDBQuery = "DELETE FROM " + TABLE_NAME + " WHERE(( " + USER_ID + "= '" + userID + "'))";
            sqLiteDatabase.execSQL(clearDBQuery);
            result = true;
        } catch (SQLiteAbortException e) {
            result = false;
        }
        return result;
    }


}
