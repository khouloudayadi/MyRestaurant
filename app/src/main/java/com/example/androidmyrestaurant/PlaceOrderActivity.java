package com.example.androidmyrestaurant;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import android.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.DataBase.cartDataBase;
import com.example.androidmyrestaurant.DataBase.cartDataSource;
import com.example.androidmyrestaurant.DataBase.localCartDataSource;
import com.example.androidmyrestaurant.EventBus.SendTotalCashEvent;
import com.example.androidmyrestaurant.Retrofit.IBraintreeAPI;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitBraintreeClient;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicReference;


public class PlaceOrderActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final int REQUEST_BRAINTREE_CODE = 7777;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI myRestaurantAPI;
    IBraintreeAPI myBraintreeAPI;
    cartDataSource cartDataSource;
    AlertDialog dialog;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.edt_date)
    EditText edt_date;
    @BindView(R.id.txt_user_phone)
    TextView txt_user_phone;
    @BindView(R.id.txt_total_cash)
    TextView txt_total_cash;
    @BindView(R.id.txt_user_address)
    TextView txt_user_address;
    @BindView(R.id.txt_new_address)
    TextView txt_new_address;
    @BindView(R.id.btn_add_new_address)
    Button btn_add_new_address;
    @BindView(R.id.ckb_default_address)
    CheckBox ckb_default_address;
    @BindView(R.id.rdi_cod)
    RadioButton rdi_cod;
    @BindView(R.id.rdi_online_payment)
    RadioButton rdi_online_payment;
    @BindView(R.id.btn_proceed)
    Button btn_proceed;

