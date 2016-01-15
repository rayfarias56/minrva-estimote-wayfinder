package edu.illinois.ugl.minrvaestimote;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_recs);

        //TODO change to async task, use api
        loadListContent();
    }

    private void loadListContent() {
        //load contents into the list
        ListView listView = (ListView) this.findViewById(R.id.bookRecsList);
        SimpleAdapter adapter = new SimpleAdapter(this,getRecsData(),R.layout.recs_list_item,
                new String[]{"recsTitle","recsAuthor","recsThumbnail","recsShelfNumber"},
                new int[]{R.id.recsTitle,R.id.recsAuthor,R.id.recsThumbnail,R.id.recsShelfNumber});
        if (adapter != null) {
            listView.setAdapter(adapter);
        }

        //TODO load database info
    }

    private List<Map<String, Object>> getRecsData() {
        //temp test data
        List<Map<String, Object>> historyList = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("recsTitle", "Item 1");
        map.put("recsAuthor", "Name of the author");
        map.put("recsThumbnail", R.drawable.test);
        map.put("recsShelfNumber", "A11");
        historyList.add(map);

        map = new HashMap<>();
        map.put("recsTitle", "Item 2");
        map.put("recsAuthor", "Name of the author");
        map.put("recsThumbnail", R.drawable.test);
        map.put("recsShelfNumber", "A12");
        historyList.add(map);

        return historyList;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
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
