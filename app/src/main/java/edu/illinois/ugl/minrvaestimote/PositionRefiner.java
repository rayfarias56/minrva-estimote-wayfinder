package edu.illinois.ugl.minrvaestimote;


import android.util.Log;

/**
 * Created by Maxx on 2/19/2016.
 */
public class PositionRefiner {
    private boolean started;
    private long prevTime;
    private double[] prevCoords;

    public PositionRefiner()
    {
        started = false;
    }

    public double[] refinePosition(double[] userCoords)
    {
        if (!started)
        {
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
        double [] newCoords = new double[2];
        if (purportedSpeed <= 0.20)
        {
            // It is possible the user walked here fast enough, so the coords are valid
            newCoords = userCoords;
        }
        else
        {
            // It is unlikely the user walked here fast enough, so only move them a little bit.
            // Move the user slightly in the direction they were calculated to be in, just in case
            // they really are moving to that position.
            newCoords[0] = prevCoords[0] + (x * 0.08);
            newCoords[1] = prevCoords[1] + (y * 0.08);
        }

        prevTime = curTime;
        prevCoords = newCoords;
        System.out.println("newCoords " + newCoords[0] + " " + newCoords[1]);
        return newCoords;
    }
}
