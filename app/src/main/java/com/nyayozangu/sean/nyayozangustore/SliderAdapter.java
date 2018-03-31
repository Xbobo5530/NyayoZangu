package com.nyayozangu.sean.nyayozangustore;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Sean on 3/12/18.
 */

public class SliderAdapter extends PagerAdapter {

    /**
     * the content of the sliderView
     */
    //Arrays
    public int[] slide_images = {
            R.drawable.nyayozangu_tutorial_image,
            R.drawable.requests_tutorial_image,
            R.drawable.payments_tutorial_image,
            R.drawable.delivery_tutorial_image
    };
    public String[] slide_headings = {
            "NYAYO ZANGU STORE",
            "PRODUCT REQUESTS",
            "MOBILE PAYMENTS",
            "DOOR TO DOOR DELIVERY"
    };
    public String[] slide_descriptions = {
            "Find products you are looking for\nfrom our catalog of unique products\nhandpicked just for you.",
            "In case you can't find what you are looking for,\nhead over to the Product Requests section\nand tell us what you need.",
            "Making payments is fast and easy.\nUse MPesa, Tigo Pesa, Airtel Money\nor even a bank deposit if you so choose.",
            "We understand how precious your time is, \nwe will bring your package to you.\nWherever that might be."
    };
    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context){

        this.context = context;
    }

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        try {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

            ImageView slideImageView = view.findViewById(R.id.tutortialImageView);
            TextView slideHeading = view.findViewById(R.id.tutorialHeadingTextView);
            TextView slideDescriptions = view.findViewById(R.id.tutorialDescriptionTextView);

            slideImageView.setImageResource(slide_images[position]);
            slideHeading.setText(slide_headings[position]);
            slideDescriptions.setText(slide_descriptions[position]);

            container.addView(view);

            return view;
        }catch (NullPointerException e){
            Log.i("Sean","Error on inflating, error is " + e.getMessage());
            return null;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((RelativeLayout)object);

    }
}
