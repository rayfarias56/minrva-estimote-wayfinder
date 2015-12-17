package edu.illinois.ugl.minrvaestimote;

import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yierh on 12/1/15.
 */
public class DownloadItemAsyncTask extends AsyncTask<String, Void, JSONObject> {
    static String displayApiUrl = "http://minrva-dev.library.illinois.edu:8080/api/display/";
    private WeakReference<TextView> itemTitleTVRef;
    private WeakReference<ImageView> itemThumbnailIVRef;
    private WeakReference<TextView> itemCallNumberTVRef;

    public DownloadItemAsyncTask(TextView itemTitleTV, ImageView itemThumbnailIV, TextView itemCallNumberTV) {
        this.itemTitleTVRef = new WeakReference<>(itemTitleTV);
        this.itemThumbnailIVRef = new WeakReference<>(itemThumbnailIV);
        this.itemCallNumberTVRef = new WeakReference<>(itemCallNumberTV);
    }

    @Override
    protected JSONObject doInBackground(String... bibId) {
        return GET(bibId[0]);
    }

    @Override
    protected void onPostExecute(JSONObject libraryItem) {
        // TODO maybe transform to a LibraryItem class?
        // Extract item information
        String itemTitle = null;
        String itemThumbnailUrl = null;
        String itemCallNumber = null;
        try {
            if (libraryItem != null) {
                itemTitle = libraryItem.getString("title");
                itemThumbnailUrl = libraryItem.getString("thumbnail");
                JSONArray callnumsArray= libraryItem.getJSONArray("callnums");
                itemCallNumber = callnumsArray.getString(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //TODO suggest reentering bibId if not found
        // Display on main view
        TextView itemTitleTV = itemTitleTVRef.get();
        if (itemTitleTV != null) {
            if (itemTitle != null && !itemTitle.equalsIgnoreCase("")) {
                itemTitleTV.setText(itemTitle);
            } else {
                itemTitleTV.setText("Item Not Found");
            }
        }
        else {
            // the view has been destroyed
        }

        ImageView itemThumbnailIV = itemThumbnailIVRef.get();
        if (itemThumbnailIV != null && itemThumbnailUrl != null) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(itemThumbnailUrl, itemThumbnailIV);
        }

        TextView itemCallNumberTV = itemCallNumberTVRef.get();
        if (itemCallNumberTV != null) {
            if (itemCallNumber != null && !itemCallNumber.equalsIgnoreCase("")) {
                itemCallNumberTV.setText("Call Number:" + itemCallNumber);
            }
        }

        //TODO find out shelf number

    }

    private static JSONObject GET(String bibId){
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        URL url;
        JSONObject responseObject;

        try {
            String urlString = displayApiUrl + bibId;
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String response = "";
            while ((line = bufferedReader.readLine()) != null)
                response += line;
            bufferedReader.close();
            inputStream.close();
            urlConnection.disconnect();
            responseObject = (JSONObject) new JSONTokener(response).nextValue();

            return responseObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    //ignored
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

}
