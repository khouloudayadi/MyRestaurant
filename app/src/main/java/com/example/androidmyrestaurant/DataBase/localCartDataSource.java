package com.example.androidmyrestaurant.DataBase;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class localCartDataSource implements cartDataSource {
    private CartDAO cartDAO;

    public localCartDataSource(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public Flowable<List<cartItem>> getAllCart(String fbid, int restaurantId) {
        return cartDAO.getAllCart(fbid,restaurantId);
    }

    @Override
    public Single<Integer> countItemCart(String fbid, int restaurantId) {
        return cartDAO.countItemCart(fbid,restaurantId);
    }

    @Override
    public Single<Long> sumPrice(String fbid, int restaurantId) {
        return cartDAO.sumPrice(fbid,restaurantId);
    }

    @Override
    public Single<cartItem> getItemCart(String foodId, String fbid, int restaurantId) {
        return cartDAO.getItemCart(foodId,fbid,restaurantId);
    }

    @Override
    public Completable insertorReplaceAll(cartItem... cartItems) {
        return cartDAO.insertorReplaceAll(cartItems);
    }

    @Override
    public Single<Integer> updateCart(cartItem cart) {
        return cartDAO.updateCart(cart);
    }

    @Override
    public Single<Integer> deleteCart(cartItem cart) {
        return cartDAO.deleteCart(cart);
    }

    @Override
    public Single<Integer> cleanCart(String fbid, int restaurantId) {
        return cartDAO.cleanCart(fbid,restaurantId);
    }
}
