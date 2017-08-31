package de.ur.mi.travelnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.Calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by wexle on 16.08.2017.
 */

public class DiaryDbDatabase {

    private DiaryDbHelper helper;
    private SQLiteDatabase db;

    public static final String DB_NAME = "diary_db";
    public static final int DB_VERSION = 10;

    public static final String DIARY_TABLE = "my_diary_table";
    public static final String KEY_ID = "_id";
    public static final String KEY_BODY = "body";
    public static final String KEY_DATE = "date";
    public static final String KEY_PLACE = "place";

    public DiaryDbDatabase(Context context) {
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
        v.put(KEY_PLACE, item.getPlace().toString());
        long newInsertId =db.insert(DIARY_TABLE, null, v);
        return newInsertId;
    }

    public ArrayList<DiaryEntry> getAllmyObjects() {
        ArrayList<DiaryEntry> items = new ArrayList<DiaryEntry>();
        String[] allColumns = new String[] {KEY_ID, KEY_BODY, KEY_DATE, KEY_PLACE};
        Cursor results = db.query(DIARY_TABLE, allColumns, null, null, null, null, null);
        if (results.moveToFirst()) {
            do {
                String content = results.getString(1);
                String date = results.getString(2);
                String place = results.getString(3);

                Date formattedDate = null;
                try {
                    formattedDate = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).parse(date);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    Calendar cal = Calendar.getInstance(Locale.GERMAN);
                    cal.setTime(formattedDate);
                    items.add(new DiaryEntry(content, cal.get(Calendar.DAY_OF_MONTH) ,cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), place));
                }
            }while (results.moveToNext());
        }
        return items;
    }

    public void removeObject(DiaryEntry entry) {
        String toDelete = KEY_ID + "=?";
        String[] deleteArgs = new String[] {entry.getBody()};
        db.delete(DIARY_TABLE, toDelete, deleteArgs);
    }

    public void reset() throws SQLException {
        db.execSQL("drop table " + DB_NAME);
    }


    private class DiaryDbHelper extends SQLiteOpenHelper {

        private static final String CREATE_DB = "create table " + DIARY_TABLE
                + " (" + KEY_ID + " integer primary key autoincrement, "
                + KEY_BODY + " text not null, "
                + KEY_DATE + " text not null, "
                + KEY_PLACE + " text not null);";

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

    public void clearDatabase(String TABLE_NAME) {
        String clearDBQuery = "DELETE FROM "+TABLE_NAME;
        db.execSQL(clearDBQuery);
    }
}
