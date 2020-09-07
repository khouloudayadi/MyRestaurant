package com.example.androidmyrestaurant.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.EventBus.sizeLoadEvent;
import com.example.androidmyrestaurant.Interface.IOnRecyclerViewClickListener;
import com.example.androidmyrestaurant.Model.Favorite;
import com.example.androidmyrestaurant.Model.Food;
import com.example.androidmyrestaurant.Model.Size;
import com.example.androidmyrestaurant.R;
import com.example.androidmyrestaurant.Retrofit.IMyRestaurantAPI;
import com.example.androidmyrestaurant.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyFavoriteAdapter extends RecyclerView.Adapter<MyFavoriteAdapter.MyViewHolder> {
    Context context;
    List<Favorite> favoriteList;

    CompositeDisposable compositeDisposable;
    IMyRestaurantAPI iMyRestaurantAPI;


    private Double sizePrice = 0.0;
    private String sizeSelected;
    double extraPrice;
    double originalPrice;
    Food selectedFood;

    //init view
    TextView txt_money_food,txt_description_food,txt_discount_food;
    ImageView img_food_detail;
    RadioGroup radio_group_size;

    public MyFavoriteAdapter(Context context, List<Favorite> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;
        compositeDisposable = new CompositeDisposable();
        iMyRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item_favorite = LayoutInflater.from(context).inflate(R.layout.layout_favorite,parent,false);
        return new MyViewHolder(item_favorite);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Picasso.get().load(Common.URL_IMG+favoriteList.get(position).getFoodImage())
                .placeholder(R.drawable.app_icon)
                .into(holder.img_food);
        holder.txt_name_food.setText(favoriteList.get(position).getFoodName());
        holder.txt_price_food.setText(new StringBuilder(context.getString(R.string.money_sign)).append(favoriteList.get(position).getPrice()));
        holder.txt_name_restaurant.setText(favoriteList.get(position).getRestaurantName());
        holder.setListener(new IOnRecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
              // Toast.makeText(context,favoriteList.get(position).getFoodName(),Toast.LENGTH_SHORT).show();
                compositeDisposable = new CompositeDisposable();
                iMyRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
                compositeDisposable.add(iMyRestaurantAPI.getfoodById(Common.API_KEY,favoriteList.get(position).getFoodId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(FoodModel->{
                                    View food_detail = LayoutInflater.from(context)
                                            .inflate(R.layout.layout_food_detail,null);
                                    img_food_detail =(ImageView)food_detail.findViewById(R.id.img_food_detail);
                                    txt_money_food =(TextView)food_detail.findViewById(R.id.txt_money_food);
                                    txt_description_food =(TextView)food_detail.findViewById(R.id.txt_description_food);
                                    txt_discount_food =(TextView)food_detail.findViewById(R.id.txt_discount_food);
                                    radio_group_size =(RadioGroup)food_detail.findViewById(R.id.radio_group_size);

                                    selectedFood = FoodModel.getResult().get(0);
                                    originalPrice = selectedFood.getPrice();
                                    txt_money_food.setText(String.valueOf(originalPrice));
                                    txt_description_food.setText(selectedFood.getDescription());
                                    txt_discount_food.setText(String.valueOf(selectedFood.getDiscount()));
                                    Picasso.get().load(Common.URL_IMG+selectedFood.getImage()).into(img_food_detail);

                                    if (selectedFood.isSize()){
                                        //load size
                                        compositeDisposable.add(iMyRestaurantAPI.getSizeOfFood(Common.API_KEY,selectedFood.getId())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(sizeModel -> {
                                                            EventBus.getDefault().post(new sizeLoadEvent(true,sizeModel.getResult()));
                                                         },
                                                        throwable -> {
                                                            Toast.makeText(context,"[Get Size]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                                            Log.i("[Get Size]",throwable.getMessage());
                                                        }
                                                )
                                        );
                                    }

                                    new AlertDialog.Builder(context)
                                            .setView(food_detail)
                                            .setCancelable(true)
                                            .show();
                                },
                                throwable -> {
                                    Toast.makeText(context,"[get food]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                        )
                );

            }
        });

    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_name_food)
        TextView txt_name_food;
        @BindView(R.id.txt_name_restaurant)
        TextView txt_name_restaurant;
        @BindView(R.id.txt_price_food)
        TextView txt_price_food;
        @BindView(R.id.img_food)
        ImageView img_food;




        IOnRecyclerViewClickListener listener;
        public void setListener(IOnRecyclerViewClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            listener.onClick(view,getAdapterPosition());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void displaySize(sizeLoadEvent event){
        if(event.isSuccess())
        {
            //create radio Button base on size length
            for (Size size : event.getSizeList())
            {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if(b){
                            sizePrice = size.getExtraPrice();}
                        else
                            sizePrice = -size.getExtraPrice();
                        calculatePrice();
                        sizeSelected = size.getDescription();
                    }
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,0.1f);
                radioButton.setLayoutParams(params);
                radioButton.setText(size.getDescription());
                radioButton.setTag(size.getExtraPrice());
                radio_group_size.addView(radioButton);
            }
        }
    }

    private void calculatePrice() {

        extraPrice = 0.0;
        double newPrice;
        extraPrice += sizePrice;
        newPrice = originalPrice *= extraPrice;

        txt_money_food.setText(String.valueOf(newPrice));
    }

}

