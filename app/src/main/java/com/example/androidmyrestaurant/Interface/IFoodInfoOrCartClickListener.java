package com.example.androidmyrestaurant.Interface;

import android.view.View;

public interface IFoodInfoOrCartClickListener {
    void onFoodItemClickListener(View view,int position, boolean isInfo);
}

