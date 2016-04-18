package edu.illinois.ugl.minrvaestimote;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


import java.sql.SQLClientInfoException;

public class VersionDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "beacons.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + VersionEntry.TABLE_NAME + " (" +
                    VersionEntry.COLUMN_ID + " BIGINT PRIMARY KEY)";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + VersionEntry.TABLE_NAME;

    public VersionDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insert(SQLiteDatabase db, int id) {
        db.execSQL(
                "INSERT INTO " + VersionEntry.TABLE_NAME + " (id) " +
                        "VALUES (" + id + ")"
        );
    }

    /**
     *
     * @param version The version received from the server.
     * @return True if the version matches. False otherwise.
     */
    public boolean checkVersion(int version) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + VersionEntry.TABLE_NAME, null);

        // Set new version number if empty result
        if (cursor.getCount() < 1) {
            insert(db, version);
            cursor.close();
            return false;
        }

        cursor.moveToFirst();

        if (version == cursor.getInt(cursor.getColumnIndex("id"))) {
            cursor.close();
            return true;
        }

        cursor.close();
        return false;
    }

    /* Inner class that defines the table contents */
    public static abstract class VersionEntry implements BaseColumns {
        public static final String TABLE_NAME   = "Versions";
        public static final String COLUMN_ID  = "id";
    }
}