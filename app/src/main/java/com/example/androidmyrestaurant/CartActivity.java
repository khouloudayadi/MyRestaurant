package com.example.androidmyrestaurant;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidmyrestaurant.Adapter.MyCartAdapter;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.DataBase.cartDataBase;
import com.example.androidmyrestaurant.DataBase.localCartDataSource;
import com.example.androidmyrestaurant.EventBus.SendTotalCashEvent;
import com.example.androidmyrestaurant.EventBus.calculatePriceEvent;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI myRestaurantAPI;
    AlertDialog dialog;com.example.androidmyrestaurant.DataBase.cartDataSource cartDataSource;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txt_total_cost)
    TextView txt_total_cost;
    @BindView(R.id.btn_order)
    Button btn_order;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        init();
        initView();
        getAllItemInCart();
    }

    private void getAllItemInCart() {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid(),Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    if(cartItems.isEmpty()){
                        btn_order.setText(getString(R.string.cart_empty));
                        btn_order.setEnabled(false);
                        btn_order.setBackgroundResource(android.R.color.darker_gray);
                    }
                    else{
                        btn_order.setText(getString(R.string.order));
                        btn_order.setEnabled(true);
                        btn_order.setBackgroundResource(R.color.button_color);

                        MyCartAdapter adapter =new MyCartAdapter(CartActivity.this,cartItems);
                        recycler_cart.setAdapter(adapter);

                        calculateCartToastPrice();
                    }
                },throwable -> {
                    Toast.makeText(CartActivity.this,"[Get Cart]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.i("cart",throwable.getMessage());

                }));
    }

    private void calculateCartToastPrice() {
        cartDataSource.sumPrice(Common.currentUser.getFbid(),Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(Long aLong) {
                        if(aLong == 0){
                            btn_order.setText(getString(R.string.cart_empty));
                            btn_order.setEnabled(false);
                            btn_order.setBackgroundResource(android.R.color.darker_gray);
                        }
                        else{
                            btn_order.setText(getString(R.string.order));
                            btn_order.setEnabled(true);
                            btn_order.setBackgroundResource(R.color.button_color);
                        }
                        txt_total_cost.setText(String.valueOf(aLong));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e.getMessage().contains("Query returned empty"))
                            txt_total_cost.setText("0");
                        else
                            Toast.makeText(CartActivity.this,"[Sum price]"+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void init() {
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
        cartDataSource = new localCartDataSource(cartDataBase.getInstance(this).cartDAO());
    }

    private void initView() {
        ButterKnife.bind(this);
        toolbar.setTitle(getString(R.string.cart));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);// init linear layout manager
        recycler_cart.setLayoutManager(layoutManager);//attach linear to recycleview
        recycler_cart.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));

        btn_order.setOnClickListener(view -> {
            EventBus.getDefault().postSticky(new SendTotalCashEvent(txt_total_cost.getText().toString()));
            startActivity(new Intent(CartActivity.this,PlaceOrderActivity.class));
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void calculatePrice(calculatePriceEvent event){
        if (event == null){
            calculateCartToastPrice();
        }

    }
}
