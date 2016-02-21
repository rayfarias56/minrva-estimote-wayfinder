package edu.illinois.ugl.minrvaestimote;

import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.ugl.minrvaestimote.Network.DownloadItemAsyncTask;

public class MainActivity extends ActionBarActivity {

    private BeaconManager beaconManager;
    private Region region;
    private String bibId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                bibId = null;
            } else {
                bibId = extras.getString("bibId");
            }
        } else {
            bibId = (String) savedInstanceState.getSerializable("bibId");
        }
        setContentView(R.layout.activity_main);

        getItemInfo();
        getSearchHistoryReady();
        getRecsReady();

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
        final PositionRefiner positionRefiner = new PositionRefiner();

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
                    userCoords = positionRefiner.refinePosition(userCoords);
                    if (!gridmap.isInLegalCell(userCoords[0], userCoords[1])) {
                        userCoords = gridmap.getClosestLegalCoords(userCoords[0], userCoords[1]);
                    }
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

    private void getItemInfo() {
        // set imageLoader config here
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(getApplicationContext());
        ImageLoader.getInstance().init(config.build());

        // call minrva api to get item info, update the view
        final TextView itemTitleTV = (TextView) findViewById(R.id.itemTitle);
        final ImageView itemThumbnailIV = (ImageView) findViewById(R.id.itemThumbnail);
        final TextView itemCallNumberTV = (TextView) findViewById(R.id.itemCallNumber);
        new DownloadItemAsyncTask(itemTitleTV, itemThumbnailIV, itemCallNumberTV).execute(bibId);
    }

    private void getSearchHistoryReady() {
        Button searchHistoryBtn = (Button) findViewById(R.id.searchHistoryBtn);

        searchHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initHistoryPopWindow();
            }
        });
    }

    private void initHistoryPopWindow() {
        View contentView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.history_list_popup, null);
        PopupWindow popupWindow = new PopupWindow(findViewById(R.id.mainLayout), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(contentView);
        //TODO dim the background activity

        //load contents into the list
        ListView listView = (ListView) contentView.findViewById(R.id.historyItemList);
        SimpleAdapter adapter = new SimpleAdapter(this,getHistoryData(),R.layout.history_list_item,
                new String[]{"historyTitle","historyInfo","historyThumbnail"},
                new int[]{R.id.historyTitle,R.id.historyInfo,R.id.historyThumbnail});

        if (adapter != null) {
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    //TODO get the bibId from search history
                    String historyBibId = "uiu_3359738";
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("bibId", historyBibId);
                    startActivity(intent);
                }
            });

        }

        popupWindow.showAtLocation(findViewById(R.id.mainLayout), Gravity.CENTER, 0, 0);
    }

    private List<Map<String, Object>> getHistoryData() {
        //TODO implement cache and load search history
        List<Map<String, Object>> historyList = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("historyTitle", "Book 1");
        map.put("historyInfo", "uiuc undergraduate library 1");
        map.put("historyThumbnail", R.drawable.test);
        historyList.add(map);

        map = new HashMap<>();
        map.put("historyTitle", "Book 2");
        map.put("historyInfo", "uiuc undergraduate library 2");
        map.put("historyThumbnail", R.drawable.test);
        historyList.add(map);

        map = new HashMap<>();
        map.put("historyTitle", "Book 3");
        map.put("historyInfo", "uiuc undergraduate library 3");
        map.put("historyThumbnail", R.drawable.test);
        historyList.add(map);

        return historyList;
    }

    private void getRecsReady() {
        Button recsBtn = (Button) findViewById(R.id.itemRecsBtn);

        recsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO get the shelf number near user
                String shelfNumber = "15";

                if ( !shelfNumber.equals("") ) {
                    Intent intent = new Intent(getApplicationContext(), RecsActivity.class);
                    intent.putExtra("shelfNumber", shelfNumber);
                    startActivity(intent);
                }
            }
        });
    }
}