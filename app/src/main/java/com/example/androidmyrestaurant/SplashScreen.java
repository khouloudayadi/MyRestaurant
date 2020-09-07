package com.example.androidmyrestaurant;


import android.Manifest;
import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.example.androidmyrestaurant.Service.MyFirebaseMessagingService;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.*;

public class SplashScreen extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI myRestaurantAPI;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        //permission
       Dexter.withActivity(this)////la bibliothèque Dexter simplifier le processus d’ajout des autorisations d’exécution
               .withPermissions(permission.ACCESS_FINE_LOCATION , permission.WRITE_EXTERNAL_STORAGE)//pour demander une autorisation
               .withListener(new MultiplePermissionsListener() {
                   @Override
                   public void onPermissionsChecked(MultiplePermissionsReport report) {
                       if (report.areAllPermissionsGranted()) {
                           // do you work now
                           //get token notification
                           FirebaseInstanceId.getInstance().getInstanceId()
                                   .addOnFailureListener(new OnFailureListener(){
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           Toast.makeText(SplashScreen.this,"[Get Token]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                       }
                                   })
                                   .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>(){
                                       @Override
                                       public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                           if (task.isSuccessful()){
                                               AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                                   @Override
                                                   public void onSuccess(Account account) {
                                                       //Toast.makeText(SplashScreen.this,R.string.Alredy_logged, Toast.LENGTH_SHORT).show();
                                                       //AccountKit.logOut();
                                                       //save fbid
                                                       Paper.book().write(Common.REMEMBER_FBID,account.getId());

                                                       dialog.show();
                                                       compositeDisposable.add(myRestaurantAPI.updateTokenServer(Common.API_KEY,account.getId(),task.getResult().getToken())
                                                               .subscribeOn(Schedulers.io())
                                                               .observeOn(AndroidSchedulers.mainThread())
                                                               .subscribe(tokenModel -> {
                                                                           if(!tokenModel.isSuccess())
                                                                               Toast.makeText(SplashScreen.this,"[Update Token Result]"+tokenModel.getMessage(),Toast.LENGTH_SHORT).show();

                                                                           compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY,account.getId())
                                                                                   .subscribeOn(Schedulers.io())
                                                                                   .observeOn(AndroidSchedulers.mainThread())
                                                                                   .subscribe(userModel -> {
                                                                                               if(userModel.isSuccess()){  //if user is available in database , start MaineActivity for homeActivity
                                                                                                   Common.currentUser = userModel.getResult().get(0);

                                                                                                   Intent home = new Intent(SplashScreen.this,HomeActivity.class);
                                                                                                   startActivity(home);
                                                                                                   finish();
                                                                                               }
                                                                                               else{//if user not available in database, start updateActivity for register information
                                                                                                   Intent update = new Intent(SplashScreen.this,UpdateInfoActivity.class);
                                                                                                   startActivity(update);
                                                                                                   finish();
                                                                                               }
                                                                                               dialog.dismiss();
                                                                                           },
                                                                                           throwable -> {
                                                                                               dialog.dismiss();
                                                                                               Toast.makeText(SplashScreen.this,"[GET USER API]" +throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                           }) );

                                                                       },
                                                                       throwable -> {
                                                                           Toast.makeText(SplashScreen.this,"[Update Token]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                                                       }));
                                                   }

                                                   @Override
                                                   public void onError(AccountKitError accountKitError) {
                                                       Toast.makeText(SplashScreen.this,R.string.AccountKitError, Toast.LENGTH_SHORT).show();
                                                       startActivity(new Intent(SplashScreen.this,MainActivity.class));
                                                       finish();
                                                   }
                                               });
                                           }
                                       }
                                   });


                       }

                       if (report.isAnyPermissionPermanentlyDenied()) {
                           // permission is denied permenantly, navigate user to app settings

                       }
                   }

                   @Override
                   public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                   }
               })
               .check();
        //Clés de hachage
       // printKeyHash();
/*
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                startActivity(new Intent(SplashScreen.this,MainActivity.class));
                finish();
            }
        },5000);

*/
    }

    private void init() {
        Paper.init(this);
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature:info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KEY_HASH", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
