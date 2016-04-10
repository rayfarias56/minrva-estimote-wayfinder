package edu.illinois.ugl.minrvaestimote;


import android.util.Log;

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
        System.out.println("elapsedTime is " + elapsedTime);

        double x = userCoords[0] - prevCoords[0];
        double y = userCoords[1] - prevCoords[1];
        double distanceTraveled = Math.abs(x) + Math.abs(y); // Use Manhattan distance for now
        System.out.println("Distance is " + distanceTraveled + ", x " + x + ", y " + y);

        // Now check if user could have actually traveled that distance in that time
        // Average walking speed according to Wikipedia is 1.4 m/s or 0.14 cm/ms
        // So we will use 2.0 as our cutoff for now
        double purportedSpeed = distanceTraveled / elapsedTime;
        System.out.println("speed " + purportedSpeed);
        System.out.println("prevCoords " + prevCoords[0] + " " + prevCoords[1]);
        double[] newCoords = new double[2];
        if (purportedSpeed <= 0.30) {
            // It is possible the user walked here fast enough, so the coords are valid
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
        System.out.println("newCoords " + newCoords[0] + " " + newCoords[1]);
        return newCoords;
    }

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

    public double[] metabeaconsToDistances(List<Metabeacon> list)
    {
        int num = Math.min(3, list.size());
        double[] distances = new double[num];
        for (int i = 0; i < num; i++)
            distances[i] = list.get(i).distance;
        return distances;
    }

    public double[][] metabeaconsToCoords(List<Metabeacon> list)
    {
        int num = Math.min(3, list.size());
        double[][] coords = new double[num][2];
        for (int i = 0; i < num; i++)
        {
            coords[i][0] = list.get(i).x;
            coords[i][1] = list.get(i).y;
        }
        return coords;
    }

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