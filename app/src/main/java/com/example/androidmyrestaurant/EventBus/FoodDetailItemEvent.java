package com.example.androidmyrestaurant.EventBus;

import com.example.androidmyrestaurant.Model.Food;

import java.util.List;

public class FoodDetailItemEvent {
    private boolean success;
    private Food food;

    public FoodDetailItemEvent(boolean success, Food food) {
        this.success = success;
        this.food = food;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }
}
