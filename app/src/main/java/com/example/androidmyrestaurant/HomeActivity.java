package com.example.androidmyrestaurant;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.androidmyrestaurant.Adapter.MyRestaurantAdapter;
import com.example.androidmyrestaurant.Adapter.RestaurantSliderAdapter;
import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.EventBus.RestaurantLoadEvent;
import com.example.androidmyrestaurant.Model.Restaurant;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.example.androidmyrestaurant.Service.PicassoImageLoadingService;
import com.facebook.accountkit.AccountKit;

import android.util.Log;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
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
import ss.com.bannerslider.Slider;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView txt_user_name,txt_user_phone;
    @BindView(R.id.slider_restaurant) Slider slider_restaurant;
    @BindView(R.id.recycler_restaurant) RecyclerView recycler_restaurant;

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
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        txt_user_name = (TextView)headerView.findViewById(R.id.text_user_name);
        txt_user_phone = (TextView)headerView.findViewById(R.id.text_user_phone);
        txt_user_phone.setText(Common.currentUser.getUserPhone());
        txt_user_name.setText(Common.currentUser.getName());

        init();
        initView();
        loadRestaurant();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_contact_us) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_nearby) {
            startActivity(new Intent(HomeActivity.this,NearbyRestaurantActivity.class) );
        } else if (id == R.id.nav_order_history) {
            startActivity(new Intent(HomeActivity.this,ViewOrderActivity.class) );
        } else if (id == R.id.nav_update_info) {
             startActivity(new Intent(HomeActivity.this,UpdateInfoActivity.class) );
        } else if (id == R.id.nav_sign_out) {
             signOut();
        } else if (id == R.id.nav_share) { 
             share();
        } else if (id == R.id.nav_favorite) {
             startActivity(new Intent(HomeActivity.this,FavoriteActivity.class) );
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void share() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Restaurant");
            String shareMessage = "\n My Restaurant \n Est une application mobile conçues pour faciliter\n" + "la commande de plats dans divers restaurants..." ;
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            e.toString();
        }
    }

    private void initView() {

        ButterKnife.bind(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);// init linear layout manager
        recycler_restaurant.setLayoutManager(layoutManager);//attach linear to recycleview
        recycler_restaurant.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));
    }

    private void init() {
        dialog =  new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
        Slider.init(new PicassoImageLoadingService());//spécifiez service de chargement d'images
    }

    private void loadRestaurant() {
        dialog.show();
        compositeDisposable.add(myRestaurantAPI.getRestaurant(Common.API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(RestaurantModel -> {//Lancer un événement
                            //EventBus simplifie la communication entre les activités, les fragments, les threads et les services, avec moins de code
                            EventBus.getDefault().post(new RestaurantLoadEvent(true,RestaurantModel.getResult()));
                        },
                        throwable -> {
                            EventBus.getDefault().post(new RestaurantLoadEvent(false,throwable.getMessage()));
                        }
                )
        );

    }

    private void signOut() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_alert_sign_up)
                .setMessage(R.string.msg_alert_sign_up)
                .setPositiveButton(R.string.ok, (dialog, i) -> {
                    Common.currentUser = null;
                    Common.currentRestaurant = null;
                    AccountKit.logOut();
                    Intent logout = new Intent(HomeActivity.this,MainActivity.class);
                    logout.addFlags(logout.FLAG_ACTIVITY_CLEAR_TASK);//Closing all the Activities
                    startActivity(logout);
                    finish();
                })

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }
    //register EventBus
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }
    //unregister EventBus
    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN) //listen EventBus
    public void processRestaurantLoadEvent(RestaurantLoadEvent event){
        if (event.isSuccess()){
           displayBanner(event.getRestaurantList());
           displayRestaurant(event.getRestaurantList());
        }
        else{

            Toast.makeText(HomeActivity.this,"[restaurant load]"+event.getMessage(),Toast.LENGTH_SHORT).show();
            Log.i("[restaurant load]",event.getMessage());
        }
        dialog.dismiss();
    }

    private void displayRestaurant(List<Restaurant> restaurantList) {
        MyRestaurantAdapter adapter = new MyRestaurantAdapter(this,restaurantList);
        recycler_restaurant.setAdapter(adapter);
    }

    private void displayBanner(List<Restaurant> restaurantList) {
     slider_restaurant.setAdapter(new RestaurantSliderAdapter(restaurantList));
    // slider_restaurant.setSelectedSlide(2);
    }
}
