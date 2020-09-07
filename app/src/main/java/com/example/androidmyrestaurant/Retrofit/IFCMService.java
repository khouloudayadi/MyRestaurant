package com.example.androidmyrestaurant.Retrofit;

import com.example.androidmyrestaurant.Model.FCMRespons;
import com.example.androidmyrestaurant.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
      @Headers({
              "Content-Type:application/json",
              "Authorization:key=AAAAaB9m_qU:APA91bF3kOLXGlA4XBUbJzr_ZSKKE12zlTjZM6osWLQuKDEvgeYCE00g_UH_QaOa5V4ePnzHMBr0YqfOzuKSm1l5ek4SkrolQkGamZOpuYv0VDVXqnuUtTVgd_UBrDjWbLXnixyo1Dnf"
      })

      @POST("fcm/send")
      Observable<FCMRespons> sendNotification(@Body FCMSendData body);

}
