package com.example.androidmyrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.Toast;

import com.example.androidmyrestaurant.Adapter.MyOrderAdapter;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Model.OrderModel;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;


public class ViewOrderActivity extends AppCompatActivity {
    @BindView(R.id.recycler_view_order)
    RecyclerView recycler_view_order;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI myRestaurantAPI;
    android.app.AlertDialog dialog;


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        init();
        initView();
        getAllOrder();
    }

    private void getAllOrder() {
        dialog.show();
        compositeDisposable.add(myRestaurantAPI.getOrder(Common.API_KEY,Common.currentUser.getFbid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(OrderModel ->{
            if(OrderModel.isSuccess()){
                if(OrderModel.getResult().size()>0){
                    //create Adapter
                    MyOrderAdapter adapter = new MyOrderAdapter(ViewOrderActivity.this , OrderModel.getResult());
                    recycler_view_order.setAdapter(adapter);
                }
            }
            else{
                Toast.makeText(ViewOrderActivity.this,"[Get Order Result]"+OrderModel.getMessage(),Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
                }
        ,throwable -> {
            dialog.dismiss();
                    Toast.makeText(ViewOrderActivity.this,"[Get Order]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                }));
    }

    private void initView() {
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);// init linear layout manager
        recycler_view_order.setLayoutManager(layoutManager);//attach linear to recycleview
        recycler_view_order.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));

        toolbar.setTitle(R.string.your_order);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}