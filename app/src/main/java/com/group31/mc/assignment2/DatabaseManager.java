package com.group31.mc.assignment2;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by ak on 10/4/17.
 */


public class DatabaseManager {
    private SQLiteDatabase db;
    public static final String TABLE_TIMESTAMP = "Timestamp";
    public static final String TABLE_X = "xValue";
    public static final String TABLE_Y = "yValue";
    public static final String TABLE_Z = "zValue";
    public static final String DATABASE_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DATABASE_NAME = "group32.db";

    public String newTableQueryString = "create table IF NOT EXISTS Name_ID_Age_Sex "
            + " ("
            + TABLE_TIMESTAMP
            + " TIMESTAMP PRIMARY KEY NOT NULL, "
            + TABLE_X
            + " INTEGER not null, "
            + TABLE_Y
            + " INTEGER not null, "
            + TABLE_Z
            + " INTEGER not null" + ");";
//        Log.i("create query", newTableQueryString);

//    public DatabaseManager(Context context) {
//        File dbDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/Data/CSE535_ASSIGNMENT2");
//        dbDirectory.mkdirs();
//
//        CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context, dbDirectory.getAbsolutePath()+"/"+DATABASE_NAME, 1,TABLE_TIMESTAMP, TABLE_X, TABLE_Y, TABLE_Z );
//        db = helper.getWritableDatabase();
//
//    }

    public DatabaseManager(Context context)
    {
        try
        {
            Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            File dbDirectory = context.getDatabasePath(DATABASE_NAME);
            String NAME = "";

            if(isSDPresent)
            {
                Log.i("inmanager","hi");
                dbDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/Data/CSE535_ASSIGNMENT2");
                dbDirectory.mkdirs();

                NAME = dbDirectory.getAbsolutePath()+File.separator+DATABASE_NAME;
                Log.i("inmanager",NAME);

            }
            else
            {
                NAME = dbDirectory.getAbsolutePath();

            }
            Log.i("Database location", NAME);

            db = SQLiteDatabase.openDatabase(NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);

        }
        catch (SQLiteException ex)
        {
            Log.e(TAG, "error -- " + ex.getMessage(), ex);
            // error means tables does not exits
        }


    }
    public void createTable() {
        db.execSQL(newTableQueryString);
    }
    public Cursor returnLastTenSecondsData(){

        Long tsLong = System.currentTimeMillis()/1000 - 10;
        String ts = tsLong.toString();
        String query = "SELECT * FROM Name_ID_Age_Sex WHERE Timestamp >= '" +ts + "';";
        Cursor returnCursor = db.rawQuery(query,null);

//        Log.i(returnCursor.getString(1),returnCursor.getString(2));
//                  " " + returnCursor.getString(3));
        return returnCursor;
    }

    public boolean insertIntoTable(float x, float y, float z){

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        String query = "INSERT INTO Name_ID_Age_Sex (Timestamp, xValue, yValue, zValue) VALUES('" + ts + "'," + x + "," + y + "," + z +");";
        Log.i("insert query",query);
        db.execSQL(query);
        return true;
    }
}
