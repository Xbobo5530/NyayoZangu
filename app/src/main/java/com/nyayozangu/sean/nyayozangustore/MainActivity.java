package com.nyayozangu.sean.nyayozangustore;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_MESSAGE = "com.nyayozangu.sean.nyayozangustore.CREATE_ACC_URL";
    private WebView mWebView;
    private HomeFragment homeFragment;
    private MeFragment meFragment;
    private CartFragment cartFragment;
    private CollectionsFragment collectionsFragment;
    private AlertFragment alertFragment;
    private MoreFragment moreFragment;
    private ProgressBar mProgressBar;
    private BottomNavigationView navigation;
    private FloatingActionButton mFab;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;{
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            //on selecting bottom navigation item
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        if (isConnected()) {
                            setFragment(homeFragment, getString(R.string.store_home_url));
                            return true;
                        }
                        checkConnection();
                        return true;
                    case R.id.navigation_collections:
                        if (isConnected()) {
                            setFragment(collectionsFragment, getString(R.string.store_collections_url));
                            return true;
                        }
                        checkConnection();
                        return true;
                    case R.id.navigation_cart:
                        if (isConnected()) {
                            setFragment(cartFragment, getString(R.string.store_cart_url));
                            return true;
                        }
                        checkConnection();
                        return true;
                    case R.id.navigation_more:
                        if (isConnected()) {
                            setFragment(moreFragment, null);
                            return true;
                        }
                        checkConnection();
                        return true;
                }
                return false;
            }
        };
    }

    private void setFragment(Fragment fragment, String url) {
        //set fragment, and pass on the url to load
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
        if (url != null) {
            mWebView.loadUrl(url);
        }
        Log.i("Sean", "at setFragment, setting Fragment " + fragment.toString() +
                "\nloading mWebView, url is " + url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //constructing elements
        mWebView = findViewById(R.id.webView);
        mProgressBar = findViewById(R.id.progress_bar);
        mFab = findViewById(R.id.fab);

        //fragments
        homeFragment = new HomeFragment();
        collectionsFragment = new CollectionsFragment();
        meFragment = new MeFragment();
        cartFragment = new CartFragment();
        moreFragment = new MoreFragment();
        alertFragment = new AlertFragment();

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        showProgressBar(); //load page display the loading progress bar

        //create new webView and handle cache
        WebView webView = new WebView(getApplicationContext());
        handleCache(webView);

        //getting javascript settings
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        Log.i("Sean", "at onCreate(), handling webView settings");

        mWebView.setWebViewClient(new MyWebViewClient()); //use settings from MyWebViewClient


        if (isConnected()) {
            Intent intent = getIntent();
            //determine if MainActivity is launched from createAccount
            if (intent.getStringExtra(EXTRA_MESSAGE) == null) {
                proceed();
            } else {
                Log.i("Sean", "at onCreate, gettingStringExtra, extra message is "
                        + intent.getStringExtra(EXTRA_MESSAGE));
                String createAccUrl = intent.getStringExtra(EXTRA_MESSAGE);
                setFragment(meFragment, createAccUrl);
            }
        } else {
            checkConnection();
        }
        //opened from external links to deep links
        handleDeepLinkIntent(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLinkIntent(intent);
    }

    private void handleDeepLinkIntent(Intent intent) {
        Log.i("Sean", "at handleDeepLinkIntent");
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            String incomingUrl = String.valueOf(appLinkData);
            Log.i("Sean", "incomingUrl is " + incomingUrl);
            setFragment(homeFragment, incomingUrl);
        }
    }

    private void proceed() {
        setFragment(homeFragment, getString(R.string.store_home_url));
        Log.i("Sean", "at proceed(url)");
    }

    // TODO: 3/9/18 implement the onSavedInstance method to properly handle configuration changes
    // TODO: 3/9/18 implement onRestoreInstance methods to handle configuration changes

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void handleCache(WebView webView) {
        //noinspection deprecation
        webView.getSettings().setAppCacheMaxSize( 5 * 1024 * 1024 ); // 5MB
        webView.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
        webView.getSettings().setAllowFileAccess( true );
        webView.getSettings().setAppCacheEnabled( true );
        webView.getSettings().setJavaScriptEnabled( true );
        webView.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT ); // load online by default
    }

    private void showProgressBar() {
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
            if (isConnected()) {
                Log.i("Sean","at showProgressBar, showing progressBar");
                if (progress < 100 && mProgressBar.getVisibility() == ProgressBar.GONE) {
                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                    mWebView.setVisibility(View.INVISIBLE);
                }


                mProgressBar.setProgress(progress);
                if (progress > 40) {
                    Log.i("Sean", "at showProgressBar, progress is " + progress);
                    mProgressBar.setVisibility(ProgressBar.GONE);
                    mWebView.setVisibility(View.VISIBLE);
                }
            }else{
                mProgressBar.setVisibility(ProgressBar.GONE);
                mWebView.setVisibility(View.GONE);
                checkConnection();
            }
            }
        });
    }

    public void checkConnection() {
        Log.i("Sean", "at checkConnection");
        if (isConnected()){
            mFab.setVisibility(View.VISIBLE); //when connected show the fab
            if (mWebView.getUrl() == null){proceed();}
            else{
                navigation.setSelectedItemId(navigation.getSelectedItemId());
                mWebView.loadUrl(mWebView.getUrl());
            }
        } else {
            Log.i("Sean", "at checkConnection, disconnected");
            showAlertScreen();
        }
    }

    private boolean isConnected() {
        //check if there's a connection
        Context context = getApplicationContext();
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void showAlertScreen() {
        Log.i("Sean", "at showAlertScreen, alert message is ");
        mFab.setVisibility(View.INVISIBLE); //hide fab when alertScreen is visible
        setFragment(alertFragment, null);
        // TODO: 3/22/18 show the alert message

    }

    public void openAccount(View view) {
        if (isConnected()){
            setFragment(meFragment, getString(R.string.store_acc_url));
        }else{
            checkConnection();
        }
    }

    public void openProductRequests(View view) {
        if (isConnected()){
            setFragment(meFragment, getString(R.string.store_requests_url));
        }else{checkConnection();}
    }

    public void openBlog(View view) {
        if (isConnected()){
            setFragment(meFragment, getString(R.string.blog_url));
        }else{checkConnection();}
    }

    public void openContact(View view) {
        if (isConnected()) {
            setFragment(meFragment, getString(R.string.contact_url));
        }else{checkConnection();}
    }

    public void openAbout(View view) {
        if (isConnected()) {
            setFragment(meFragment, getString(R.string.about_url));
        }else{checkConnection();}
    }

    public void reConnect(View view) {
        rotateReconnect(view);
        checkConnection();//when the retry button is check the connection;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            if (!mWebView.getUrl().equals(getString(R.string.store_home_url))) {
                mWebView.goBack();
                return true;
            } else {
                finish();
            }//if at home url, pressing back exits the app
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    public void rotateReconnect(View view) {
        //animate rotation on the alert screen
        ImageView reconnectAnimation;
        reconnectAnimation = findViewById(R.id.reconnect_image_view);
        Animation startRotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_once);
        reconnectAnimation.startAnimation(startRotateAnimation);
    }

    public void openSearch(View view) {
        if (isConnected()) {
            setFragment(meFragment, getString(R.string.store_search_url));

        } else {
            checkConnection();
        }
    }

    public void openLiveChat(View view) {
        //opening liveChat
        setFragment(meFragment, getString(R.string.livechat_url));
    }

    private class MyWebViewClient extends WebViewClient {

        //manage navigation to outside links by creating a MyWebView class that extends the WebViewClient Class
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().contains(getString(R.string.nyayozangucom_url_search))) {
                Log.i("Sean", "at MyWebKit, shouldOverrideUrlLoading," +
                        "url is store, url is " + url);
                /*
                This is my web site, so do not override; let my WebView load the page
                updating the bottom navigation bar items
                 */
                if (url.equals(getString(R.string.store_home_url))) {
                    navigation.setSelectedItemId(R.id.navigation_home);
                    setFragment(homeFragment, url);
                } else if (url.contains("collections") || url.contains("products")) {
                    //switch the navigation item selected
                    Log.i("Sean", "at shouldOverrideUrlLoading. " +
                            "Url contains products / collections. Url is " + url);
                    navigation.setSelectedItemId(R.id.navigation_collections);
                    setFragment(collectionsFragment, url);
                } else if (url.contains("cart")) {
                    navigation.setSelectedItemId(R.id.navigation_cart);
                    setFragment(cartFragment, url);
                } else {
                    // TODO: 3/22/18 handle 'more' links within the app
                    setFragment(meFragment, url);
                }
                return false;
            }else if (url.startsWith(getString(R.string.tel_url_search))) {
                //Handle telephony Urls
                Log.i("Sean", "at shouldOverrideUrlLoading, Url is " + url);
                return true;
            }

            //if its any other link
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); //open url in browser
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();

            if (uri.toString().contains(getString(R.string.nyayozangucom_url_search)) ||
                    uri.toString().contains(getString(R.string.shopify_url_text))) {
                // This is my web site, so do not override; let my WebView load the page
                //update bottom navigation items
                if (uri.toString().equals(getString(R.string.store_home_url))) {
                    navigation.setSelectedItemId(R.id.navigation_home);
                    setFragment(homeFragment, uri.toString());
                } else if (uri.toString().contains("collections") || uri.toString().contains("products")) {
                    //switch the navigation item selected
                    Log.i("Sean", "at shouldOverrideUrlLoading. " +
                            "Url contains products / collections. Url is " + uri.toString());
                    navigation.setSelectedItemId(R.id.navigation_collections);
                    setFragment(collectionsFragment, uri.toString());
                } else if (uri.toString().contains("cart")) {
                    navigation.setSelectedItemId(R.id.navigation_cart);
                    setFragment(cartFragment, uri.toString());
                } else {
                    // TODO: 3/22/18 handle 'more' links within the app
                    setFragment(meFragment, uri.toString());
                }

                return false;
            } else if (uri.toString().startsWith(getString(R.string.tel_url_search))) {
                //Handle telephony Urls
                startActivity(new Intent(Intent.ACTION_DIAL, uri));
            }
            //Handle Web Urls
            startActivity(new Intent(Intent.ACTION_VIEW, uri)); //open url in browser
            return true;
        }

        //handle http errors
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Log.i("Sean", "at onReceivedHttpError, error is " + errorResponse);
            super.onReceivedHttpError(view, request, errorResponse);
            showAlertScreen();
        }
    }
}
