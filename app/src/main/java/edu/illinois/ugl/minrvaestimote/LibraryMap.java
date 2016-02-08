package edu.illinois.ugl.minrvaestimote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.DrawableMarginSpan;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * Created by Maxx on 10/28/2015.
 */
public class LibraryMap extends ImageView {

    private boolean DRAW_BEACONS = true;

    // Origin with regard to the coordinates of the beacons
    // (ORIGIN * MAP_DIMS) is the location of the origin on the map in pixels
    // e.g. (0,0) is the top left corner and (1,1) is the bottom right
    public double[] ORIGIN = {0.0, 0.0};

    // Real-world dimensions of the map in meters
    private static final float[] MAP_DIMS = new float[]{56.244f, 55.9f};

    // TODO Don't display until trilateration performed?
    private double[] UNK_COORDS = new double[0];
    private double[] userCoords = UNK_COORDS;

    // Coordinates of the discovered beacons in meters
    private double[][] beaconsCoords = new double[0][];

    private Drawable[] userDots;
    private Paint beaconPaint;
    private Paint radiusPaint;

    public LibraryMap(Context context, AttributeSet attSet) {
        super(context, attSet);

        int[] dotResources = {
                R.drawable.walking_dot_01,
                R.drawable.walking_dot_02,
                R.drawable.walking_dot_03,
                R.drawable.walking_dot_04,
                R.drawable.walking_dot_05,
                R.drawable.walking_dot_06,
                R.drawable.walking_dot_07,
        };

        userDots = new Drawable[dotResources.length];
        for (int i = 0; i < dotResources.length; i++)
            userDots[i] = context.getResources().getDrawable(dotResources[i]);

        beaconPaint = new Paint();
        beaconPaint.setColor(Color.RED);

        radiusPaint = new Paint();
        beaconPaint.setColor(Color.RED);
        radiusPaint.setStyle(Paint.Style.STROKE);
        radiusPaint.setStrokeWidth(5);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (userCoords != UNK_COORDS) {
            float[] userMapCoords = translateCoords(this.userCoords);
            Drawable userDot = userDots[0];
            userDot.setBounds(getDotBounds(userDot, userMapCoords));
            userDot.draw(canvas);
        }

        if (DRAW_BEACONS)
            drawBeacons(canvas);
    }

    private void drawBeacons(Canvas canvas) {
        for (double[] beaconCoords : this.beaconsCoords) {
            // TODO update the radius dynamically?
            float[] beaconMapCoords = translateCoords(beaconCoords);
            canvas.drawCircle(beaconMapCoords[0], beaconMapCoords[1], 25, beaconPaint);
            canvas.drawCircle(beaconMapCoords[0], beaconMapCoords[1], 250, radiusPaint);
        }
    }

    public void updateLocations(double[] userCoords, double[][] beaconsCoords) {
        if (userCoords != null)
            this.userCoords = userCoords;
        this.beaconsCoords = beaconsCoords;
        invalidate();
    }

    /**
     * @param coords Coordinates from the origin in meters
     * @return (x,y) coordinates from the top left corner of the canvas in pixels
     */
    private float[] translateCoords(double[] coords) {
        int[] canvasDims = {this.getWidth(), this.getHeight()};
        float[] translated = new float[coords.length];
        for (int i = 0; i < coords.length; i++)
            translated[i] = (float) (coords[i] + ORIGIN[i]) * canvasDims[i] / MAP_DIMS[i];

        return translated;
    }

    private Rect getDotBounds(Drawable userDot, float[] userMapCoords) {
        int x = (int) userMapCoords[0];
        int y = (int) userMapCoords[1];
        int width = userDot.getIntrinsicWidth() / 2;
        int height = userDot.getIntrinsicHeight() / 2;
        return new Rect(x-width, y-height, x+width, y+height);
    }
}
