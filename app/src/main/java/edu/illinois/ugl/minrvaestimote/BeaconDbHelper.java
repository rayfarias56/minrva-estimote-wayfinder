package edu.illinois.ugl.minrvaestimote;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.sql.SQLClientInfoException;

public class BeaconDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "beacons.db";

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

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + BeaconEntry.TABLE_NAME;

    public BeaconDbHelper(Context context) {
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

    public void insert(SQLiteDatabase db, String uuid, int major, int minor, double x, double y, double z, String desc) {
        db.execSQL(
                "INSERT INTO " + BeaconEntry.TABLE_NAME + " (uuid, major, minor, x, y, z, description) " +
                        "VALUES (\"" + uuid + "\", " + major + ", " + minor + ", " + x + ", " + y + ", " + z + ", \"" + desc + "\")"
        );
    }

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
            Log.d("Beacon", beacons[i].toString());
            result.moveToNext();
        }
        result.close();
        return beacons;
    }

    /* Inner class that defines the table contents */
    public static abstract class BeaconEntry implements BaseColumns {
        public static final String TABLE_NAME   = "beacons";
        public static final String COLUMN_UUID  = "uuid";
        public static final String COLUMN_MAJOR = "major";
        public static final String COLUMN_MINOR = "minor";
        public static final String COLUMN_X     = "x";
        public static final String COLUMN_Y     = "y";
        public static final String COLUMN_Z     = "z";
        public static final String COLUMN_DESC  = "description";
    }
}