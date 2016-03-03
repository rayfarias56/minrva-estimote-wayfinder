package edu.illinois.ugl.minrvaestimote.Network;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.ugl.minrvaestimote.ExtendedSimpleAdapter;
import edu.illinois.ugl.minrvaestimote.MainActivity;
import edu.illinois.ugl.minrvaestimote.R;
import edu.illinois.ugl.minrvaestimote.RecsActivity;
import edu.illinois.ugl.minrvaestimote.WebActivity;

/**
 * Created by yierh on 12/1/15.
 */
public class DownloadRecsAsyncTask extends AsyncTask<String, Void, JSONArray> {
    //static String recApiUrl = "http://minrva-dev.library.illinois.edu/api/recommend/popularnear?shelfNums=";
    static String recApiUrl = "http://minrva-dev.library.illinois.edu:8080/v8/recommend/popularnear?shelfNums=";
    private WeakReference<ListView> recBookLVRef;
    private WeakReference<ListView> recEBookLVRef;
    private WeakReference<ListView> recDatabaseLVRef;
    private Context displayContext;

    private List<Map<String, Object>> recBookList;
    private List<Map<String, Object>> recEbookList;
    private List<Map<String, Object>> recDatabaseList;

    private Map<String, Bitmap> thumbnailBitmaps;
    private ImageSize thumbnailSize = new ImageSize(70,100);

    public DownloadRecsAsyncTask(ListView recBookLV, ListView recEBookLV, ListView recDatabaseLV, Context context) {
        this.recBookLVRef = new WeakReference<>(recBookLV);
        this.recEBookLVRef = new WeakReference<>(recEBookLV);
        this.recDatabaseLVRef = new WeakReference<>(recDatabaseLV);
        this.displayContext = context;

        recBookList = new ArrayList<>();
        recEbookList = new ArrayList<>();
        recDatabaseList = new ArrayList<>();

        thumbnailBitmaps = new HashMap<>();
    }

    @Override
    protected JSONArray doInBackground(String... shelfNumber) {
        UrlDownloader urlDownloader = new UrlDownloader();
        String url = recApiUrl + shelfNumber[0];
        JSONArray popularItems = urlDownloader.getArray(url);

        JSONObject popularItem;
        String itemBibId;
        ImageLoader imageLoader = ImageLoader.getInstance();
        for (int i = 0; i < popularItems.length(); i++) {
            try {
                popularItem = popularItems.getJSONObject(i);
                if (popularItem != null){
                    itemBibId = popularItem.getString("bibId");
                    Bitmap thumbnail = imageLoader.loadImageSync(popularItem.getString("thumbnail"),thumbnailSize);

                    if (thumbnail != null && thumbnail.getHeight() > 1 && thumbnail.getWidth() > 1) {
                        thumbnailBitmaps.put(itemBibId, thumbnail);
                    }

                }
            } catch  (JSONException e) {
                e.printStackTrace();
            }
        }

        return popularItems;
    }

