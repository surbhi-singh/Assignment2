package com.group31.mc.assignment2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ak on 10/4/17.
 */


public class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
    String TABLE_NAME;
    String COLUMN_NAMES[] = new String[4];
    String P_TIME;
    String X;
    String Y;
    String Z;


    public CustomSQLiteOpenHelper(Context context, String dbName, int dbVersion, String col1, String col2, String col3, String col4) {
        super(context, dbName, null, dbVersion);
        P_TIME = col1;
        X = col2;
        Y = col3;
        Z = col4;



    }
    // This method only runs the first time the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
// Create a table for photos and all their details
        String newTableQueryString = "create table Name_ID_Age_Sex"
                + " ("
                + P_TIME
                + " TIMESTAMP PRIMARY KEY NOT NULL, "
                + X
                + " INTEGER not null, "
                + Y
                + " INTEGER not null, "
                + Z
                + " INTEGER not null" + ");";
        Log.i("create query", newTableQueryString);
        db.execSQL(newTableQueryString);
    }
    // This method only runs when we increment DB_VERSION
// We will look at this in chapter 26
    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
    }
}
