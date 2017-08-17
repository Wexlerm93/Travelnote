package de.ur.mi.travelnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wexle on 16.08.2017.
 */

public class DiaryDbAdapter {

    private DiaryDbHelper helper;
    private SQLiteDatabase db;

    public static final String DB_NAME = "diary_db";
    public static final int DB_VERSION = 1;

    public static final String DIARY_TABLE = "my_diary_table";
    public static final String KEY_ID = "_id";
    public static final String KEY_BODY = "body";
    public static final String KEY_DATE = "date";

    public DiaryDbAdapter(Context context) {
        helper = new DiaryDbHelper(context, DB_NAME, null, DB_VERSION);
    }

    public void open() {
        db = helper.getWritableDatabase();
    }

    public void close() {
        db.close();
        helper.close();
    }

    public long insertMyObject(DiaryEntry item) {
        ContentValues v = new ContentValues();
        v.put(KEY_BODY, item.getBody().toString());
        v.put(KEY_DATE, item.getFormattedDate().toString());
        long newInsertId =db.insert(DIARY_TABLE, null, v);
        return newInsertId;
    }

    public Cursor getAllmyObjects() {
        String[] allColumns = new String[] {KEY_ID, KEY_BODY, KEY_DATE};
        Cursor results = db.query(DIARY_TABLE, allColumns, null, null, null, null, null);
        return results;
    }

    public void removeObject(long id) {
        String toDelete = KEY_ID + "=?";
        String[] deleteArgs = new String[] {String.valueOf(id)};
        db.delete(DIARY_TABLE, toDelete, deleteArgs);
    }

    private class DiaryDbHelper extends SQLiteOpenHelper {

        private static final String CREATE_DB = "create table " + DIARY_TABLE
                + " (" + KEY_ID + " integer primary key autoincrement, "
                + KEY_BODY + " text not null, "
                + KEY_DATE + " text not null);";

        public DiaryDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
