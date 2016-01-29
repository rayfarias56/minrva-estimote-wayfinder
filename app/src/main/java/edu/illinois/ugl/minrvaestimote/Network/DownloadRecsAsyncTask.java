package edu.illinois.ugl.minrvaestimote.Network;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.ugl.minrvaestimote.R;

/**
 * Created by yierh on 12/1/15.
 */
public class DownloadRecsAsyncTask extends AsyncTask<String, Void, JSONArray> {
    static String recApiUrl = "http://minrva-dev.library.illinois.edu/api/recommend/popularnear?shelfNums=";
    private WeakReference<ListView> recBookLVRef;
    private WeakReference<ListView> recEBookLVRef;
    private WeakReference<ListView> recDatabaseLVRef;
    private Context displayContext;

    private List<Map<String, Object>> recBookList;
    private List<Map<String, Object>> recEbookList;
    private List<Map<String, Object>> recDatabaseList;

    public DownloadRecsAsyncTask(ListView recBookLV, ListView recEBookLV, ListView recDatabaseLV, Context context) {
        this.recBookLVRef = new WeakReference<>(recBookLV);
        this.recEBookLVRef = new WeakReference<>(recEBookLV);
        this.recDatabaseLVRef = new WeakReference<>(recDatabaseLV);
        this.displayContext = context;

        recBookList = new ArrayList<>();
        recEbookList = new ArrayList<>();
        recDatabaseList = new ArrayList<>();
    }

    @Override
    protected JSONArray doInBackground(String... shelfNumber) {
        UrlDownloader urlDownloader = new UrlDownloader();
        String url = recApiUrl + shelfNumber[0];
        return urlDownloader.getArray(url);
    }

    @Override
    protected void onPostExecute(JSONArray popularItems) {
        // Extract item information
        JSONObject popularItem;
        String itemFormat;
        String itemTitle;
        String itemThumbnailUrl;
        String itemAuthor;
        String itemShelfNumber;

        ImageLoader imageLoader = ImageLoader.getInstance();

        if (popularItems == null) {
            return;
        }

        for (int i = 0; i < popularItems.length(); i++) {
            try {
                popularItem = popularItems.getJSONObject(i);
                if (popularItem != null) {
                    itemFormat = popularItem.getString("format");

                    if (itemFormat.equals("Book")) {
                        itemTitle = popularItem.getString("title");
                        itemThumbnailUrl = popularItem.getString("thumbnail");
                        itemAuthor = popularItem.getString("author");
                        itemShelfNumber = "000";

                        Map<String, Object> map = new HashMap<>();
                        map.put("recsTitle", itemTitle);
                        map.put("recsAuthor", "Author: " + itemAuthor);
                        //map.put("recsThumbnail", imageLoader.loadImageSync(itemThumbnailUrl));
                        map.put("recsThumbnail", R.drawable.test);
                        map.put("recsShelfNumber", "Shelf Number: " + itemShelfNumber);

                        recBookList.add(map);

                    } else if (itemFormat.equals("eBook")) {
                        itemTitle = popularItem.getString("title");
                        itemThumbnailUrl = popularItem.getString("thumbnail");
                        itemAuthor = popularItem.getString("author");

                        Map<String, Object> map = new HashMap<>();
                        map.put("recsTitle", itemTitle);
                        map.put("recsAuthor", "Author: " + itemAuthor);
                        //map.put("recsThumbnail", imageLoader.loadImageSync(itemThumbnailUrl));
                        map.put("recsThumbnail", R.drawable.test);

                        recEbookList.add(map);

                    } else if (itemFormat.equals("Database")) {
                        itemTitle = popularItem.getString("title");
                        itemThumbnailUrl = popularItem.getString("thumbnail");
                        itemAuthor = popularItem.getString("author");

                        Map<String, Object> map = new HashMap<>();
                        map.put("recsTitle", itemTitle);
                        map.put("recsAuthor", "Author: " + itemAuthor);
                        map.put("recsThumbnail", R.drawable.test);

                        recDatabaseList.add(map);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //load contents into the lists
        ListView bookListView = recBookLVRef.get();
        SimpleAdapter bookAdapter = new SimpleAdapter(displayContext, recBookList, R.layout.recs_list_item_book,
                new String[]{"recsTitle", "recsAuthor", "recsThumbnail", "recsShelfNumber"},
                new int[]{R.id.recsTitle, R.id.recsAuthor, R.id.recsThumbnail, R.id.recsShelfNumber});
        if (bookAdapter != null) {
            bookListView.setAdapter(bookAdapter);
        }

        ListView ebookListView = recEBookLVRef.get();
        SimpleAdapter ebookAdapter = new SimpleAdapter(displayContext, recEbookList, R.layout.recs_list_item_ebook_db,
                new String[]{"recsTitle", "recsAuthor", "recsThumbnail"},
                new int[]{R.id.recsTitle, R.id.recsAuthor, R.id.recsThumbnail});
        if (ebookAdapter != null) {
            ebookListView.setAdapter(ebookAdapter);
        }

        ListView dbListView = recDatabaseLVRef.get();
        SimpleAdapter dbAdapter = new SimpleAdapter(displayContext, recDatabaseList, R.layout.recs_list_item_ebook_db,
                new String[]{"recsTitle", "recsAuthor", "recsThumbnail"},
                new int[]{R.id.recsTitle, R.id.recsAuthor, R.id.recsThumbnail});
        if (dbAdapter != null) {
            dbListView.setAdapter(dbAdapter);
        }

    }

}
