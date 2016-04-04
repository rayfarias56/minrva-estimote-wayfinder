package edu.illinois.ugl.minrvaestimote;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.estimote.sdk.Beacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Ben on 11/11/2015.
 */
public class BeaconDict {

    // TODO Set all library beacons to a specific UUID and change this
    public static final UUID MINRVA_UUID = UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d");

    private final HashMap<Integer, HashMap<Integer, double[]>> beaconCoords;
    private List<double []> currentBeacons = new ArrayList<double []>();
    private int numCalculations = 0;

    public BeaconDict() {
        this.beaconCoords = new HashMap<Integer, HashMap<Integer, double[]>>();

        BeaconDbHelper dbHelper = new BeaconDbHelper(MainActivity.context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        BeaconObject[] dbBeacons = dbHelper.getBeacons(db);

        for (BeaconObject beacon : dbBeacons) {
            addBeacon(beacon.getMajor(), beacon.getMinor(), new double[]{625.12 * beacon.getY() - 312.56, 4674.28 - 623.24 * beacon.getX()});
        }

        // Temporarily hard-code the coordinates for testing
        /*
        addBeacon(30361, 13334, new double[]{2813.04, 4674.28});

        addBeacon(47997, 21952, new double[]{312.56, 4051.04});
        addBeacon(60183, 20478, new double[]{937.68, 4051.04});
        addBeacon(7639, 41966, new double[]{1562.8, 4051.04});
        addBeacon(13796, 61719, new double[]{2187.92, 4051.04});
        addBeacon(14774, 31826, new double[]{2813.04, 4051.04});
        addBeacon(54206, 19453, new double[]{3438.16, 4051.04});
        addBeacon(14481, 65459, new double[]{4063.28, 4051.04});
        addBeacon(38495, 57964, new double[]{4688.4, 4051.04});
        addBeacon(5017, 48174, new double[]{5313.52, 4051.04});

        addBeacon(12829, 8803, new double[]{312.46, 3427.8});
        addBeacon(13010, 39738, new double[]{937.68, 3427.8});
        addBeacon(25108, 62750, new double[]{1562.8, 3427.8});
        addBeacon(32380, 48923, new double[]{4063.28, 3427.8});
        addBeacon(21733, 54978, new double[]{4688.4, 3427.8});
        addBeacon(46896, 52218, new double[]{5313.52, 3427.8});

        addBeacon(29021, 25571, new double[]{312.46, 2804.26});
        addBeacon(27474, 11444, new double[]{937.68, 2804.26});
        addBeacon(1417, 33903, new double[]{1562.8, 2804.26});
        addBeacon(63145, 5680, new double[]{4063.28, 2804.26});
        addBeacon(24416, 18745, new double[]{4688.4, 2804.26});
        addBeacon(58253, 1071, new double[]{5313.52, 2804.26});

        addBeacon(37946, 36488, new double[]{312.46, 2181.32});
        addBeacon(49702, 24264, new double[]{937.68, 2181.32});
        addBeacon(11224, 10643, new double[]{1562.8, 2181.32});
        addBeacon(28382, 9286, new double[]{4063.8, 2181.32});
        addBeacon(60547, 13362, new double[]{4688.4, 2181.32});
        addBeacon(35677, 36394, new double[]{5313.52, 2181.32});

        addBeacon(6137, 18047, new double[]{312.46, 1558.08});
        addBeacon(54514, 7644, new double[]{937.68, 1558.08});
        addBeacon(11050, 38665, new double[]{1562.8, 1558.08});
        addBeacon(58808, 20974, new double[]{2187.92, 1558.08});
        addBeacon(2876, 53837, new double[]{2813.04, 1558.08});
        addBeacon(48337, 56555, new double[]{3438.16, 1558.08});
        addBeacon(43286, 22687, new double[]{4063.28, 1558.08});
        addBeacon(55809, 65302, new double[]{4688.4, 1558.08});
        addBeacon(63544, 26162, new double[]{5313.52, 1558.08});

        addBeacon(28904, 56891, new double[]{312.46, 934.84});
        addBeacon(29472, 24136, new double[]{937.68, 934.84});
        addBeacon(32367, 12044, new double[]{1562.8, 934.84});
        addBeacon(34959, 19644, new double[]{2187.92, 934.84});
        addBeacon(24494, 32441, new double[]{2813.04, 934.84});
        addBeacon(20796, 53124, new double[]{3438.16, 934.84});
        addBeacon(47448, 7663, new double[]{4063.28, 934.84});
        addBeacon(29403, 39034, new double[]{4688.4, 934.84});
        addBeacon(7702, 3760, new double[]{5313.52, 934.84});

        addBeacon(26610, 9252, new double[]{312.46, 311.6});
        addBeacon(40056, 28904, new double[]{937.68, 311.6});
        addBeacon(39247, 61002, new double[]{1562.8, 311.6});
        addBeacon(58691, 24320, new double[]{4063.28, 311.6});
        addBeacon(30816, 41832, new double[]{4688.4, 311.6});
        addBeacon(22900, 32356, new double[]{5313.52, 311.6});
        */
    }
    //TODO Make sure that z coordinates are also used
    private void addBeacon(int major, int minor, double[] coords) {
        if (!this.beaconCoords.containsKey(major))
            this.beaconCoords.put(major, new HashMap<Integer, double[]>());

        this.beaconCoords.get(major).put(minor, coords);
    }

    public double[] getCoords(Beacon beacon) {
        if (!beacon.getProximityUUID().equals(MINRVA_UUID)) {
            Log.d("Minrva Wayfinder", "Non-Minrva beacon: " + beacon.toString());
            return null; // This should never happen since Ranging feature only uses our UUID
        }

        HashMap<Integer, double[]> minorMap = this.beaconCoords.get(beacon.getMajor());
        if (minorMap == null) {
            Log.d("Minrva Wayfinder", "Unrecognized Major: " + beacon.toString());
            return null;
        }

        double[] coords = minorMap.get(beacon.getMinor());
        if (coords == null) {
            Log.d("Minrva Wayfinder", "Unrecognized Minor: " + beacon.toString());
            return null;
        }
        return new double[]{coords[0], coords[1]};
    }

    public List<Beacon> removeInvalidBeacons(List<Beacon> beacons)
    {
        currentBeacons = new ArrayList<double []>();
        List<Beacon> validBeacons = new ArrayList<Beacon>();
        for (int i = 0; i < beacons.size(); i++)
        {
            double[] coords = getCoords(beacons.get(i));
            if (coords != null) {
                validBeacons.add(beacons.get(i));
                currentBeacons.add(coords);
            }
        }
        return validBeacons;
    }

    public double[][] getCoords() {
        double[][] coords = currentBeacons.toArray(new double[currentBeacons.size()][]);
        return coords;
    }
}