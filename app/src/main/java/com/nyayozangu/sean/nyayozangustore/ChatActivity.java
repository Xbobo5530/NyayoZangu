package com.nyayozangu.sean.nyayozangustore;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.jivosite.sdk.JivoDelegate;
import com.jivosite.sdk.JivoSdk;

import java.util.Locale;

public class ChatActivity extends Activity implements JivoDelegate {

    private static final String TAG = "Sean";
    //initialize variables
    JivoSdk jivoSdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        //initialize sdk
        String lang = Locale.getDefault().getLanguage().contains("ru") ? "ru" : "en";
        jivoSdk = new JivoSdk((WebView) findViewById(R.id.chat_webview), lang);
        jivoSdk.delegate = this;
        jivoSdk.prepare();

    }

    @Override
    public boolean onNavigateUp() {
        Log.d(TAG, "at onNavigateUp");
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
        return true;
    }

    @Override
    public void onEvent(String name, String data) {
        Log.d(TAG, "at onEvent:\nname is: " +
                name + "\ndata is: " + data);
        if (name.equals("url.click")) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
            startActivity(browserIntent);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //go to MainActivity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}
