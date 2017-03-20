package com.motopit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by abdul on 1/11/16.
 */

public class DBHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "MyDBName.db";

    public static final String TABLE_NAME = "vendor";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ACTIVEFLAG = "active";


    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table vendor " +
                        "(id integer primary key autoincrement,name text,latitude text, longitude text ,active text default yes)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //AR 01-11-16 @Rahman For Insert data to table
    public boolean insertVendor(String name,String latitude,String longitude, String flag)
    {
        SQLiteDatabase db= this.getWritableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("name", name);
            contentValues.put("latitude", latitude);
            contentValues.put("longitude", longitude);
            contentValues.put("active", flag);
            String selectQuery = "select * from vendor where name ='" + name + "'" ;
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.getCount() == 1) {

                db.update("vendor", contentValues, "name ='" + name + "'", null);
                return true;
            } else {

                db.insert("vendor", null, contentValues);
                return true;
            }
        }
        finally {

            db.close();
        }
    }

    //AR 01-11-16 @Rahman For getting all data from table
    public ArrayList<String> getAllDetails()
    {
        ArrayList<String> array_list = new ArrayList<String>();
        String latitude = "";
        String longitude = "";
        String name = "";
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from vendor where active = 'yes'", null );
        res.moveToFirst();
        try {
            while (res.isAfterLast() == false) {
                latitude = res.getString(res.getColumnIndex(COLUMN_LATITUDE));
                longitude = res.getString(res.getColumnIndex(COLUMN_LONGITUDE));
                name = res.getString(res.getColumnIndex(COLUMN_NAME));
                array_list.add(latitude+","+longitude+","+name);
                res.moveToNext();
            }
            return array_list;
        }
        finally {
            res.close();
            db.close();
        }
    }

    public boolean clearTable(String table_name){    //AR 01-11-16 @Rahman For clear table
        SQLiteDatabase db= this.getWritableDatabase();
        db.execSQL("Delete from "+table_name);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name ='"+table_name+"';");
        db.close();
        return true;
    }
}
