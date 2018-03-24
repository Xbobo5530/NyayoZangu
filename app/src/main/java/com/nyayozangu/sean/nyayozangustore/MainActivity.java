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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String EXTRA_MESSAGE = "com.nyayozangu.sean.nyayozangustore.CREATE_ACC_URL";
    private static final String TAG = "Sean";

    private FirebaseAnalytics mFirebaseAnalytics;

    private WebView mWebView;
    private FrameLayout mMainFrame;
    private RelativeLayout mFabBackground;
    private ProgressBar mProgressBar;
    private BottomNavigationView navigation;
    private FloatingActionButton mFab, mShareFab, mSearchFab;

    private HomeFragment homeFragment;
    private MeFragment meFragment;
    private CartFragment cartFragment;
    private CollectionsFragment collectionsFragment;
    private AlertFragment alertFragment;
    private MoreFragment moreFragment;

    private Boolean isFabOpen;

    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    {
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
        Log.i(TAG, "at setFragment, setting Fragment " + fragment.toString() +
                "\nloading mWebView, url is " + url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //subscribe to app updates
        FirebaseMessaging.getInstance().subscribeToTopic("UPDATES");
        Log.d(TAG, "user subscribed to topic UPDATES");

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // TODO: 3/23/18 finish setting up analytics

        //constructing elements
        mWebView = findViewById(R.id.webView);
        mProgressBar = findViewById(R.id.progress_bar);
        mMainFrame = findViewById(R.id.main_frame);
        mFabBackground = findViewById(R.id.fab_background);

        mFab = findViewById(R.id.fab);
        mShareFab = findViewById(R.id.share_fab);
        mSearchFab = findViewById(R.id.search_fab);
        isFabOpen = false;

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        mFab.setOnClickListener(this);
        mShareFab.setOnClickListener(this);
        mSearchFab.setOnClickListener(this);

        //construct fragments
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
        Log.i(TAG, "at onCreate(), handling webView settings");

        mWebView.setWebViewClient(new MyWebViewClient()); //use settings from MyWebViewClient

        if (isConnected()) {
            Log.d(TAG, "at onCreate, checking connection\n" +
                    "isConnected is: " + isConnected());
            onNewIntent(getIntent());
            //determine if MainActivity is launched from createAccount
            Intent intent = getIntent();
            if (intent.getStringExtra(EXTRA_MESSAGE) == null) {
                /*
                //opened from external links to deep links
                handleDeepLinkIntent(getIntent());
                //handle notifications
                handleNotifications(getIntent());
                */
                proceed();
            } else {
                Log.i(TAG, "at onCreate, gettingStringExtra, extra message is "
                        + intent.getStringExtra(EXTRA_MESSAGE));
                String createAccUrl = intent.getStringExtra(EXTRA_MESSAGE);
                setFragment(meFragment, createAccUrl);
            }
        } else {
            checkConnection();
        }
    }

    private void handleNotifications(Intent intent) {
        Log.d(TAG, "at handleNotifications");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(TAG, "extras != null\n extras: " + extras);
            if (extras.containsKey("targetUrl")) {
                //extract the notification data(targetUrl)
                String targetUrl = extras.getString("targetUrl");
                Log.d(TAG, "targetUrl: " + targetUrl);
                //set fragment and launch webView
                setFragment(meFragment, targetUrl);
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLinkIntent(intent);
        handleNotifications(intent);

    }

    private void handleDeepLinkIntent(Intent intent) {
        Log.i(TAG, "at handleDeepLinkIntent");
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            String incomingUrl = String.valueOf(appLinkData);
            Log.i(TAG, "incomingUrl is " + incomingUrl);
            setFragment(collectionsFragment, incomingUrl);
        }
    }

    private void proceed() {
        setFragment(homeFragment, getString(R.string.store_home_url));
        Log.i(TAG, "at proceed(url)");
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
        webView.getSettings().setAppCacheMaxSize(5 * 1024 * 1024); // 5MB
        webView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); // load online by default
    }

    private void showProgressBar() {
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (isConnected()) {
                    Log.i(TAG, "at showProgressBar, showing progressBar");
                    if (progress < 100 && mProgressBar.getVisibility() == ProgressBar.GONE) {
                        mProgressBar.setVisibility(ProgressBar.VISIBLE);
                        mWebView.setVisibility(View.INVISIBLE);
                    }


                    mProgressBar.setProgress(progress);
                    if (progress > 40) {
                        Log.i(TAG, "at showProgressBar, progress is " + progress);
                        mProgressBar.setVisibility(ProgressBar.GONE);
                        mWebView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mProgressBar.setVisibility(ProgressBar.GONE);
                    mWebView.setVisibility(View.GONE);
                    checkConnection();
                }
            }
        });
    }

    public void checkConnection() {
        Log.i(TAG, "at checkConnection");
        if (isConnected()) {
            mFab.setVisibility(View.VISIBLE); //when connected show the fab
            if (mWebView.getUrl() == null) {
                proceed();
            } else {
                navigation.setSelectedItemId(navigation.getSelectedItemId());
                mWebView.loadUrl(mWebView.getUrl());
            }
        } else {
            Log.i(TAG, "at checkConnection, disconnected");
            showAlertScreen();
        }
    }

    private boolean isConnected() {
        //check if there's a connection
        Context context = getApplicationContext();
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void showAlertScreen() {
        Log.i(TAG, "at showAlertScreen, alert message is ");
        mFab.setVisibility(View.INVISIBLE); //hide fab when alertScreen is visible
        setFragment(alertFragment, null);
        // TODO: 3/22/18 show the alert message
    }

    public void openAccount(View view) {
        if (isConnected()) {
            setFragment(meFragment, getString(R.string.store_acc_url));
        } else {
            checkConnection();
        }
    }

    public void openProductRequests(View view) {
        if (isConnected()) {
            setFragment(meFragment, getString(R.string.store_requests_url));
        } else {
            checkConnection();
        }
    }

    public void openBlog(View view) {
        if (isConnected()) {
            setFragment(meFragment, getString(R.string.blog_url));
        } else {
            checkConnection();
        }
    }

    public void openContact(View view) {
        if (isConnected()) {
            setFragment(meFragment, getString(R.string.contact_url));
        } else {
            checkConnection();
        }
    }

    public void openAbout(View view) {
        if (isConnected()) {
            setFragment(meFragment, getString(R.string.about_url));
        } else {
            checkConnection();
        }
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

    // TODO: 3/22/18 add liveChat
    /*public void openLiveChat(View view) {
        //opening liveChat
        setFragment(meFragment, getString(R.string.livechat_url));
    }*/

    //for FAP animation
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.share_fab:
                animateFAB();
                shareCurrPage();
                Log.d(TAG, "shareFab");
                break;
            case R.id.search_fab:
                //open search
                animateFAB();
                setFragment(meFragment, getString(R.string.store_search_url));
                Log.d(TAG, "searchFab");
                break;
        }
    }

    private void shareCurrPage() {
        try {
            String urlToShare = mWebView.getOriginalUrl();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, urlToShare);
            startActivity(Intent.createChooser(shareIntent, "Share this page with"));
        } catch (Exception e) {
            Log.i(TAG, "at shareCurrPage, error is " + e.getMessage());
        }
    }

    public void animateFAB() {

        if (isFabOpen) {

            mFab.startAnimation(rotate_backward);
            mShareFab.startAnimation(fab_close);
            mSearchFab.startAnimation(fab_close);
            mShareFab.setClickable(false);
            mSearchFab.setClickable(false);
            isFabOpen = false;
            mFabBackground.setClickable(false);
            Log.d(TAG, "close");

        } else {

            mFab.startAnimation(rotate_forward);
            mShareFab.startAnimation(fab_open);
            mSearchFab.startAnimation(fab_open);
            mShareFab.setClickable(true);
            mSearchFab.setClickable(true);
            isFabOpen = true;
            mFabBackground.setClickable(true);
            mFabBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    animateFAB();
                }
            });
            Log.d(TAG, "open");

        }
    }

    private class MyWebViewClient extends WebViewClient {

        //manage navigation to outside links by creating a MyWebView class that extends the WebViewClient Class
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().contains(getString(R.string.nyayozangucom_url_search))) {
                Log.i(TAG, "at MyWebKit, shouldOverrideUrlLoading," +
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
                    Log.i(TAG, "at shouldOverrideUrlLoading. " +
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
            } else if (url.startsWith(getString(R.string.tel_url_search))) {
                //Handle telephony Urls
                Log.i(TAG, "at shouldOverrideUrlLoading, Url is " + url);
                return true;
            }
            //if its any other link
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); //open url in browser
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
                    Log.i(TAG, "at shouldOverrideUrlLoading. " +
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
            Log.i(TAG, "at onReceivedHttpError, error is " + errorResponse);
            super.onReceivedHttpError(view, request, errorResponse);
            showAlertScreen();
        }
    }

}
