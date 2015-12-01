package edu.illinois.ugl.minrvaestimote;

import com.estimote.sdk.Utils;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.util.List;
import java.util.UUID;

public class MainActivity extends ActionBarActivity {

    private BeaconManager beaconManager;
    private Region region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // http://developer.android.com/guide/topics/connectivity/bluetooth-le.html
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported on your device.", Toast.LENGTH_SHORT).show();
            finish();
        }
        else Toast.makeText(this, "BLE is supported on your device.", Toast.LENGTH_SHORT).show();

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.d("Minrva Wayfinder", "Bluetooth not available");
            new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_no_bluetooth)
                .setMessage(R.string.dialog_message_no_bluetooth)
                .create().show();
        }
        else if (!adapter.isEnabled()) {
            Log.d("Minrva Wayfinder", "Bluetooth not enabled.");
            new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_bluetooth_disabled)
                .setMessage(R.string.dialog_message_bluetooth_disabled)
                .setNegativeButton(R.string.dialog_button_bluetooth_disabled_neg,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                .setPositiveButton(R.string.dialog_button_bluetooth_disabled_pos,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // TODO Turn Bluetooth off when the app closes?
                            adapter.enable();
                            dialog.cancel();
                        }
                    })
                .create().show();
        }

        final LibraryMap map = (LibraryMap) findViewById(R.id.displayCanvas);
        final BeaconDict beaconDict = new BeaconDict();
        final GridMap gridmap = new GridMap();

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                Log.d("Minrva Wayfinder", "NumBeacons was " + list.size());
                for (int i = 0; i < list.size(); i++) {
                    Log.d("Minrva Wayfinder", "Beacon " + i + ": " + list.get(i).toString());
                }

                double[][] beaconCoords = beaconDict.getCoords(list);
                double[] userCoords = null;

                if (list.size() >= 3) {

                    double[] distances = new double[list.size()];
                    for (int i = 0; i < list.size(); i++)
                        distances[i] = Utils.computeAccuracy(list.get(i));

                    // TODO new TF can throw exceptions, maybe try to catch them
                    TrilaterationFunction tf = new TrilaterationFunction(beaconCoords, distances);
                    NonLinearLeastSquaresSolver solver =
                            new NonLinearLeastSquaresSolver(tf, new LevenbergMarquardtOptimizer());
                    Optimum optimum = solver.solve();
                    userCoords = optimum.getPoint().toArray();
                    if (!gridmap.isInLegalCell(userCoords[0], userCoords[1]))
                        userCoords = gridmap.getClosestLegalCoords(userCoords[0], userCoords[1]);
                }

                map.updateLocations(userCoords, beaconCoords);

            }
        });

        region = new Region("ranged region", BeaconDict.MINRVA_UUID, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
