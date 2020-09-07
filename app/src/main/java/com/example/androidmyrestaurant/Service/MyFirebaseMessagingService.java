package com.example.androidmyrestaurant.Service;

import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.FoodActivity;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable;

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
        compositeDisposable = new CompositeDisposable();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
    }

    @Override
    public void onNewToken( String newToken) {
        super.onNewToken(newToken);
        String fbid = Paper.book().read(Common.REMEMBER_FBID);
        String apiKey = Paper.book().read(Common.API_KEY_TAG);
        compositeDisposable.add(myRestaurantAPI.updateTokenServer(apiKey,fbid,newToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tokenModel -> {
                        },
                        throwable -> {
                                Toast.makeText(MyFirebaseMessagingService.this,"[Update Token]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        }));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //get Notification
        Map<String,String> dataRecv = remoteMessage.getData();

        if (dataRecv != null){
            Common.shoowNotification(this,new Random().nextInt(),
                    dataRecv.get(Common.TITLE_NOTIF),
                    dataRecv.get(Common.CONTENT_NOTIF),
                    null);
        }

    }
}
