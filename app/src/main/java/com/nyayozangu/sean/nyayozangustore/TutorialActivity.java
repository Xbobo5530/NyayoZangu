package com.nyayozangu.sean.nyayozangustore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TutorialActivity extends AppCompatActivity {

    private static final String EXTRA_MESSAGE = "com.nyayozangu.sean.nyayozangustore.CREATE_ACC_URL";
    private ViewPager mSlideViewPager;
    private LinearLayout mDotsLayout;
    private SliderAdapter sliderAdapter;
    private Button mBackButton;
    private Button mNextButton;

    //the dots
    private TextView[] mDots;
    private int mCurrentPage;
    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onPageSelected(int i) {

            addDotsIndicator(i);
            mCurrentPage = i;

            if (i == 0 ){
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(false);
                mBackButton.setVisibility(View.INVISIBLE);
                mNextButton.setText(R.string.next_tut_text);
                mBackButton.setText("");

            }else if(i == mDots.length -1){
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(true);
                mBackButton.setVisibility(View.VISIBLE);
                mNextButton.setText(R.string.finish_tut_text);
                mNextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //start the main activity when finish is clicked
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });
                mBackButton.setText(getString(R.string.back_tut_text));

            }else{
                mNextButton.setEnabled(true);
                mBackButton.setEnabled(true);
                mBackButton.setVisibility(View.VISIBLE);
                mNextButton.setText(R.string.next_tut_text);
                mBackButton.setText(R.string.back_tut_text);
                mNextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mSlideViewPager.setCurrentItem(mCurrentPage + 1);
                    }
                });


            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        mSlideViewPager = findViewById(R.id.tutorial_view_pager_layout);
        mDotsLayout = findViewById(R.id.dots_layout);
        mBackButton = findViewById(R.id.tutorial_prev_button);
        mNextButton = findViewById(R.id.tutorial_next_button);

        sliderAdapter = new SliderAdapter(this);

        mSlideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);

        mSlideViewPager.addOnPageChangeListener(viewListener);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSlideViewPager.setCurrentItem(mCurrentPage + 1);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlideViewPager.setCurrentItem(mCurrentPage -1);
            }
        });

    }

    public void addDotsIndicator(int position){

        mDots = new TextView[4];
        mDotsLayout.removeAllViews();

        for (int i = 0; i < mDots.length; i++){
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226;"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.colorWhiteTransparent));
            mDotsLayout.addView(mDots[i]);

        }

        if (mDots.length > 0){

            mDots[position].setTextColor(getResources().getColor(R.color.colorWhite));
        }

    }

    public void closeTutorial(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
        Log.i("Sean", "at Tutorial screen, close btn is clicked");
    }

    public void createAccount(View view) {
        //open the create acc page when clicked
        Intent intent = new Intent(this, MainActivity.class);
        String createAccUrl = getString(R.string.create_acc_url);
        intent.putExtra(EXTRA_MESSAGE, createAccUrl);
        startActivity(intent);
        finish();

    }
}
