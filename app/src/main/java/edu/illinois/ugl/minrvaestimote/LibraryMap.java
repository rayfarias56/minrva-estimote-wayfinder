package edu.illinois.ugl.minrvaestimote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;


/**
 * Created by Maxx on 10/28/2015.
 */
public class LibraryMap extends TouchImageView {

    private boolean DRAW_BEACONS = true;
    private boolean DRAW_RANDOM_USER_COORDS = false;

    // Origin with regard to the coordinates of the beacons
    // (ORIGIN * MAP_DIMS) is the location of the origin on the map in pixels
    // e.g. (0,0) is the top left corner and (1,1) is the bottom right
    public double[] ORIGIN = {0.0, 0.0};

    // Real-world dimensions of the map in meters
    private static final float[] MAP_DIMS = new float[]{56.717f, 57.143f};

    // Image dimensions of the map in pixels
    private static final float[] MAP_DIMS_IMG = new float[]{550f, 550f};

    // TODO Don't display until trilateration performed?
    private double[] UNK_COORDS = new double[0];
    private double[] userCoords = UNK_COORDS;

    // Coordinates of the discovered beacons in meters
    private double[][] beaconsCoords = new double[0][];

    // Coordinates of the item in pixels
    private PointF itemCoords = new PointF();

    private Drawable[] userDots;
    private Paint beaconPaint;
    private Paint radiusPaint;
    private Paint itemPaint;

    private float[] zoomMatrix;
    private float screenDensity;

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

        itemPaint = new Paint();
        itemPaint.setColor(Color.BLUE);

        zoomMatrix = new float[9];

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenDensity = metrics.density;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        updateZooming(canvas);

        if (userCoords != UNK_COORDS) {
            float[] userMapCoords = translateCoords(this.userCoords);
            Drawable userDot = userDots[6];
            userDot.setBounds(getDotBounds(userDot, userMapCoords));
            userDot.draw(canvas);
        }

        if (DRAW_BEACONS)
            drawBeacons(canvas);

        if (itemCoords != null && (itemCoords.x != 0 || itemCoords.y != 0)) {
            canvas.drawCircle(itemCoords.x, itemCoords.y, 15, itemPaint);
        }
        
    }

    private void drawBeacons(Canvas canvas) {
        for (double[] beaconCoords : this.beaconsCoords) {
            float[] beaconMapCoords = translateCoords(beaconCoords);
            canvas.drawCircle(beaconMapCoords[0], beaconMapCoords[1], 25, beaconPaint);
            //canvas.drawCircle(beaconMapCoords[0], beaconMapCoords[1], 250, radiusPaint);
        }
    }

    public void updateLocations(double[] userCoords, double[][] beaconsCoords) {
        if (userCoords != null)
            this.userCoords = userCoords;
        if (DRAW_RANDOM_USER_COORDS)
            this.userCoords = new double[]{Math.random() * 5671, Math.random() * 5084};
        this.beaconsCoords = beaconsCoords;
        invalidate();
    }

    /**
     * @param coords Coordinates from the origin in centimeters
     * @return (x,y) coordinates from the top left corner of the canvas in pixels
     */
    private float[] translateCoords(double[] coords) {
        double[] coordsInMeters = new double[2];
        coordsInMeters[0] = coords[0] / 100;
        coordsInMeters[1] = coords[1] / 100;
        //int[] canvasDims = {this.getWidth(), this.getHeight()};
        float[] canvasDims = {MAP_DIMS_IMG[0] * screenDensity, MAP_DIMS_IMG[1] * screenDensity};
        float[] translated = new float[coords.length];

        for (int i = 0; i < coords.length; i++)
            translated[i] = (float) (coordsInMeters[i] + ORIGIN[i]) * canvasDims[i] / MAP_DIMS[i];

        return translated;
    }

    private Rect getDotBounds(Drawable userDot, float[] userMapCoords) {
        int x = (int) userMapCoords[0];
        int y = (int) userMapCoords[1];
        int width = userDot.getIntrinsicWidth() / 2;
        int height = userDot.getIntrinsicHeight() / 2;
        return new Rect(x-width, y-height, x+width, y+height);
    }

    public void updateItemCoords(Point rawItemCoords) {
        if (rawItemCoords != null) {
            PointF translatedCoords = new PointF();

            //translatedCoords.x = rawItemCoords.x / MAP_DIMS_IMG[0] * getWidth();
            //translatedCoords.y = rawItemCoords.y / MAP_DIMS_IMG[1] * getHeight();

            translatedCoords.x = rawItemCoords.x * screenDensity;
            translatedCoords.y = rawItemCoords.y * screenDensity;

            this.itemCoords = translatedCoords;
        }
        invalidate();
    }

    private void updateZooming(Canvas canvas) {
        Matrix a = getMapMatrix();
        a.getValues(zoomMatrix);

        float transX = zoomMatrix[Matrix.MTRANS_X];
        float transY = zoomMatrix[Matrix.MTRANS_Y];
        canvas.translate(transX, transY);

        float scaleX = zoomMatrix[Matrix.MSCALE_X];
        float scaleY = zoomMatrix[Matrix.MSCALE_Y];
        canvas.scale(scaleX, scaleY);
    }

    @Override
    public Matrix getMapMatrix() {
        return super.getMapMatrix();
    }


    /**
     * @return (x,y) coordinates of the user from the top left corner of the canvas in pixels
     */
    public float[] getUserCoordsInPixel() {
        if (this.userCoords != UNK_COORDS) {
            return translateCoords(this.userCoords);
        } else {
            return null;
        }
    }
}
