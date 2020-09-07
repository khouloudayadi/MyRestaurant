package com.example.androidmyrestaurant.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(version = 5,entities = cartItem.class,exportSchema = false)
public abstract class cartDataBase  extends RoomDatabase {

    private static cartDataBase  instance;

    public abstract CartDAO cartDAO();

    public  static cartDataBase getInstance(Context context){
       if (instance == null)
        instance= Room.databaseBuilder(context,cartDataBase.class,"MyRestaurantCart")
                .build();
        return instance;
    }
}
