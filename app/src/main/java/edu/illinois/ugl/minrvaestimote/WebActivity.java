package edu.illinois.ugl.minrvaestimote;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

/**
 * Created by yierh on 2/12/16.
 */
public class WebActivity extends AppCompatActivity {

    private WebView webView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://minrvaproject.org/");
    }
}
