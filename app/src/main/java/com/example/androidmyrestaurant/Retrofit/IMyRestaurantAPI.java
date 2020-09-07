package com.example.androidmyrestaurant.Retrofit;

import com.example.androidmyrestaurant.Model.CreateOrderModel;
import com.example.androidmyrestaurant.Model.FavoriteModel;
import com.example.androidmyrestaurant.Model.FavoriteOnlyIdModel;
import com.example.androidmyrestaurant.Model.FoodModel;
import com.example.androidmyrestaurant.Model.MenuModel;
import com.example.androidmyrestaurant.Model.OrderModel;
import com.example.androidmyrestaurant.Model.RestaurantModel;
import com.example.androidmyrestaurant.Model.TokenModel;
import com.example.androidmyrestaurant.Model.UpdateUserModel;
import com.example.androidmyrestaurant.Model.UserModel;
import com.example.androidmyrestaurant.Model.sizeModel;

import io.reactivex.Observable;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IMyRestaurantAPI {


    @GET("user")
    Observable<UserModel> getUser(@Query("key") String apiKey,
                                  @Query("fbid") String fbid);


    @GET("restaurant")
    Observable<RestaurantModel> getRestaurant(@Query("key") String apiKey);

    @GET("restaurantById")
    Observable<RestaurantModel> getRestaurantById(@Query("key") String apiKey,
                                                  @Query("restaurantId") String restaurantId);


    @GET("menuByRestaurant")
    Observable<MenuModel> getmenyByRestaurant(@Query("key") String apiKey,
                                        @Query("restaurantId") int restaurantId);
    @GET("foodByMenu")
    Observable<FoodModel> getfoodByMenu(@Query("key") String apiKey,
                                        @Query("menuId") int menuId);


    @GET("foodById")
    Observable<FoodModel> getfoodById(@Query("key") String apiKey,
                                 @Query("foodId") int foodId);


    @GET("size")
    Observable<sizeModel> getSizeOfFood(@Query("key") String apiKey,
                                  @Query("foodId") int foodId);

    @GET("searchFood")
    Observable<FoodModel> searchFood(@Query("key") String apiKey,
                                      @Query("foodName") String foodName,
                                      @Query("menuId") int menuId);

    @GET("favoriteByUser")
    Observable<FavoriteModel> getfavoriteByUser(@Query("key") String apiKey,
                                                @Query("fbid") String fbid);

    @GET("favoriteByRestaurant")
    Observable<FavoriteOnlyIdModel> getfavoriteByRestaurant(@Query("key") String apiKey,
                                                         @Query("fbid") String fbid,
                                                         @Query("restaurantId") int restaurantId);

    @GET("order")
    Observable<OrderModel> getOrder(@Query("key") String apiKey,
                                    @Query("orderFBID") String orderFBID);



    @GET("nearByRestaurant")
    Observable<RestaurantModel> nearByRestaurant(@Query("key") String apiKey,
                                            @Query("lat") double lat,
                                            @Query("lng") double lng,
                                            @Query("distance") double distance );


    @GET("token")
    Observable<TokenModel> getToken(@Query("key") String apiKey,
                                     @Query("fbid") String fbid);


    @POST("token")
    @FormUrlEncoded
    Observable<TokenModel>updateTokenServer(@Field("key") String apiKey,
                                   @Field("fbid") String fbid,
                                   @Field("token") String token);

    @POST("user")
    @FormUrlEncoded
    Observable<UpdateUserModel>updateUserInfo(@Field("key") String apiKey,
                                              @Field("userPhone") String userPhone,
                                              @Field("userName") String userName,
                                              @Field("userAddress") String userAddress,
                                              @Field("fbid") String fbid);

    @POST("createOrder")
    @FormUrlEncoded
    Observable<CreateOrderModel> createOrder(@Field("key") String apiKey,
                                             @Field("orderFBID") String orderFBID,
                                             @Field("orderPhone") String orderPhone,
                                             @Field("orderName") String orderName,
                                             @Field("orderAddress") String orderAddress,
                                             @Field("orderDate") String orderDate,
                                             @Field("restaurantId") int restaurantId,
                                             @Field("transactionId") String transactionId,
                                             @Field("cod") String cod,
                                             @Field("totalPrice") double totalPrice,
                                             @Field("numOfItem") int numOfItem);


    @POST("favorite")
    @FormUrlEncoded
    Observable<FavoriteModel> addfavorite(@Field("key") String apiKey,
                                          @Field("fbid") String fbid,
                                          @Field("foodId") int foodId,
                                          @Field("restaurantId") int restaurantId,
                                          @Field("restaurantName") String restaurantName,
                                          @Field("foodName") String foodName,
                                          @Field("foodImage") String foodImage,
                                          @Field("price") double price);


    @DELETE("favorite")
    Observable<FavoriteModel> deleteFavorite(@Query("key") String apiKey,
                                             @Query("fbid") String fbid,
                                             @Query("foodId") int foodId,
                                             @Query("restaurantId") int restaurantId);




}