    @Override
    protected void onPostExecute(JSONArray popularItems) {
        // Extract item information
        JSONObject popularItem;
        String itemFormat;
        String itemTitle;
        String itemAuthor;
        String itemShelfNumber;
        String itemBibId;
        Bitmap itemThumbnail;

        if (popularItems == null) {
            return;
        }

        for (int i = 0; i < popularItems.length(); i++) {
            try {
                popularItem = popularItems.getJSONObject(i);
                if (popularItem != null) {
                    itemFormat = popularItem.getString("format");

                    if (itemFormat.equals("Book")) {
                        itemBibId = popularItem.getString("bibId");
                        itemTitle = popularItem.getString("title");
                        itemAuthor = popularItem.getString("author");
                        itemShelfNumber = "000";
                        itemThumbnail = thumbnailBitmaps.get(itemBibId);

                        Map<String, Object> map = new HashMap<>();
                        map.put("recsBibId", itemBibId);
                        map.put("recsTitle", itemTitle);
                        map.put("recsAuthor", "Author: " + itemAuthor);

                        if (itemThumbnail != null) {
                            map.put("recsThumbnail", itemThumbnail);
                        } else {
                            map.put("recsThumbnail", R.drawable.icon_default_book);
                        }

                        map.put("recsShelfNumber", "Shelf Number: " + itemShelfNumber);

                        recBookList.add(map);

                    } else if (itemFormat.equals("eBook")) {
                        itemBibId = popularItem.getString("bibId");
                        itemTitle = popularItem.getString("title");
                        itemAuthor = popularItem.getString("author");
                        itemThumbnail = thumbnailBitmaps.get(itemBibId);

                        Map<String, Object> map = new HashMap<>();
                        map.put("recsBibId", itemBibId);
                        map.put("recsTitle", itemTitle);
                        map.put("recsAuthor", "Author: " + itemAuthor);

                        if (itemThumbnail != null) {
                            map.put("recsThumbnail", itemThumbnail);
                        } else {
                            map.put("recsThumbnail", R.drawable.icon_default_ebook);
                        }

                        recEbookList.add(map);

                    } else if (itemFormat.equals("Database")) {
                        itemBibId = popularItem.getString("bibId");
                        itemTitle = popularItem.getString("title");
                        itemAuthor = popularItem.getString("author");
                        itemThumbnail = thumbnailBitmaps.get(itemBibId);

                        Map<String, Object> map = new HashMap<>();
                        map.put("recsBibId", itemBibId);
                        map.put("recsTitle", itemTitle);
                        map.put("recsAuthor", "Author: " + itemAuthor);

                        if (itemThumbnail != null) {
                            map.put("recsThumbnail", itemThumbnail);
                        } else {
                            map.put("recsThumbnail", R.drawable.icon_default_database);
                        }

                        recDatabaseList.add(map);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //load contents into the lists
        ListView bookListView = recBookLVRef.get();
        ExtendedSimpleAdapter bookAdapter = new ExtendedSimpleAdapter(displayContext, recBookList, R.layout.recs_list_item_book,
                new String[]{"recsTitle", "recsAuthor", "recsThumbnail", "recsShelfNumber"},
                new int[]{R.id.recsTitle, R.id.recsAuthor, R.id.recsThumbnail, R.id.recsShelfNumber});
        if (bookAdapter != null) {
            bookListView.setAdapter(bookAdapter);
            bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // new activity guiding to the new item
                    String recsBibId = recBookList.get(position).get("recsBibId").toString();
                    if (recsBibId != null) {
                        Intent intent = new Intent(displayContext, MainActivity.class);
                        intent.putExtra("bibId", recsBibId);
                        displayContext.startActivity(intent);
                    }
                }
            });
        }

        ListView ebookListView = recEBookLVRef.get();
        SimpleAdapter ebookAdapter = new SimpleAdapter(displayContext, recEbookList, R.layout.recs_list_item_ebook_db,
                new String[]{"recsTitle", "recsAuthor", "recsThumbnail"},
                new int[]{R.id.recsTitle, R.id.recsAuthor, R.id.recsThumbnail});
        if (ebookAdapter != null) {
            ebookListView.setAdapter(ebookAdapter);
            ebookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    //open up web page within app
                    Intent intent = new Intent(displayContext, WebActivity.class);
                    displayContext.startActivity(intent);
                }
            });
        }

        ListView dbListView = recDatabaseLVRef.get();
        SimpleAdapter dbAdapter = new SimpleAdapter(displayContext, recDatabaseList, R.layout.recs_list_item_ebook_db,
                new String[]{"recsTitle", "recsAuthor", "recsThumbnail"},
                new int[]{R.id.recsTitle, R.id.recsAuthor, R.id.recsThumbnail});
        if (dbAdapter != null) {
            dbListView.setAdapter(dbAdapter);
            dbListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    //open up web page within app
                    Intent intent = new Intent(displayContext, WebActivity.class);
                    displayContext.startActivity(intent);
                }
            });
        }

    }
}
