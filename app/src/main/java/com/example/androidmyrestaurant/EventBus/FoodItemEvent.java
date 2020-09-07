package com.example.androidmyrestaurant.EventBus;

import com.example.androidmyrestaurant.Model.Menu;

public class FoodItemEvent {
    private boolean success;
    private Menu menulist;

    public FoodItemEvent(boolean success, Menu menulist) {
        this.success = success;
        this.menulist = menulist;
    }



    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Menu getMenulist() {
        return menulist;
    }

    public void setMenulist(Menu menulist) {
        this.menulist = menulist;
    }
}
