package com.example.androidmyrestaurant.DataBase;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CartDAO {
    @Query("SELECT * FROM Cart WHERE fbid=:fbid AND restaurantId=:restaurantId")
    Flowable<List<cartItem>> getAllCart(String fbid,int restaurantId);

    @Query("SELECT COUNT(*) FROM Cart WHERE fbid=:fbid AND restaurantId=:restaurantId")
    Single<Integer> countItemCart(String fbid, int restaurantId);

    @Query("SELECT SUM(foodPrice*foodQuantity) FROM Cart WHERE fbid=:fbid AND restaurantId=:restaurantId")
    Single<Long> sumPrice(String fbid, int restaurantId);

    @Query("SELECT * FROM Cart WHERE foodId=:foodId AND fbid=:fbid AND restaurantId=:restaurantId")
    Single<cartItem> getItemCart(String foodId,String fbid,int restaurantId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertorReplaceAll(cartItem... cartItems);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCart(cartItem cart);

    @Delete
    Single<Integer> deleteCart(cartItem cart);

    @Query("DELETE FROM Cart WHERE fbid=:fbid AND restaurantId=:restaurantId")
    Single<Integer> cleanCart(String fbid,int restaurantId);
}
