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
            "Simply find all the products you are looking for\nfrom our wide catalog of unique products\nhandpicked just for you.",
            "In case you don't find what you are looking for,\nhead over to the Product Requests section\nand tell us what you need.",
            "After placing your order\nsimply make your payments\nwith your preferred payment solution\nlike MPesa, Tigo Pesa, Airtel Money... \nOr even a bank deposit if you so choose.",
            "We understand how precious your time is, \nso after you make your purchase,\nwe will deliver your packaged where ever you want it."
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
