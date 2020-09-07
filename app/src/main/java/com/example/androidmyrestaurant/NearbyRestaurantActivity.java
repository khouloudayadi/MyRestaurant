package com.example.androidmyrestaurant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import android.app.AlertDialog;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.EventBus.MenuItemEvent;
import com.example.androidmyrestaurant.Model.Restaurant;
import com.example.androidmyrestaurant.Model.RestaurantModel;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class NearbyRestaurantActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyRestaurantAPI myRestaurantAPI;
    AlertDialog dialog;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;



    Marker userMarker;

    boolean isFirstLoad = false;


    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_restaurant);

        init();
        initView();
    }

    private void initView() {
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar.setTitle(getString(R.string.nearby_restaurant));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                addMarkerAndMoveCamera(locationResult.getLastLocation());
                if(!isFirstLoad){
                    isFirstLoad = !isFirstLoad;
                  //  requestNearByRestaurant(locationResult.getLastLocation().getLatitude(),
                          //  locationResult.getLastLocation().getLongitude(),10);
                   requestNearByRestaurant(30.32807,-81.485451,10);
                }

            };
        };
    }

    private void requestNearByRestaurant(double latitude, double longitude, int distance) {
        dialog.show();
        compositeDisposable.add(myRestaurantAPI.nearByRestaurant(Common.API_KEY,latitude,longitude,distance)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(RestaurantModel->{
            if (RestaurantModel.isSuccess()){

                addRestaurantMarker(RestaurantModel.getResult());
            }
            else{
                Toast.makeText(NearbyRestaurantActivity.this,"[Get NearByRestaurant Result]"+RestaurantModel.getMessage(),Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
                }
        ,throwable -> {
            dialog.dismiss();
            Toast.makeText(NearbyRestaurantActivity.this,"[Get NearByRestaurant]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
        })
        );
    }

    private void addRestaurantMarker(List<Restaurant> restaurantList) {
        for (Restaurant restaurant:restaurantList){
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_marker))
            .position(new LatLng(restaurant.getLat(),restaurant.getLng()))
            .snippet(new StringBuilder().append(restaurant.getId()).append(".").append(restaurant.getName()).toString())
                    );
        }
    }

    private void addMarkerAndMoveCamera(Location lastLocation) {
        if(userMarker != null)
            userMarker.remove();
        LatLng userLatLng =new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title(Common.currentUser.getName()));
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userLatLng,17);
        mMap.animateCamera(yourLocation);

    }

    private void buildLocationRequest() {
        locationRequest =new LocationRequest();
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setTiltGesturesEnabled(true);

        try{
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style));
            if(!success)
                Log.e("ERROR_MAP","Load style error");

        }
        catch (Resources.NotFoundException e){
            Log.e("Error_MAP","Ressource not found");
        }

        mMap.setOnInfoWindowClickListener(marker -> {
            String id  = marker.getTitle().substring(0,marker.getTitle().indexOf("."));
            if(!TextUtils.isEmpty(id)){
                compositeDisposable.add(myRestaurantAPI.getRestaurantById(Common.API_KEY,id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(RestaurantByIdModel->{
                    Common.currentRestaurant = RestaurantByIdModel.getResult().get(0);
                    EventBus.getDefault().postSticky(new MenuItemEvent(true,Common.currentRestaurant) );
                    startActivity(new Intent(NearbyRestaurantActivity.this,MenuActivity.class));
                    finish();
                        }
                ,throwable -> {
                     Toast.makeText(NearbyRestaurantActivity.this,"[Get Restaurant]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        }));
            }
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
}

