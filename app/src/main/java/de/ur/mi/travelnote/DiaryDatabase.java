package de.ur.mi.travelnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wexle on 15.08.2017.
 */

public class DiaryDatabase {
    private static final String DATABASE_NAME = "diary.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TABLE = "diaryitem";

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_CONTENT = "CONTENT";

    public static final int COLUMN_DATE_INDEX = 1;
    public static final int COLUMN_CONTENT_INDEX = 2;

    private DiaryOpenHelper diaryHelper;

    private SQLiteDatabase db;

    public DiaryDatabase(Context context) {
        diaryHelper = new DiaryOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void open () throws SQLException {
        try {
            db = diaryHelper.getWritableDatabase();
        } catch (SQLException e) {
            db = diaryHelper.getReadableDatabase();
        }
    }

    public void close() {
        db.close();
    }

    public long insertDiaryItem (DiaryItem item) {
        ContentValues itemvValues = new ContentValues();
        itemvValues.put(KEY_CONTENT, item.getContent());
        itemvValues.put(KEY_DATE, item.getFormattedDate());
    }





    private class DiaryOpenHelper extends SQLiteOpenHelper{
        private static final String DATABASSE_CREATE = "create table"
                + DATABASE_TABLE + " (" + KEY_ID
                + " integer primary key autoincrement, " + KEY_CONTENT
                + " text not null, " + KEY_DATE + " text);";

        public DiaryOpenHelper(Context c, String dbname, SQLiteDatabase.CursorFactory factory, int version) {
            super(c, dbname, factory, version);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASSE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
