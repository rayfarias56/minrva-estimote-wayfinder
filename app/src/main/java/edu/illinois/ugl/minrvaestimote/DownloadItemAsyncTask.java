package edu.illinois.ugl.minrvaestimote;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

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

    public DownloadItemAsyncTask(TextView itemTitleTV, ImageView itemThumbnailIV ) {
        this.itemTitleTVRef = new WeakReference<>(itemTitleTV);
        this.itemThumbnailIVRef = new WeakReference<>(itemThumbnailIV);
    }

    @Override
    protected JSONObject doInBackground(String... bibId) {
        return GET(bibId[0]);
    }

    @Override
    protected void onPostExecute(JSONObject libraryItem) {
        // TODO maybe transform to a LibraryItem class?
        // Display on main view
        String itemTitle = null;
        String itemThumbnail = null;
        try {
            if (libraryItem != null) {
                itemTitle = libraryItem.getString("title");
                itemThumbnail = libraryItem.getString("thumbnail");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //TODO suggest reentering bibId if not found
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
        if (itemThumbnailIV != null && itemThumbnail != null) {
            //TODO download thumbnail image
        }

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

    private Bitmap downloadThumbnailBitmap(String thumbnailUrl) {
        //TODO download thumbnail from url, maybe use minrva sdk?
        return null;
    }
}
