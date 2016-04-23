package edu.illinois.ugl.minrvaestimote;

import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import edu.illinois.ugl.minrvaestimote.Network.DownloadItemAsyncTask;
import edu.illinois.ugl.minrvaestimote.Network.DownloadMapInfoAsyncTask;

public class MainActivity extends ActionBarActivity {

    private BeaconManager beaconManager;
    private Region region;
    private String bibId;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // allow for HTTP request to happen in main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        context = getApplicationContext();

        // get current version from server and update beacons if out of date
        int currVersion = getVersion("https://minrva-wayfinder.herokuapp.com/rest/v1.0/version");
        VersionDbHelper dbHelper= new VersionDbHelper(MainActivity.context);
        if (dbHelper.checkVersion(currVersion) == false) {
            downloadBeacons("https://minrva-wayfinder.herokuapp.com/rest/v1.0/beacons");
        }

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                bibId = null;
            } else {
                bibId = extras.getString("bibId");
            }
        } else {
            bibId = savedInstanceState.getString("bibId");
        }
        setContentView(R.layout.activity_main);

        getItemInfo();
        getSearchHistoryReady();
        getRecsReady();
        getMapZoomingReady();

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

                list = beaconDict.removeInvalidBeacons(list); // must call this before getCoords
                double[][] beaconCoords = beaconDict.getCoords();
                double[] userCoords = null;

                if (list.size() >= 2) {

                    double[] distances = new double[list.size()];
                    for (int i = 0; i < list.size(); i++)
                        distances[i] = Utils.computeAccuracy(list.get(i));

                    List closeList = positionRefiner.getThreeClosestBeacons(distances, beaconCoords);
                    distances = positionRefiner.metabeaconsToDistances(closeList);
                    beaconCoords = positionRefiner.metabeaconsToCoords(closeList);

                    try {
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
                    catch (Exception e)
                    {
                        Log.e("Minrva Wayfinder", e.getMessage());
                    }

                }
                else if (list.size() == 1) {
                    userCoords = beaconCoords[0];
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

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("bibId",bibId);
    }

    private void getItemInfo() {
        // set imageLoader config here
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(getApplicationContext());
        ImageLoader.getInstance().init(config.build());

        // call minrva api to get item info, update the view
        final TextView itemTitleTV = (TextView) findViewById(R.id.itemTitle);
        final ImageView itemThumbnailIV = (ImageView) findViewById(R.id.itemThumbnail);
        final TextView itemCallNumberTV = (TextView) findViewById(R.id.itemCallNumber);
        final TextView itemShelfNumberTV = (TextView) findViewById(R.id.itemShelfNumber);
        final LibraryMap libraryMapView = (LibraryMap) findViewById(R.id.displayCanvas);
        new DownloadItemAsyncTask(itemTitleTV, itemThumbnailIV, itemCallNumberTV).execute(bibId);
        new DownloadMapInfoAsyncTask(itemShelfNumberTV, libraryMapView).execute(bibId);
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
                final LibraryMap map = (LibraryMap) findViewById(R.id.displayCanvas);
                float[] userCoords = map.getUserCoordsInPixel();

                if ( userCoords != null ) {
                    Intent intent = new Intent(getApplicationContext(), RecsActivity.class);
                    intent.putExtra("userCoords", userCoords);
                    startActivity(intent);
                } else {
                    Toast.makeText(context, "Cannot fetch user coordinates.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getMapZoomingReady() {
        Button resetBtn = (Button) findViewById(R.id.zoomResetBtn);
        Button zoomInBtn = (Button) findViewById(R.id.zoomInBtn);
        Button zoomOutBtn = (Button) findViewById(R.id.zoomOutBtn);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reset the map to default size
                LibraryMap map = (LibraryMap) findViewById(R.id.displayCanvas);
                map.resetZoom();
            }
        });

        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reset the map to default size
                LibraryMap map = (LibraryMap) findViewById(R.id.displayCanvas);
                PointF currentFocus = map.getScrollPosition();
                map.setZoom(map.getCurrentZoom() * 1.1f, currentFocus.x, currentFocus.y);
            }
        });

        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reset the map to default size
                LibraryMap map = (LibraryMap) findViewById(R.id.displayCanvas);
                PointF currentFocus = map.getScrollPosition();
                map.setZoom(map.getCurrentZoom() / 1.1f, currentFocus.x, currentFocus.y);
            }
        });
    }

    public static void downloadBeacons(String url){
        InputStream response = null;
        String jsonString = "";
        try {

            trustAllHosts();
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(url).openConnection();
            urlConnection.setHostnameVerifier(DO_NOT_VERIFY);
            response = urlConnection.getInputStream();

            // convert HTTP response to a String
            jsonString = IOUtils.toString(response, "UTF-8");

            if(jsonString == null) {
                return;
            }

            JSONArray jsonBeacons = new JSONArray(jsonString);
            BeaconDbHelper dbHelper = new BeaconDbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            //dbHelper.onUpgrade(db, 0, 1); //TODO save version number to be able to skip this part
            String currUuid;
            int currMajor;
            int currMinor;
            double currX;
            double currY;
            double currZ;
            String currDesc;

            for (int i = 0; i < jsonBeacons.length(); i++) {
                JSONObject currBeacon = jsonBeacons.getJSONObject(i);
                currUuid = currBeacon.getString("uuid");
                currMajor = currBeacon.getInt("major");
                currMinor = currBeacon.getInt("minor");
                currX = currBeacon.getDouble("x");
                currY = currBeacon.getDouble("y");
                currZ = currBeacon.getDouble("z");
                currDesc = currBeacon.getString("description");

                // add to database
                dbHelper.insert(db, currUuid, currMajor, currMinor, currX, currY, currZ, currDesc);
            }
            Cursor result = db.rawQuery("SELECT * FROM beacons", null);
            result.moveToFirst();
            Log.d("Num entries", result.getCount() + "");
            result.close();

        } catch (Exception e) {
            Toast.makeText(context, "Server connection failed. Try again later.", Toast.LENGTH_SHORT).show();
            Log.d("InputStream", e.getLocalizedMessage());
        }
    }

    public static int getVersion(String url) {
        InputStream response = null;
        String jsonString = "";
        try {

            trustAllHosts();
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(url).openConnection();
            urlConnection.setHostnameVerifier(DO_NOT_VERIFY);
            response = urlConnection.getInputStream();

            // convert HTTP response to a String
            jsonString = IOUtils.toString(response, "UTF-8");

            if(jsonString == null) {
                return 0;
            }

            JSONObject jsonVersion = new JSONObject(jsonString);
            return jsonVersion.getInt("id");

        } catch (Exception e) {
            Toast.makeText(context, "Server connection failed. Try again later.", Toast.LENGTH_SHORT).show();
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return 0;
    }
    /**
     * Always verify the host and don't check the certificate
     */
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Trust every server - don't check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}