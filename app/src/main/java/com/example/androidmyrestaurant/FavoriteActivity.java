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
import android.widget.Toast;
import android.app.AlertDialog;
import com.example.androidmyrestaurant.Adapter.MyFavoriteAdapter;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;

public class FavoriteActivity extends AppCompatActivity {
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI iMyRestaurantAPI;
    AlertDialog dialog;

    MyFavoriteAdapter adapter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_food_favorite)
    RecyclerView recycler_food_favorite;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        init();
        initView();
        loadFavorite();
    }

    private void loadFavorite() {
        toolbar.setTitle(R.string.nav_favorite);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dialog.show();
        compositeDisposable.add(iMyRestaurantAPI.getfavoriteByUser(Common.API_KEY,Common.currentUser.getFbid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(FavoriteModel->{
                            if (FavoriteModel.isSuccess()){
                                adapter = new MyFavoriteAdapter(FavoriteActivity.this,FavoriteModel.getResult());
                                recycler_food_favorite.setAdapter(adapter);
                            }
                            else{
                                Toast.makeText(FavoriteActivity.this,"[get favorite result]"+FavoriteModel.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        },
                        throwable -> {
                            dialog.dismiss();
                            Toast.makeText(FavoriteActivity.this,"[get favorite]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        })
        );

    }

    private void initView() {
        ButterKnife.bind(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);// init linear layout manager
        recycler_food_favorite.setLayoutManager(layoutManager);//attach linear to recycleview
        recycler_food_favorite.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));
    }

    private void init() {
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iMyRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
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
