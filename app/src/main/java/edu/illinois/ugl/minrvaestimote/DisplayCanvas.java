package edu.illinois.ugl.minrvaestimote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by Maxx on 10/28/2015.
 */
public class DisplayCanvas extends View {

    private Bitmap libraryMap;
    private Paint[] beaconPaints;
    private Paint radiusPaint;
    private Paint textPaint;
    private Paint userPaint;
    private float[] userCoords;
    private float[][] beacons;

    public DisplayCanvas(Context context, AttributeSet attSet) {
        super(context, attSet);

        libraryMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ugl_map);

        userCoords = new float[2];
        userCoords[0] = 0; // TODO default coords? Don't display until trilateration performed?
        userCoords[1] = 0;

        beaconPaints = new Paint[3];
        beaconPaints[0] = new Paint();
        beaconPaints[0].setColor(Color.GREEN);
        beaconPaints[1] = new Paint();
        beaconPaints[1].setColor(Color.MAGENTA);
        beaconPaints[2] = new Paint();
        beaconPaints[2].setColor(Color.RED);
        radiusPaint = new Paint();
        radiusPaint.setStyle(Paint.Style.STROKE);
        radiusPaint.setStrokeWidth(5);
        userPaint = new Paint();
        userPaint.setColor(Color.BLUE);
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);

        beacons = new float[3][2];
        beacons[0][0] = 0;
        beacons[0][1] = 0;
        beacons[1][0] = 1;
        beacons[1][1] = 3.4f;
        beacons[2][0] = 2.5f;
        beacons[2][1] = 0;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawBitmap(libraryMap, 0, 0, null);
        for (int i = 0; i < beacons.length; i++) {
            float x = beacons[i][0]*200;
            float y = beacons[i][1]*200;
            canvas.drawCircle(x, y, 25, beaconPaints[i]);
            radiusPaint.setColor(beaconPaints[i].getColor());
            canvas.drawCircle(x, y, 250, radiusPaint); // TODO update the radius dynamically?
            canvas.drawText("(" + x + ", " + y + ")", x + 25, y + 25, textPaint);
        }
        canvas.drawCircle(userCoords[0], userCoords[1], 25, userPaint);
        canvas.drawText("(" + userCoords[0] + ", " + userCoords[1] + ")",
                userCoords[0] + 25, userCoords[1] + 25, textPaint);
    }

    public void updateLocation(double[] newCoords) {
            userCoords[0] = (float) newCoords[0]*200; // TODO *200 for now, create better mapping
            userCoords[1] = (float) newCoords[1]*200;
            System.out.println("*************** " + newCoords[0] +
                    ", " + newCoords[1] + " ***********");
            invalidate();
    }
}
