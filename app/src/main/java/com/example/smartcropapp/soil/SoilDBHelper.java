package com.example.smartcropapp.soil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Soil DB helper saves soil records including URIs for soil photo and leaf photo.
 */
public class SoilDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "soil_advisory.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE = "soil_records";

    public SoilDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "timestamp INTEGER, " +
                        "location TEXT, " +
                        "soil_type TEXT, " +
                        "soil_photo_uri TEXT, " +
                        "leaf_photo_uri TEXT, " +
                        "leaf_color TEXT, " + // "yellow" or "green" or ""
                        "recommendation TEXT" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long saveRecord(long ts, String location, String soilType, String soilPhotoUri,
                           String leafPhotoUri, String leafColor, String recommendation) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("timestamp", ts);
        cv.put("location", location);
        cv.put("soil_type", soilType);
        cv.put("soil_photo_uri", soilPhotoUri == null ? "" : soilPhotoUri);
        cv.put("leaf_photo_uri", leafPhotoUri == null ? "" : leafPhotoUri);
        cv.put("leaf_color", leafColor == null ? "" : leafColor);
        cv.put("recommendation", recommendation);
        long id = db.insert(TABLE, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE + " ORDER BY timestamp DESC", null);
    }
}
