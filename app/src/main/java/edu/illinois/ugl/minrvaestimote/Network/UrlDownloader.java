package edu.illinois.ugl.minrvaestimote.Network;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yierh on 1/26/16.
 */
public class UrlDownloader {

    public static JSONObject getObject(String urlString) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        URL url;
        JSONObject responseObject;

        try {
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

    public static JSONArray getArray(String urlString) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        URL url;
        JSONArray responseArray;

        try {
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
            responseArray = (JSONArray) new JSONArray(response);

            return responseArray;
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
