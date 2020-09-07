package com.example.androidmyrestaurant.DataBase;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface cartDataSource {
    Flowable<List<cartItem>> getAllCart(String fbid, int restaurantId);

    Single<Integer> countItemCart(String fbid, int restaurantId);

    Single<Long> sumPrice(String fbid, int restaurantId);

    Single<cartItem> getItemCart(String foodId,String fbid,int restaurantId);

    Completable insertorReplaceAll(cartItem... cartItems);

    Single<Integer> updateCart(cartItem cart);

    Single<Integer> deleteCart(cartItem cart);

    Single<Integer> cleanCart(String fbid, int restaurantId);
}
