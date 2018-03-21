package com.nyayozangu.sean.nyayozangustore;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
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
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;{
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            //on selecting bottom navigation item
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        if (isConnected()) {
                            setFragment(homeFragment);
                            mWebView.loadUrl(getString(R.string.store_home_url));
                            Log.i("Sean", "at homeFragment, url is " + mWebView.getUrl() );
                            return true;
                        }
                        checkConnection();
                        return true;
                    case R.id.navigation_collections:
                        if (isConnected()) {
                            setFragment(collectionsFragment);
                            mWebView.loadUrl(getString(R.string.store_collections_url));
                            return true;
                        }
                        checkConnection();
                        return true;
                    case R.id.navigation_cart:
                        if (isConnected()) {
                            setFragment(cartFragment);
                            mWebView.loadUrl(getString(R.string.store_cart_url));
                            return true;
                        }
                        checkConnection();
                        return true;
                    case R.id.navigation_more:
                        if (isConnected()) {
                            setFragment(moreFragment);
                            return true;
                        }
                        checkConnection();
                        return true;
                }
                return false;
            }
        };
    }
    private void setFragment(Fragment fragment) {
        //set fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
        Log.i("Sean", "at setFragment, setting Fragment " + fragment.toString());
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //constructing elements
        //main activity & webView
        mWebView = findViewById(R.id.webView);
        mProgressBar = findViewById(R.id.progress_bar);

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
                setFragment(meFragment);
                String createAccUrl = intent.getStringExtra(EXTRA_MESSAGE);
                mWebView.loadUrl(createAccUrl);
            }
        } else {
            checkConnection();
        }

        handleDeepLinkIntent(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLinkIntent(intent);
    }

    private void handleDeepLinkIntent(Intent intent) {

        // TODO: 3/18/18 fix the deepLinkIssue

        // ATTENTION: This was auto-generated to handle app links.
//        Intent appLinkIntent = getIntent();
//        String appLinkAction = appLinkIntent.getAction();
//        Uri appLinkData = appLinkIntent.getData();
        Log.i("Sean", "at handleDeepLinkIntent");
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            String pageUrl = appLinkData.getLastPathSegment();
            Uri appData = Uri.parse("https://store.nyayozangu.com").buildUpon()
                    .appendPath(pageUrl).build();
            //open the meFragment and load the appData url;
            setFragment(homeFragment);
            mWebView.loadUrl(String.valueOf(appData));
            Log.i("Sean", "at handleDeepLinkIntent, url is " + String.valueOf(appData));
        }
    }

    private void proceed() {
        setFragment(homeFragment);
        mWebView.loadUrl(getString(R.string.store_home_url));
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
                    //set the corresponding BottomNavigationBarItem
//                    updateNavBarItem();
                }

                mProgressBar.setProgress(progress);
                if (progress == 100) {
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

    private void updateNavBarItem() {
        // TODO: 3/18/18 handle the updateNavBarItem method.

        Log.i("Sean", "at updateNavBarItem, currentUrl is " + mWebView.getUrl());
        //check the url
        //for the collections/products related links
        if (mWebView.getUrl().contains(getString(R.string.store_collections_url)) ||
                mWebView.getUrl().contains(getString(R.string.store_products_url_head))) {
            //set the navigation item to collections
            String newUrl = mWebView.getUrl();
            Log.i("Sean", "at updateNavBarItem, newUrl is " + newUrl);
            navigation.setSelectedItemId(R.id.navigation_collections);
            mWebView.loadUrl(newUrl);
        }
        //for cart related links
//        else if (currentUrl.contains()){}else{}
    }

    public void checkConnection() {
        Log.i("Sean", "at checkConnection");
        if (isConnected()){
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
        Log.i("Sean","at showAlertScreen");
        setFragment(alertFragment);

    }

    public void openAccount(View view) {
        if (isConnected()){
            setFragment(meFragment);
            mWebView.loadUrl(getString(R.string.store_acc_url));
        }else{
            checkConnection();
        }
    }

    public void openProductRequests(View view) {
        if (isConnected()){
            setFragment(meFragment);
            mWebView.loadUrl(getString(R.string.store_requests_url));
        }else{checkConnection();}
    }

    public void openBlog(View view) {
        if (isConnected()){
            setFragment(meFragment);
            mWebView.loadUrl("https://store.nyayozangu.com/blogs/stories");
        }else{checkConnection();}
    }

    public void openContact(View view) {
        if (isConnected()) {
            setFragment(meFragment);
            mWebView.loadUrl("https://store.nyayozangu.com/pages/contact-us");
        }else{checkConnection();}
    }

    public void openAbout(View view) {
        if (isConnected()) {
            setFragment(meFragment);
            mWebView.loadUrl("https://store.nyayozangu.com/pages/about-us");
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
            mWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    public void rotateReconnect(View view) {
        ImageView reconnectAnimation;
        reconnectAnimation = findViewById(R.id.reconnect_image_view);
        Animation startRotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_once);
        reconnectAnimation.startAnimation(startRotateAnimation);
    }

    public void openSearch(View view) {
        if (isConnected()) {
            setFragment(meFragment);
            mWebView.loadUrl(getString(R.string.store_search_url)
            );
        } else {
            checkConnection();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        //manage navigation to outside links by creating a MyWebView class that extends the WebViewClient Class
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().contains(getString(R.string.nyayozangucom_url_search))) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }else if (url.startsWith(getString(R.string.tel_url_search))) {
                //Handle telephony Urls
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                return true;
            }else if (url.contains("instagram")){
                Intent instaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                instaIntent.setPackage("com.instagram.android");
                try {
                    startActivity(instaIntent);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            }
            //updating the Bottom navigation bar
            else if (Uri.parse(url).getHost().contains(getString(R.string.store_collections_url)) ||
                    Uri.parse(url).getHost().contains(getString(R.string.store_products_url_head))) {
                Log.i("Sean", "at MyWebViewClient, url is " + url);
                navigation.setSelectedItemId(R.id.navigation_collections);
                mWebView.loadUrl(url);
            }
            //if its any other link
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); //open url in browser
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();

            if (uri.toString().contains(getString(R.string.nyayozangucom_url_search)) || uri.toString().contains(getString(R.string.shopify_url_text))) {
                    // This is my web site, so do not override; let my WebView load the page
                return false;
            } else if (uri.toString().startsWith(getString(R.string.mailto_url_search))) {
                //Handle mail Urls
                startActivity(new Intent(Intent.ACTION_SENDTO, uri));
            } else if (uri.toString().startsWith(getString(R.string.tel_url_search))) {
                //Handle telephony Urls
                startActivity(new Intent(Intent.ACTION_DIAL, uri));
            }
            //updating the Bottom Navigation Bar selected item
            else if (uri.toString().contains(getString(R.string.store_products_url_head)) ||
                    uri.toString().contains(getString(R.string.store_collections_url))) {
                navigation.setSelectedItemId(R.id.navigation_collections);
                mWebView.loadUrl(uri.toString());
            }
            //Handle Web Urls
            startActivity(new Intent(Intent.ACTION_VIEW, uri)); //open url in browser
            return true;
        }

        //update Bottom NavigationView


    }
}
