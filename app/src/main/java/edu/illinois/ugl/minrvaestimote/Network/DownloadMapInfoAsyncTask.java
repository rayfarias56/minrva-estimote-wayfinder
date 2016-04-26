package edu.illinois.ugl.minrvaestimote.Network;

import android.graphics.Point;
import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import edu.illinois.ugl.minrvaestimote.LibraryMap;

/**
 * Download item's coordinates and shelf number and update front-end asynchronously
 */
public class DownloadMapInfoAsyncTask extends AsyncTask<String, Void, JSONObject> {
    static String mapApiUrl = "http://minrva-dev.library.illinois.edu:8080/api/wayfinder/map_data/uiu_undergrad/";
    //static String mapImageApiUrl = "http://minrva-dev.library.illinois.edu:8080/api/wayfinder/map/1_undergrad.png?";//x=459&y=339
    private WeakReference<TextView> itemShelfNumberTVRef;
    private WeakReference<LibraryMap> libraryMapRef;

    public DownloadMapInfoAsyncTask(TextView itemShelfNumberTV, LibraryMap libraryMapView) {
        this.itemShelfNumberTVRef = new WeakReference<>(itemShelfNumberTV);
        this.libraryMapRef = new WeakReference<>(libraryMapView);
    }

    @Override
    protected JSONObject doInBackground(String... bibId) {
        UrlDownloader urlDownloader = new UrlDownloader();
        return urlDownloader.getObject(mapApiUrl + bibId[0]);
    }

    @Override
    protected void onPostExecute(JSONObject itemMapInfo) {
        // Extract item information
        String itemShelfNumber = null;
        Point itemCoordinate = null;

        try {
            //Example returned object by map_data api
            //{"call_num":"D810.W7 W43 1990","map_name":"1_undergrad.png",
            // "x":"459","y":"339","shelf_number":"7","author":"Weatherford, Doris. ",
            // "title":"American women and World War II "}
            if (itemMapInfo != null) {
                itemShelfNumber = itemMapInfo.getString("shelf_number");
                itemCoordinate = new Point(itemMapInfo.getInt("x"), itemMapInfo.getInt("y"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView itemShelfNumberTV = itemShelfNumberTVRef.get();
        if (itemShelfNumberTV != null) {
            if (itemShelfNumber != null && !itemShelfNumber.equalsIgnoreCase("")) {
                itemShelfNumberTV.setText("Shelf Number: " + itemShelfNumber);
            }
        }

        LibraryMap libraryMapView = libraryMapRef.get();
        if (libraryMapView != null) {
            if (itemCoordinate != null) {
                libraryMapView.updateItemCoords(itemCoordinate);
            }


        }
    }

}
