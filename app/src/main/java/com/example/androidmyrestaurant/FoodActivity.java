package com.example.androidmyrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidmyrestaurant.Adapter.MyFoodAdapter;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.EventBus.FoodItemEvent;
import com.example.androidmyrestaurant.Model.FoodModel;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FoodActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI myRestaurantAPI;
    AlertDialog dialog;
    int menu_selected;

    @BindView(R.id.img_menu_food)
    ImageView img_menu_food;
    @BindView(R.id.recycler_food)
    RecyclerView recycler_food;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txt_category_food)
    TextView txt_menu_food;

    MyFoodAdapter adapter,searchAdapter;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
          if(adapter != null){
              adapter.onStop();
          }
          if(searchAdapter != null){
             searchAdapter.onStop();
          }
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater search_food = getMenuInflater();
        search_food.inflate(R.menu.menu_search,menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager=(SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        //EVENT
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearchFood(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                //Restore to original adapter when use close Search
                recycler_food.setAdapter(adapter);
                return true;
            }
        });
        return true;
    }

    private void startSearchFood(String query_name_Food) {
        dialog.show();
        compositeDisposable.add(myRestaurantAPI.searchFood(Common.API_KEY,query_name_Food,menu_selected)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(FoodModel-> {
                        if (FoodModel.isSuccess()){
                            searchAdapter = new MyFoodAdapter(FoodActivity.this, FoodModel.getResult());
                            recycler_food.setAdapter(searchAdapter);
                        }
                        else{
                            if(FoodModel.getMessage().equals("Empty"))
                                recycler_food.setAdapter(null);
                            Toast.makeText(FoodActivity.this,"Food Not Found",Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                        },
                        throwable -> {
                             dialog.dismiss();
                             Toast.makeText(FoodActivity.this,"[search food]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        })
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        init();
        initView();
    }

    private void initView() {
        ButterKnife.bind(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);// init linear layout manager
        recycler_food.setLayoutManager(layoutManager);//attach linear to recycleview
        recycler_food.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));
    }

    private void init() {
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
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


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void loadfoodByMenu(FoodItemEvent event){
        if(event.isSuccess())
        {
            menu_selected = event.getMenulist().getId();
            Picasso.get().load(Common.URL_IMG + event.getMenulist().getImage()).into(img_menu_food);//event.getMenulist().getImage()
            txt_menu_food.setText(event.getMenulist().getName());


            toolbar.setTitle(event.getMenulist().getName());
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            dialog.show();

            compositeDisposable.add(myRestaurantAPI.getfoodByMenu(Common.API_KEY,event.getMenulist().getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(FoodModel -> {
                            if (FoodModel.isSuccess()) {
                                adapter = new MyFoodAdapter(FoodActivity.this, FoodModel.getResult());
                                recycler_food.setAdapter(adapter);
                            }
                            else{
                                Toast.makeText(FoodActivity.this,"[Get food result]"+FoodModel.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        dialog.dismiss();
                            },
                            throwable -> {
                                dialog.dismiss();
                                Toast.makeText(FoodActivity.this,"[Get food]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                            }

                    ));
        }


    }
}
