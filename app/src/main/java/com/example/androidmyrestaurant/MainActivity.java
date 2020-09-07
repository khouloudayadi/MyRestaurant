package com.example.androidmyrestaurant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.example.androidmyrestaurant.Service.MyFirebaseMessagingService;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI myRestaurantAPI;
    AlertDialog dialog;

    private static final int APP_REQUEST_CODE = 1234;

    @BindView(R.id.btn_sign_in)
         Button btn_sign_in;

    @OnClick(R.id.btn_sign_in)
         void loginUser(){
        Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder builder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,builder.build()); //{CLE,VALEUR}
        startActivityForResult(intent,APP_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE){
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

            if(loginResult.getError() != null){
              String toastMsg = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(this,toastMsg,Toast.LENGTH_SHORT).show();
                return;
            }
            else if (loginResult.wasCancelled()) {
                Toast.makeText(this, R.string.login_cancelled, Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                dialog.show();
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                       //save fbid
                        Paper.book().write(Common.REMEMBER_FBID,account.getId());
                        //get token notification
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnFailureListener(new OnFailureListener(){
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this,"[Get Token]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>(){
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        compositeDisposable.add(myRestaurantAPI.updateTokenServer(Common.API_KEY,account.getId(),task.getResult().getToken())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(tokenModel -> {
                                                            if (!tokenModel.isSuccess())
                                                                Toast.makeText(MainActivity.this,"[Update Token Result]"+tokenModel.getMessage(),Toast.LENGTH_SHORT).show();


                                                            compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY,account.getId())
                                                                 .subscribeOn(Schedulers.io())
                                                                 .observeOn(AndroidSchedulers.mainThread())
                                                                 .subscribe(userModel -> {
                                                                                 if(userModel.isSuccess()){  //if user is available in database , start MaineActivity for homeActivity
                                                                                     Common.currentUser = userModel.getResult().get(0);
                                                                                     Intent home = new Intent(MainActivity.this,HomeActivity.class);
                                                                                     startActivity(home);
                                                                                     finish();
                                                                                 }
                                                                                 else{//if user not available in database, start updateActivity for register information
                                                                                     Intent update = new Intent(MainActivity.this,UpdateInfoActivity.class);
                                                                                     startActivity(update);
                                                                                     finish();
                                                                                 }
                                                                            dialog.dismiss();
                                                                            },
                                                                             throwable -> {
                                                                                 dialog.dismiss();
                                                                                 Toast.makeText(MainActivity.this,"[GET USER API]" +throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                                                             })
                                                            );


                                                        },
                                                        throwable -> {
                                                            Toast.makeText(MainActivity.this,"[Update Token]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                                        })
                                        );
                                    }
                                });

                     }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(MainActivity.this,"[Account Kit Error]"+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }


        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();

    }

    private void init() {
        Paper.init(this);
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
    }
}
