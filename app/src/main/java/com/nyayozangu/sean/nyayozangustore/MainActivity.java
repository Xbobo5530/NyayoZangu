package com.nyayozangu.sean.nyayozangustore;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String EXTRA_MESSAGE = "com.nyayozangu.sean.nyayozangustore.CREATE_ACC_URL";
    private static final String TAG = "Sean";

    boolean doubleBackToExitPressedOnce = false;
    //WebView states
    Bundle inState;
    Bundle outState;
    private FirebaseAnalytics mFirebaseAnalytics;
    private WebView mWebView;
    private FrameLayout mMainFrame;
    private RelativeLayout mFabBackground;
    private ProgressBar mProgressBar;
    private BottomNavigationView navigation;
    private FloatingActionButton mFab, mShareFab, mSearchFab, mChatFab;
    private SearchView mSearchView;
    private HomeFragment homeFragment;
    private MeFragment meFragment;
    private CartFragment cartFragment;
    private CollectionsFragment collectionsFragment;
    private AlertFragment alertFragment;
    private MoreFragment moreFragment;
    private Boolean isFabOpen;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;
    private ProgressDialog mProgressDialog;
    private List<String> urls = new ArrayList<>();

    {
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            //on selecting bottom navigation item
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        setFragment(homeFragment, getString(R.string.store_home_url));
                        return true;
                    case R.id.navigation_collections:
                        setFragment(collectionsFragment, getString(R.string.store_collections_url));
                        return true;
                    case R.id.navigation_cart:
                        setFragment(cartFragment, getString(R.string.store_cart_url));
                        return true;
                    case R.id.navigation_more:
                        setFragment(moreFragment, null);
                        return true;
                }
                return false;
            }
        };
    }

    // TODO: 3/28/18 when returning from the chatActivity restore mWebView

    // TODO: 3/9/18 implement the onSavedInstance method to properly handle configuration changes

    /*@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        mWebView.saveState(savedInstanceState);
        savedInstanceState.putString("savedUrl", mWebView.getUrl());

    }*/

    // TODO: 3/9/18 implement onRestoreInstance methods to handle configuration changes
    /*@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);

        String savedUrl = savedInstanceState.getString("savedUrl");
        mWebView.loadUrl(savedUrl);

    }*/

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
        mMainFrame = findViewById(R.id.main_frame);
        mFabBackground = findViewById(R.id.fab_background);
        mSearchView = findViewById(R.id.search_view);

        mFab = findViewById(R.id.fab);
        mShareFab = findViewById(R.id.share_fab);
        mSearchFab = findViewById(R.id.search_fab);
        mChatFab = findViewById(R.id.chat_fab);
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

        showProgress(); //load page display the loading progress bar

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
            //determine if MainActivity is launched from createAccount
            Intent intent = getIntent();
            if (intent.getStringExtra(EXTRA_MESSAGE) == null) {
                //not coming from create acc
                if (getIntent().getData() != null) {
                    //checking for deep links
                    onNewIntent(getIntent());
                } else {
                    //no deep link data
                    proceed();
                }
            } else {
                //coming from create account
                Log.i(TAG, "gettingStringExtra, extra message is "
                        + intent.getStringExtra(EXTRA_MESSAGE));
                String createAccUrl = intent.getStringExtra(EXTRA_MESSAGE);
                setFragment(meFragment, createAccUrl);
            }
        } else {
            checkConnection();
        }
    }

    private void setFragment(Fragment fragment, String url) {
        //set fragment, and pass on the url to load
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
        if (url != null) {
            // TODO: 3/28/18 check if there are saved pages
            mWebView.loadUrl(url);
        }
        Log.i(TAG, "at setFragment, setting Fragment " + fragment.toString() +
                "\nloading mWebView, url is " + url);
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
        Log.i(TAG, "at proceed()");
    }

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
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // load online by default
        //extra settings

    }

    private void showProgress() {
        //construct the progressDialog
        mProgressDialog = new ProgressDialog(this);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (isConnected()) {
                    if (progress < 40) {
                        mProgressDialog.setMessage("Loading...");
                        mProgressDialog.show();
                    } else {
                        mProgressDialog.dismiss();
                    }
                } else {
                    checkConnection();
                }
            }
        });
    }

    public void checkConnection() {
        Log.i(TAG, "at checkConnection");
        if (isConnected()) {
            mFab.setVisibility(View.VISIBLE); //when connected show the fab
            mWebView.setVisibility(View.VISIBLE); //show webView
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
        //when the retry button is check the connection;
        rotateReconnect(view);
        checkConnection();
    }

    @Override
    public void onBackPressed() {
        //back button is pressed
        Log.i(TAG, "at onBackPressed: back is pressed");
        //check if webView has history

        if (mWebView.getUrl() != null &&
                !mWebView.getUrl().equals(getString(R.string.store_home_url)) &&
                mWebView.canGoBack()) {
            Log.d(TAG, "not at home\n mWebView can go back");
            /*
            * mWebView is not null
            * mwWebView is not at home
            * has history
            * the urls have content
            */

            //launch the previous page
            mWebView.goBack();
            Log.d(TAG, "going back");

            // TODO: 3/29/18 handle update bottom nav btn on backPressed

            /*
            mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            Log.d(TAG, "home is selected from back");
                            setFragment(homeFragment, urls.get(0));
                            return true;
                        case R.id.navigation_collections:
                            setFragment(collectionsFragment, urls.get(0));
                            return true;
                        case R.id.navigation_cart:
                            setFragment(cartFragment, urls.get(0));
                            return true;
                    }
                    return false;
                }
            };


            //update bottom navigation
            if (urls.get(0).contains("collections") &&
                    urls.get(0).contains("products")){
                Log.d(TAG,"contains collections/products: " + urls.get(0));
                navigation.setSelectedItemId(R.id.navigation_collections);
            }else if (urls.get(0).contains("cart")){
                Log.d(TAG,"contains cart: " + urls.get(0));
                navigation.setSelectedItemId(R.id.navigation_cart);
            }
            else{
                Log.d(TAG,"contains else: " + urls.get(0));
                navigation.setSelectedItemId(R.id.navigation_home);
            }*/

            //update urls
            if (urls.size() > 0) {
                urls.remove(0);
            }

        } else {
            /*
            * webView at home page
            * webView has no history
            * Urls list is empty*/
            Log.d(TAG, "clearing urls\n ");
            for (int i = 1; i < urls.size() - 1; i++) {
                Log.d(TAG, "clearing: " + urls.get(i));
                urls.remove(i);
            }

            doubleBackToExit();

        }
    }

    public void doubleBackToExit() {
        Log.d(TAG, "at doubleBackToExit");

        if (doubleBackToExitPressedOnce) {
            Log.d(TAG, "pressed once");
            //back button is pressed for the first time
            super.onBackPressed();
            return;
        }
        //change the back button pressed once true
        this.doubleBackToExitPressedOnce = true;
        promptExit();
        //create a delay to listen to the second time back is ressed
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //when the 2 seconds pass reset the number of counts back is pressed
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void promptExit() {
        Snackbar.make(mMainFrame,
                "Are you you want to exit?",
                Snackbar.LENGTH_LONG)
                .setAction("Exit", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

    public void rotateReconnect(View view) {
        //animate rotation on the alert screen
        ImageView reconnectAnimation;
        reconnectAnimation = findViewById(R.id.reconnect_image_view);
        Animation startRotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_once);
        reconnectAnimation.startAnimation(startRotateAnimation);
    }

    public void openSearch() {
        Log.d(TAG, "at openSearch");
        showSearchView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //get the query and process it
                String query = String.valueOf(mSearchView.getQuery());
                if (!query.isEmpty()) {
                    String constructedUrl = searchItemUrlConstructor(query);
                    setFragment(collectionsFragment, constructedUrl);
                }
                //restore search view state
                hideSearchView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void showSearchView() {

        if (navigation.getSelectedItemId() == R.id.navigation_more) {
            navigation.setSelectedItemId(R.id.navigation_collections);
        }
        //Load animation
        Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);
        mSearchView.startAnimation(slide_down);
        mSearchView.setVisibility(View.VISIBLE);

        //editing the text color and hints of the searchView
        int textViewId = mSearchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView textView = mSearchView.findViewById(textViewId);
        textView.setTextColor(Color.WHITE);
        textView.setHintTextColor(getResources().getColor(R.color.colorWhiteTransparent));

        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setFocusable(true);
        mSearchView.setIconified(false);
        mSearchView.requestFocusFromTouch();
        mSearchView.setQueryHint(getString(R.string.search_view_query_hint_text));
        mFabBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchView();
                mFabBackground.setClickable(false);
            }
        });
    }

    private void hideSearchView() {
        if (mSearchView.getVisibility() == View.VISIBLE) {
            //load animation
            Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_up);
            mSearchView.startAnimation(slide_up);
            mSearchView.setQuery(String.valueOf(""), false);
            mSearchView.setVisibility(View.GONE);
        }
    }

    private String searchItemUrlConstructor(String query) {
        Log.d(TAG, "at searchItemUrlConstructor");
        String searchUrlHead = "https://store.nyayozangu.com/search?q=";
        StringBuilder refinedSearchQuery = new StringBuilder();
        for (int i = 0; i < query.length(); i++) {
            if (query.substring(i).equals(" ")) {
                refinedSearchQuery.append("+");
            } else {
                refinedSearchQuery.append(query.charAt(i));
            }
        }
        Log.d(TAG, "refinedSearchQuery: " + refinedSearchQuery);
        return searchUrlHead + refinedSearchQuery;
    }

    public void openChat() {
        //opening the chatActivity
        Log.d(TAG, "at openChat");
        startActivity(new Intent(getApplicationContext(), ChatActivity.class));
    }

    //for FAB animation
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.share_fab:
                animateFAB();
                sharePage();
                Log.d(TAG, "shareFab");
                break;
            case R.id.search_fab:
                //open search
                animateFAB();
                openSearch();
                Log.d(TAG, "searchFab");
                break;
            case R.id.chat_fab:
                //openChat
                animateFAB();
                openChat();
                Log.d(TAG, "chatFAB");
                break;
        }
    }

    private void sharePage() {
        try {
            String urlToShare = mWebView.getOriginalUrl();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, urlToShare);
            startActivity(Intent.createChooser(shareIntent, "Share this page with"));
        } catch (Exception e) {
            Log.i(TAG, "at sharePage, error is " + e.getMessage());
        }
    }

    public void animateFAB() {

        if (isFabOpen) {

            mFab.startAnimation(rotate_backward);
            mShareFab.startAnimation(fab_close);
            mSearchFab.startAnimation(fab_close);
            mChatFab.startAnimation(fab_close);
            mShareFab.setClickable(false);
            mSearchFab.setClickable(false);
            mChatFab.setClickable(false);
            isFabOpen = false;
            mFabBackground.setClickable(false);
            Log.d(TAG, "close");

        } else {

            mFab.startAnimation(rotate_forward);
            mShareFab.startAnimation(fab_open);
            mSearchFab.startAnimation(fab_open);
            mChatFab.startAnimation(fab_open);
            mShareFab.setClickable(true);
            mSearchFab.setClickable(true);
            mChatFab.setClickable(true);
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

    public void openChatTest(View view) {
        openChat();
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
                } else if (url.contains("collections") ||
                        url.contains("products")) {
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
            }
            //for liveChat
            else if (url.contains("twak.to/chat/")) {
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

            if (uri.toString().contains(getString(R.string.nyayozangucom_url_search))) {
                /*
                * let webView load the page
                * update bottomNavBar
                */
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
                } else if (uri.toString().contains("twak.to/chat/")) {
                    return false;
                } else {
                    setFragment(meFragment, uri.toString());
                }

                return false;
            } else if (uri.toString().startsWith(getString(R.string.tel_url_search))) {
                //Handle telephony Urls
                // TODO: 3/26/18 follow the crash on phone call bug
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (errorResponse.getStatusCode() == ERROR_TIMEOUT) {
                    showAlertScreen();
                } else {

                    //show snack bar
                    Snackbar.make(mMainFrame,
                            "Unstable connection...",
                            Snackbar.LENGTH_LONG)
                            .setAction("Reconnect", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    checkConnection();
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.colorAccent))
                            .show();
                }
            } else {
                showAlertScreen();
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            Log.d(TAG, "at onPageStarted \n");

            //check if webView nav if is back or forward
            if (!mWebView.canGoForward() && urls.size() != 0) {
                Log.d(TAG, "at latest page");
                if (urls.size() > 1) {//urls has more than one item
                    if (!urls.get(0).equals(urls.get(1))) {//when item @0 != item @1
                        urls.add(0, url);
                        Log.d(TAG, "url added: " + urls.get(0) +
                                "\nurls size is: " + urls.size() +
                                "\nurls are: " + urls.toString());
                    } else if (urls.size() == 1 &&
                            !urls.get(0).equals(getString(R.string.store_home_url))) {
                        urls.add(0, url);
                        Log.d(TAG, "url added: " + urls.get(0) +
                                "\nurls size is: " + urls.size() +
                                "\nurls are: " + urls.toString());
                    }
                } else {
                    urls.add(0, url);
                    Log.d(TAG, "url added: " + urls.get(0) +
                            "\nurls size is: " + urls.size() +
                            "\nurls are: " + urls.toString());
                }
            }
            super.onPageStarted(view, url, favicon);
        }
    }

}
