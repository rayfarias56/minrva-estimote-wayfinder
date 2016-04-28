package edu.illinois.ugl.minrvaestimote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Maxx on 2/19/2016.
 */
public class PositionRefiner {
    private boolean started;
    private long prevTime;
    private double[] prevCoords;

    public PositionRefiner() {
        started = false;
    }

    /**
     * Adjusts the user's position if it changed too much from the previous position.
     * @param userCoords The user's library coordinates in cm from trilateration.
     * @return The user coords after being adjusted if necessary.
     */
    public double[] refinePosition(double[] userCoords) {
        if (!started) {
            prevTime = System.currentTimeMillis();
            prevCoords = userCoords;
            started = true;
            return userCoords;
        }

        long curTime = System.currentTimeMillis();
        long elapsedTime = curTime - prevTime;
        if (elapsedTime == 0) elapsedTime++; // Just to prevent crashes from dividing by zero

        double x = userCoords[0] - prevCoords[0];
        double y = userCoords[1] - prevCoords[1];
        double distanceTraveled = Math.abs(x) + Math.abs(y); // Use Manhattan distance for now

        // Now check if user could have actually traveled that distance in that time.
        // Average walking speed according to Wikipedia is 1.4 m/s or 0.14 cm/ms.
        // We will use a higher threshold to be safe.
        double purportedSpeed = distanceTraveled / elapsedTime;
        double[] newCoords = new double[2];
        if (purportedSpeed <= 0.30) {
            // It is possible the user walked here fast enough, so the coords are valid.
            newCoords = userCoords;
        } else {
            // It is unlikely the user walked here fast enough, so only move them a little bit.
            // Move the user slightly in the direction they were calculated to be in, just in case
            // they really are moving to that position.
            newCoords[0] = prevCoords[0] + (x * 0.20);
            newCoords[1] = prevCoords[1] + (y * 0.20);
        }

        prevTime = curTime;
        prevCoords = newCoords;
        return newCoords;
    }

    /**
     * Finds at most the 3 closest beacons to improve trilateration accuracy.
     * @param distances The distances between the user and beacons.
     * @param coords The coordinates of the beacons.
     * @return The three (or fewer) closest beacons to the user.
     */
    public List getThreeClosestBeacons(double[] distances, double[][] coords)
    {
        List<Metabeacon> list = new ArrayList<Metabeacon>();
        for (int i = 0; i < distances.length; i++)
        {
            list.add(new Metabeacon(distances[i], coords[i][0], coords[i][1]));
        }
        Collections.sort(list);
        System.out.println("List value after sort: "+ list);
        return list;
    }

    /**
     * Converts the list of Metabeacons to an array of their distances.
     * @param list A list of Metabeacons.
     * @return An array of the distances corresponding to the input Metabeacons.
     */
    public double[] metabeaconsToDistances(List<Metabeacon> list)
    {
        int max = Math.min(3, list.size());
        double[] distances = new double[max];
        for (int i = 0; i < max; i++)
            distances[i] = list.get(i).distance;
        return distances;
    }

    /**
     * Converts the list of Metabeacons to an array of their coordinates.
     * @param list A list of Metabeacons.
     * @return An array of the coordinates corresponding to the input Metabeacons.
     */
    public double[][] metabeaconsToCoords(List<Metabeacon> list)
    {
        int max = Math.min(3, list.size());
        double[][] coords = new double[max][2];
        for (int i = 0; i < max; i++)
        {
            coords[i][0] = list.get(i).x;
            coords[i][1] = list.get(i).y;
        }
        return coords;
    }

    /**
     * Simple class used to sort beacons to find closest ones.
     */
    private class Metabeacon implements Comparable<Metabeacon>
    {
        public double distance;
        public double x;
        public double y;

        Metabeacon(double distance, double x, double y)
        {
            this.distance = distance;
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Metabeacon other) {
            if (this.distance < other.distance)
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
    }
}