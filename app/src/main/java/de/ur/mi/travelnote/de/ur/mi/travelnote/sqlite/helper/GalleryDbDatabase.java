package de.ur.mi.travelnote.de.ur.mi.travelnote.sqlite.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by wexle on 18.09.2017.
 */

public class GalleryDbDatabase {

    private GalleryDbHelper galleryHelper;
    private SQLiteDatabase database;

    public static final String DB_NAME = "gallery_db";
    public static final int DB_VERSION = 7;

    public static final String IMAGE_TABLE = "my_image_table";
    public static final String IMAGE_ID = "_id";
    public static final String IMAGE_BODY = "image";


    public GalleryDbDatabase(Context context) {
        galleryHelper = new GalleryDbHelper(context, DB_NAME, null, DB_VERSION);
    }

    public void open() {
        database = galleryHelper.getReadableDatabase();
    }

    public void close() {
        database.close();
        galleryHelper.close();
    }

    public long insertNewObject(Bitmap image){
        ContentValues v = new ContentValues();
        v.put(IMAGE_BODY, bitmapToString(image));
        long newInsertId = database.insert(IMAGE_TABLE, null, v);
        return newInsertId;
    }

    public ArrayList<Bitmap> getAllMyImages() {
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        String[] allColumns = new String[]{IMAGE_ID, IMAGE_BODY};
        Cursor results = database.query(IMAGE_TABLE, allColumns, null, null, null, null, null);
        if (results.moveToFirst()) {
            do {
                Bitmap image = stringToBitmap(results.getString(1));
                images.add(image);
            } while (results.moveToNext());
        }
        return images;
    }

    public void clearDatabse(String TABLE_NAME) {
        String clearDBQuery = "DELETE FROM" + TABLE_NAME;
        database.execSQL(clearDBQuery);
    }

    public final static String bitmapToString(Bitmap in){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        in.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        return Base64.encodeToString(bytes.toByteArray(),Base64.DEFAULT);
    }
    public final static Bitmap stringToBitmap(String in){
        byte[] bytes = Base64.decode(in, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private class GalleryDbHelper extends SQLiteOpenHelper {

        private static final String CREATE_DB = "create table " + IMAGE_TABLE
                + " (" + IMAGE_ID + " integer primary key autoincrement, "
                + IMAGE_BODY + "text not null);";

        public GalleryDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase database) {
            database.execSQL(CREATE_DB);
        }

        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        }

    }



}