boolean isSelectedDate=false, isNewAddress=false;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

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
        ButterKnife.bind(this);

        txt_user_phone.setText(Common.currentUser.getUserPhone());
        txt_user_address.setText(Common.currentUser.getAddress());

        toolbar.setTitle(R.string.place_order);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_add_new_address.setOnClickListener(view -> {
             isNewAddress =true;
             ckb_default_address.setChecked(false);
             View layout_add_new_address = LayoutInflater.from(PlaceOrderActivity.this).inflate(R.layout.layout_add_new_address,null);
             EditText edt_new_adress = (EditText) layout_add_new_address.findViewById(R.id.edt_add_new_address);
             edt_new_adress.setText(txt_new_address.getText().toString().trim());

            androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(PlaceOrderActivity.this)
                    .setTitle(R.string.add_new_address)
                    .setView(layout_add_new_address)
                    .setNegativeButton("CANCEL",((dialogInterface, i) -> dialogInterface.dismiss()))
                    .setPositiveButton("ADD",((dialogInterface, i) -> txt_new_address.setText(edt_new_adress.getText().toString())));
            androidx.appcompat.app.AlertDialog addNewAddressDialog =builder.create();
            addNewAddressDialog.show();
        });

        btn_proceed.setOnClickListener(view -> {
            if (!isSelectedDate){
                Toast.makeText(PlaceOrderActivity.this,R.string.toast_date,Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNewAddress)
            {
                if(!ckb_default_address.isChecked()){
                    Toast.makeText(PlaceOrderActivity.this,R.string.toast_address,Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if(rdi_cod.isChecked()){
                //process cod
                getOrderNumber(false);
            }
            else if(rdi_online_payment.isChecked()){
                //process online payment
                getOrderNumber(true);
            }


        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_BRAINTREE_CODE){
            if(resultCode == RESULT_OK){
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
               if(!TextUtils.isEmpty(txt_total_cash.getText().toString())){
                   String amount = txt_total_cash.getText().toString();
                   if(dialog.isShowing()){
                       dialog.dismiss();
                   }
                   String address = ckb_default_address.isChecked() ? txt_user_address.getText().toString():txt_new_address.getText().toString();
                   compositeDisposable.add(myBraintreeAPI.submitPayment(amount,nonce.getNonce())
                           .subscribeOn(Schedulers.io())
                           .observeOn(AndroidSchedulers.mainThread())
                           .subscribe(BraintreeTransaction ->{
                                if (BraintreeTransaction.isSuccess()){
                                    if(!dialog.isShowing()){
                                        dialog.show();

                                        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid(),Common.currentRestaurant.getId())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(cartItems ->{

                                                            //get order number from server
                                                            compositeDisposable.add(myRestaurantAPI.createOrder(Common.API_KEY,
                                                                    Common.currentUser.getFbid(),
                                                                    Common.currentUser.getUserPhone(),
                                                                    Common.currentUser.getName(),
                                                                    address,
                                                                    edt_date.getText().toString(),
                                                                    Common.currentRestaurant.getId(),
                                                                    BraintreeTransaction.getTransaction().getId(),
                                                                    "0",
                                                                    Double.valueOf(amount),
                                                                    cartItems.size())
                                                                    .subscribeOn(Schedulers.io())
                                                                    .observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(CreateOrderModel->{
                                                                                if(CreateOrderModel.isSuccess()){
                                                                                    //Toast.makeText(PlaceOrderActivity.this,"[Create order success]",Toast.LENGTH_SHORT).show();
                                                                                    //after have order number, we will update all item of this order to order to orderDetail
                                                                                    //clear cart
                                                                                    //clear cart
                                                                                    cartDataSource.cleanCart(Common.currentUser.getFbid(),Common.currentRestaurant.getId())
                                                                                            .subscribeOn(Schedulers.io())
                                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                                            .subscribe(new SingleObserver<Integer>() {
                                                                                                           @Override
                                                                                                           public void onSubscribe(Disposable d) {

                                                                                                           }

                                                                                                           @Override
                                                                                                           public void onSuccess(Integer integer) {
                                                                                                               Toast.makeText(PlaceOrderActivity.this,R.string.OrderPlaced,Toast.LENGTH_SHORT).show();
                                                                                                               Intent HomeActivity = new Intent(PlaceOrderActivity.this, com.example.androidmyrestaurant.HomeActivity.class);
                                                                                                               HomeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                               startActivity(HomeActivity);
                                                                                                               finish();
                                                                                                           }

                                                                                                           @Override
                                                                                                           public void onError(Throwable e) {
                                                                                                               Toast.makeText(PlaceOrderActivity.this,"[Clear Cart]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                                                                                           }
                                                                                                       }
                                                                                            );
                                                                                }
                                                                                else{
                                                                                    Toast.makeText(PlaceOrderActivity.this,"[Create Order Result]"+CreateOrderModel.getMessage(),Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            },
                                                                            throwable -> {
                                                                                dialog.dismiss();
                                                                                Toast.makeText(PlaceOrderActivity.this, "[Create Order]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            })
                                                            );
                                                            dialog.dismiss();
                                                        },
                                                        throwable -> {
                                                            dialog.dismiss();
                                                            Toast.makeText(PlaceOrderActivity.this,"[Get Cart]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                                        })
                                        );
                                    }
                                }
                                else{
                                    Toast.makeText(PlaceOrderActivity.this, R.string.TransactionFailed, Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                                   }
                           ,throwable -> {
                                       Toast.makeText(PlaceOrderActivity.this, "[Submit Payment]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                   }));
               }
            }
        }
    }


    private void getOrderNumber(boolean isOnlinePayment) {
        dialog.show();
        if(!isOnlinePayment){
            String address = ckb_default_address.isChecked() ? txt_user_address.getText().toString() :txt_new_address.getText().toString();
            compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid(),Common.currentRestaurant.getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(cartItems ->{
                //get order number from server
                        compositeDisposable.add(myRestaurantAPI.createOrder(Common.API_KEY,
                                    Common.currentUser.getFbid(),
                                    Common.currentUser.getUserPhone(),
                                    Common.currentUser.getName(),
                                    address,
                                    edt_date.getText().toString(),
                                    Common.currentRestaurant.getId(),
                                    "none",
                                    "1",
                                    Double.valueOf(txt_total_cash.getText().toString()),
                                    cartItems.size())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(CreateOrderModel->{
                                                if(CreateOrderModel.isSuccess()){
                                                    //clar cart
                                                    cartDataSource.cleanCart(Common.currentUser.getFbid(),Common.currentRestaurant.getId())
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(new SingleObserver<Integer>() {
                                                                           @Override
                                                                           public void onSubscribe(Disposable d) {
                                                                           }

                                                                           @Override
                                                                           public void onSuccess(Integer integer) {
                                                                               Toast.makeText(PlaceOrderActivity.this,R.string.OrderPlaced,Toast.LENGTH_SHORT).show();
                                                                               Intent HomeActivity = new Intent(PlaceOrderActivity.this, com.example.androidmyrestaurant.HomeActivity.class);
                                                                               HomeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                               startActivity(HomeActivity);
                                                                               finish();
                                                                           }

                                                                           @Override
                                                                           public void onError(Throwable e) {
                                                                               Toast.makeText(PlaceOrderActivity.this,"[Clear Cart]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                                                           }
                                                                       }
                                                            );
                                                }
                                                if (dialog.isShowing())
                                                    dialog.dismiss();
                                            },
                                            throwable -> {
                                                dialog.dismiss();
                                                Toast.makeText(PlaceOrderActivity.this, "[Create order]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.i("Create order",throwable.getMessage());
                                            })
                        );
             dialog.dismiss();
             },
             throwable -> {
                dialog.dismiss();
                Toast.makeText(PlaceOrderActivity.this,"[Get Cart]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
             })
            );
        }
        else{
            //if OnlinePayment
            //first get token
            compositeDisposable.add(myBraintreeAPI.getToken()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(BraintreeToken ->{
                if(BraintreeToken.isSuccess()){
                    DropInRequest dropInRequest = new DropInRequest().clientToken(BraintreeToken.getClientToken());
                    startActivityForResult(dropInRequest.getIntent(PlaceOrderActivity.this),REQUEST_BRAINTREE_CODE);
                }
                else{
                    Toast.makeText(PlaceOrderActivity.this,"[Cannont Get Token]",Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                    }
            ,throwable -> {
                dialog.dismiss();
                Toast.makeText(PlaceOrderActivity.this,"[Get Token]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
             }
             ));
        }
    }

    private void init() {
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
        myBraintreeAPI = RetrofitBraintreeClient.getInstance(Common.currentRestaurant.getPaymentUrl()).create(IBraintreeAPI.class);
        cartDataSource = new localCartDataSource(cartDataBase.getInstance(this).cartDAO());
    }

    public void DatePicker(View view) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(PlaceOrderActivity.this,
                now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show(getSupportFragmentManager(),"DatePickerDialog");

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        isSelectedDate =true;
        edt_date.setText(new StringBuilder("")
                .append(dayOfMonth)
                .append("/")
                .append(monthOfYear + 1)
                .append("/")
                .append(year));
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void sendTotalCash(SendTotalCashEvent event){
        txt_total_cash.setText(String.valueOf(event.getCash()));
    }
}
