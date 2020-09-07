package com.example.androidmyrestaurant.Adapter;

import android.util.Log;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Model.Restaurant;
import com.example.androidmyrestaurant.R;

import java.util.List;

import ss.com.bannerslider.adapters.SliderAdapter;
import ss.com.bannerslider.viewholder.ImageSlideViewHolder;

public class RestaurantSliderAdapter extends SliderAdapter {

    List<Restaurant> restaurantList;

    public RestaurantSliderAdapter(List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    @Override
    public void onBindImageSlide(int position, ImageSlideViewHolder imageSlideViewHolder) {
        imageSlideViewHolder.bindImageSlide(Common.URL_IMG+restaurantList.get(position).getImage());
    }
}
