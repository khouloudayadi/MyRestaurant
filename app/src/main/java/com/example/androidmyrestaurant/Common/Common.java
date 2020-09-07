package com.example.androidmyrestaurant.Common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import com.example.androidmyrestaurant.Model.FavoriteOnlyId;
import com.example.androidmyrestaurant.Model.Restaurant;
import com.example.androidmyrestaurant.Model.User;
import com.example.androidmyrestaurant.R;
import com.example.androidmyrestaurant.Retrofit.IFCMService;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;

import java.util.List;

import androidx.core.app.NotificationCompat;


public class Common {

    public static final String baseUrl ="http://192.168.43.117:3000/" ;
    public static final String URL_IMG ="http://192.168.43.117:3000/images/" ;
    public static final String API_KEY ="1234" ;
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String REMEMBER_FBID ="REMEMBER_FBID" ;
    public static final String API_KEY_TAG = "API_KEY";
    public static final String TITLE_NOTIF = "title" ;
    public static final String CONTENT_NOTIF = "content" ;
    public static User currentUser;
    public static Restaurant currentRestaurant;

    public static List<FavoriteOnlyId> currentFavOfRestaurant;


    public static boolean checkFavorite(int id, List<FavoriteOnlyId> currentFavOfRestaurant) {
        boolean result = false;
        for (FavoriteOnlyId item:currentFavOfRestaurant)
            if (item.getFoodId() == id) {
                result = true;
            }
        return result;
    }

    public static void removeFavorite(int id, List<FavoriteOnlyId> currentFavOfRestaurant) {
        for (FavoriteOnlyId item:currentFavOfRestaurant){
         if (item.getFoodId() == id){
             currentFavOfRestaurant.remove(item);
         }
        }
    }

    public static String convertStatusToString(int orderStatus){
        switch (orderStatus)
        {
            case 0:
                return "Placed";
            case 1:
                return "Shippiing";
            case 2:
                return "Shipped";
            default:
                return "Cancelled";
        }
    }

    public static IFCMService getFCMService(){
        return RetrofitClient.getInstance("https://fcm.googleapis.com/").create(IFCMService.class);
    }

    public static void shoowNotification(Context context, int notifid, String  title, String body, Intent intent) {

        PendingIntent pendingIntent = null;

        if(intent != null)

            pendingIntent = PendingIntent.getActivity(context,notifid,intent,PendingIntent.FLAG_UPDATE_CURRENT);

            String NOTIFICATION_CHANNEL_ID = "My_Restaurant";

            // enregistre le canal avec le système
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) //Starting in Android 8.0 (API level 26)
            {
                // all notifications must be assigned to a channel
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "My Restaurant Notification",NotificationManager.IMPORTANCE_DEFAULT); //Fait un son

                notificationChannel.setDescription("My Restaurant Client App");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new  long[]{0,1000,500,1000});
                notificationChannel.enableVibration(true);

                notificationManager.createNotificationChannel(notificationChannel);
            }

            //create notif
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);

            builder.setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_notifications_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.app_icon));

            if(pendingIntent != null)
                   builder.setContentIntent(pendingIntent);

            //add as notification
            notificationManager.notify(notifid,builder.build());


    }
}
