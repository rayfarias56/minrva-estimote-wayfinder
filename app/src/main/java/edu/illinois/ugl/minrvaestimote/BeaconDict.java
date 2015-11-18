package edu.illinois.ugl.minrvaestimote;

import android.util.Log;

import com.estimote.sdk.Beacon;

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

    public BeaconDict() {
        this.beaconCoords = new HashMap<Integer, HashMap<Integer, double[]>>();
        addBeacon(55809, 65302, new double[]{5.0, 2.0});
        addBeacon(30816, 41832, new double[]{2.0, 5.0});
    }

    private void addBeacon(int major, int minor, double[] coords) {
        if (!this.beaconCoords.containsKey(major))
            this.beaconCoords.put(major, new HashMap<Integer, double[]>());

        this.beaconCoords.get(major).put(minor, coords);
    }

    public double[] getCoords(Beacon beacon) {
        if (!beacon.getProximityUUID().equals(MINRVA_UUID)) {
            Log.d("Minrva Wayfinder", "Non-Minrva beacon: " + beacon.toString());
            return null;
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

        return coords;
    }

    public double[][] getCoords(List<Beacon> beacons) {
        double[][] coords = new double[beacons.size()][];
        for (int i = 0; i < beacons.size(); i++)
            coords[i] = getCoords(beacons.get(i));

        return coords;
    }
}
