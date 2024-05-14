package com.malinduliyanage.wastu;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "wastuDB";
    private static final int DB_VERSION = 1;

    public SQLiteHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        getWritableDatabase();
    }

    public void onCreate(SQLiteDatabase db) {
        String captureQuery = "CREATE TABLE captures ("
                + "img_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "path TEXT,"
                + "timestamp TEXT,"
                + "lat TEXT,"
                + "lon TEXT)";

        String recognitionQuery = "CREATE TABLE recognitions ("
                + "rec_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "img_path TEXT,"
                + "path TEXT,"
                + "timestamp TEXT)";

        db.execSQL(captureQuery);
        db.execSQL(recognitionQuery);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS captures");
        db.execSQL("DROP TABLE IF EXISTS recognitions");
        onCreate(db);
    }

    public void addCapture(String path, String timestamp, String lat, String lon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("timestamp", timestamp);
        values.put("lat", lat);
        values.put("lon", lon);
        db.insert("captures", null, values);
        db.close();
    }

    public void addRecognition(String img_path, String path, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("img_path", img_path);
        values.put("path", path);
        values.put("timestamp", timestamp);
        db.insert("recognitions", null, values);
        db.close();
    }
}


