package edu.illinois.ugl.minrvaestimote;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Helper class that handles common functions for the local beacon database.
 */
public class BeaconDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "beacons.db";

    // SQL for creating the table
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BeaconEntry.TABLE_NAME + " (" +
                    BeaconEntry._ID + "  INTEGER," +
                    BeaconEntry.COLUMN_UUID + " VARCHAR(54) NOT NULL," +
                    BeaconEntry.COLUMN_MAJOR + " INT NOT NULL," +
                    BeaconEntry.COLUMN_MINOR + " INT NOT NULL," +
                    BeaconEntry.COLUMN_X + " REAL NOT NULL," +
                    BeaconEntry.COLUMN_Y + " REAL NOT NULL," +
                    BeaconEntry.COLUMN_Z + " REAL NOT NULL," +
                    BeaconEntry.COLUMN_DESC + " TEXT," +
                    "PRIMARY KEY (" + BeaconEntry.COLUMN_UUID + ", " + BeaconEntry.COLUMN_MAJOR + ", " + BeaconEntry.COLUMN_MINOR +
                    " ))";

    // SQL for deleting the table
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + BeaconEntry.TABLE_NAME;

    /**
     * Constructor for a BeaconDbHelper
     * @param context The context from the MainActivity
     */
    public BeaconDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the table in the given database
     * @param db The database that should contain the beacons table
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * Deletes the beacons table and recreates it.
     * @param db The database that contains the beacons table
     * @param oldVersion Not used, but required for overriding.
     * @param newVersion Not used, but required for overriding.
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Deletes the beacons table and recreates it.
     * @param db The database that contains the beacons table
     * @param oldVersion Not used, but required for overriding.
     * @param newVersion Not used, but required for overriding.
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Inserts the information of a beacon into the beacons table
     * @param db The database that contains the beacons table
     * @param uuid The UUID of the beacon to be added
     * @param major The major number of the beacon to be added
     * @param minor The minor number of the beacon to be added
     * @param x The X coordinate of the beacon to be added
     * @param y The Y coordinate of the beacon to be added
     * @param z The Z coordinate of the beacon to be added
     * @param desc The description of the beacon to be added
     */
    public void insert(SQLiteDatabase db, String uuid, int major, int minor, double x, double y, double z, String desc) {
        db.execSQL(
                "INSERT INTO " + BeaconEntry.TABLE_NAME + " (uuid, major, minor, x, y, z, description) " +
                        "VALUES (\"" + uuid + "\", " + major + ", " + minor + ", " + x + ", " + y + ", " + z + ", \"" + desc + "\")"
        );
    }

    /**
     * Retrieves all beacons from the local database as an array of BeaconObjects
     * @param db The database that contains the beacons table
     * @return A BeaconObject[] containing all beacons from the local database
     */
    public BeaconObject[] getBeacons(SQLiteDatabase db) {

        Cursor result = db.rawQuery("SELECT * FROM " + BeaconEntry.TABLE_NAME, null);
        result.moveToFirst();
        BeaconObject[] beacons = new BeaconObject[result.getCount()];

        int uuid_col = result.getColumnIndex("uuid");
        int major_col = result.getColumnIndex("major");
        int minor_col = result.getColumnIndex("minor");
        int x_col = result.getColumnIndex("x");
        int y_col = result.getColumnIndex("y");
        int z_col = result.getColumnIndex("z");
        int desc_col = result.getColumnIndex("description");

        for (int i = 0; i < result.getCount(); i++) {
            String uuid = result.getString(uuid_col);
            int major = result.getInt(major_col);
            int minor = result.getInt(minor_col);
            double x = result.getDouble(x_col);
            double y = result.getDouble(y_col);
            double z = result.getDouble(z_col);
            String desc = result.getString(desc_col);

            beacons[i] = new BeaconObject(uuid, major, minor, x, y, z, desc);
            result.moveToNext();
        }
        result.close();
        return beacons;
    }

    /* Inner class that defines the table contents */
    public static abstract class BeaconEntry implements BaseColumns {
        public static final String TABLE_NAME   = "Beacons";
        public static final String COLUMN_UUID  = "uuid";
        public static final String COLUMN_MAJOR = "major";
        public static final String COLUMN_MINOR = "minor";
        public static final String COLUMN_X     = "x";
        public static final String COLUMN_Y     = "y";
        public static final String COLUMN_Z     = "z";
        public static final String COLUMN_DESC  = "description";
    }
}