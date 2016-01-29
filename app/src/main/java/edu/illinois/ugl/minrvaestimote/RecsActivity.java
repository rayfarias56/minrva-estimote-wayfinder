package edu.illinois.ugl.minrvaestimote;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import edu.illinois.ugl.minrvaestimote.Network.DownloadRecsAsyncTask;

public class RecsActivity extends AppCompatActivity {

    private String shelfNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                shelfNumber = null;
            } else {
                shelfNumber = extras.getString("shelfNumber");
            }
        } else {
            shelfNumber = (String) savedInstanceState.getSerializable("shelfNumber");
        }
        setContentView(R.layout.activity_recs);

        // load list contents and update the view
        final ListView bookListView = (ListView) this.findViewById(R.id.bookRecsList);
        final ListView ebookListView = (ListView) this.findViewById(R.id.ebookRecsList);
        final ListView dbListView = (ListView) this.findViewById(R.id.dbRecsList);
        new DownloadRecsAsyncTask(bookListView, ebookListView, dbListView, this).execute(shelfNumber);
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
