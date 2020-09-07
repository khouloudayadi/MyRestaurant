package com.example.androidmyrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;

public class UpdateInfoActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI myRestaurantAPI;
    AlertDialog dialog;

    @BindView(R.id.edt_user_name) EditText edt_user_name;
    @BindView(R.id.edt_user_address) EditText edt_user_address;
    @BindView(R.id.btn_update) Button btn_update ;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        ButterKnife.bind(this);
        init();
        initView();
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void initView() {
        toolbar.setTitle(getString(R.string.my_profile));
        setSupportActionBar(toolbar);//récupérer une instance de ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        compositeDisposable.add(myRestaurantAPI.updateUserInfo(Common.API_KEY,
                                account.getPhoneNumber().toString(),
                                edt_user_name.getText().toString(),
                                edt_user_address.getText().toString(),
                                account.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(updateUserModel -> {
                                        if (updateUserModel.isSuccess()){
                                            //if user has been update, just refresh again
                                            compositeDisposable.add(myRestaurantAPI.getUser(Common.API_KEY, account.getId())
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(userModel -> {
                                                        if (userModel.isSuccess()){
                                                            Common.currentUser = userModel.getResult().get(0);
                                                            startActivity(new Intent(UpdateInfoActivity.this,HomeActivity.class));
                                                            finish();
                                                        }
                                                        else {
                                                            Toast.makeText(UpdateInfoActivity.this,"[Get User result]"+userModel.getMessage(),Toast.LENGTH_SHORT).show();
                                                        }
                                                        dialog.dismiss();
                                                            },
                                                            throwable -> {
                                                                dialog.dismiss();
                                                                Toast.makeText(UpdateInfoActivity.this,"[Get User]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();

                                                            })
                                            );
                                        }
                                        else {
                                            dialog.dismiss();
                                            Toast.makeText(UpdateInfoActivity.this,"[Update User API Return]"+updateUserModel.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                        },
                                        throwable -> {
                                            dialog.dismiss();
                                            Toast.makeText(UpdateInfoActivity.this,"[Update user]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                        })
                        );
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(UpdateInfoActivity.this,"[Account kit Error]"+accountKitError.getErrorType().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        if( Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.getName()))
            edt_user_name.setText(Common.currentUser.getName());
        if( Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.getAddress()))
            edt_user_address.setText(Common.currentUser.getAddress());
    }
    public boolean onOptionItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void init() {
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
    }
}
