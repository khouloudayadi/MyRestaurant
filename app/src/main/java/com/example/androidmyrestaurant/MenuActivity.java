package com.example.androidmyrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.androidmyrestaurant.Adapter.MyMenuAdapter;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.DataBase.CartDAO;
import com.example.androidmyrestaurant.DataBase.cartDataBase;
import com.example.androidmyrestaurant.DataBase.cartDataSource;
import com.example.androidmyrestaurant.DataBase.localCartDataSource;
import com.example.androidmyrestaurant.EventBus.MenuItemEvent;
import com.example.androidmyrestaurant.Model.FavoriteOnlyIdModel;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nex3z.notificationbadge.NotificationBadge;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI myRestaurantAPI;
    AlertDialog dialog;


    @BindView(R.id.img_restaurant)
    ImageView img_restaurant;
    @BindView(R.id.recycler_menu)
    RecyclerView recycler_menu;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton btn_cart;
    @BindView(R.id.badge)
    NotificationBadge badge;

    MyMenuAdapter adapter;
    cartDataSource  cartDataSource;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        countCartByRestaurant();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        init();
        initView();
        countCartByRestaurant();
        loadFavoriteByRestaurant();
    }

    private void loadFavoriteByRestaurant() {
        compositeDisposable.add(myRestaurantAPI.getfavoriteByRestaurant(Common.API_KEY,Common.currentUser.getFbid(),Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(FavoriteOnlyIdModel->{
                    if(FavoriteOnlyIdModel.isSuccess()){
                        if(FavoriteOnlyIdModel != null && FavoriteOnlyIdModel.getResult().size()>0){

                            Common.currentFavOfRestaurant = FavoriteOnlyIdModel.getResult();
                        }
                        else{
                            Common.currentFavOfRestaurant = new ArrayList<>();
                        }

                    }
                    else{
                        Toast.makeText(MenuActivity.this,"[Get favoriteByResto result]"+FavoriteOnlyIdModel.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                        },
                        throwable -> {
                            Toast.makeText(MenuActivity.this,"[Get favorite]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void countCartByRestaurant() {
    cartDataSource.countItemCart(Common.currentUser.getFbid(),Common.currentRestaurant.getId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleObserver<Integer>() {
                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onSuccess(Integer integer) {
                    badge.setText(String.valueOf(integer));
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(MenuActivity.this,"[count cart]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.i("[count cart]",e.getMessage());
                }
            });
    }

    private void initView() {
        ButterKnife.bind(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this,2);// init grid layout manager
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(adapter != null){
                    switch (adapter.getItemViewType(position))
                    {
                        case Common.DEFAULT_COLUMN_COUNT :return 1 ;
                        case Common.FULL_WIDTH_COLUMN :return 2;
                        default:return -1;
                    }
                }
                return -1;
            }
        });
        recycler_menu.setLayoutManager(layoutManager);

        btn_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(MenuActivity.this,CartActivity.class));
            }
        });
    }

    private void init() {
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
        cartDataSource = new localCartDataSource(cartDataBase.getInstance(this).cartDAO());
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
        super.onStart();
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void loadMenuByRestaurant(MenuItemEvent event)
    {
        if(event.isSuccess())
        {
            Picasso.get().load(Common.URL_IMG+event.getRestaurant().getImage()).into(img_restaurant);
            toolbar.setTitle(event.getRestaurant().getName());
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            dialog.show();
            compositeDisposable.add(myRestaurantAPI.getmenyByRestaurant(Common.API_KEY,event.getRestaurant().getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MenuModel -> {
                                adapter = new MyMenuAdapter(MenuActivity.this , MenuModel.getResult());
                                recycler_menu.setAdapter(adapter);
                                dialog.dismiss();
                            },
                            throwable -> {
                            dialog.dismiss();
                                Toast.makeText(MenuActivity.this,"[Get Menu]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                            })
            );
        }


    }
}
