package com.example.androidmyrestaurant.Adapter;


import android.annotation.SuppressLint;
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
import com.example.androidmyrestaurant.DataBase.cartDataBase;
import com.example.androidmyrestaurant.DataBase.cartDataSource;
import com.example.androidmyrestaurant.DataBase.cartItem;
import com.example.androidmyrestaurant.DataBase.localCartDataSource;
import com.example.androidmyrestaurant.EventBus.FoodDetailItemEvent;
import com.example.androidmyrestaurant.EventBus.sizeLoadEvent;
import com.example.androidmyrestaurant.Interface.IFoodInfoOrCartClickListener;
import com.example.androidmyrestaurant.Model.FavoriteModel;
import com.example.androidmyrestaurant.Model.FavoriteOnlyId;
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
public class MyFoodAdapter extends RecyclerView.Adapter<MyFoodAdapter.MyViewHolder> {

    Context context;
    List<Food> foodList;
    CompositeDisposable compositeDisposable;
    cartDataSource  cartDataSource;
    IMyRestaurantAPI myRestaurantAPI;

    private Double sizePrice = 0.0;
    private String sizeSelected;
    double extraPrice;
    double originalPrice;
    Food selectedFood;

    //init view
    TextView  txt_money_food,txt_description_food,txt_discount_food;
    ImageView img_food_detail;
    RadioGroup radio_group_size;



    public  void onStop(){
        compositeDisposable.clear();
    }

    public MyFoodAdapter(Context context, List<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
        compositeDisposable  = new CompositeDisposable();
        cartDataSource = new localCartDataSource(cartDataBase.getInstance(context).cartDAO());
        myRestaurantAPI = RetrofitClient.getInstance(Common.baseUrl).create(IMyRestaurantAPI.class);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View item_food = LayoutInflater.from(context).inflate(R.layout.layout_food,parent,false);
      return new MyViewHolder(item_food);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(Common.URL_IMG+foodList.get(position).getImage())
                .placeholder(R.drawable.app_icon)
                .into(holder.img_food);
        holder.txt_name_food.setText(foodList.get(position).getName());//foodList.get(position).getName()
        holder.txt_price_food.setText(new StringBuilder(context.getString(R.string.money_sign)).append(foodList.get(position).getPrice()));//foodList.get(position).getPrice()


        //CHECK FAVORITE
        if(Common.currentFavOfRestaurant != null && Common.currentFavOfRestaurant.size() >0 )
        {
            if (Common.checkFavorite(foodList.get(position).getId(),Common.currentFavOfRestaurant))
            {
               holder.img_fav_food.setImageResource(R.drawable.ic_action_favorite);
               holder.img_fav_food.setTag(true);
            }
            else
            {
                holder.img_fav_food.setImageResource(R.drawable.ic_favorite_border_24dp);
                holder.img_fav_food.setTag(false);
            }
        }
        else
        {
             // default all food  is no favorite
            holder.img_fav_food.setTag(false);
        }

        //event add or remove favorite
        holder.img_fav_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView fav = (ImageView)view;
                //delete
                if ((boolean)fav.getTag()){
                    compositeDisposable.add(myRestaurantAPI.deleteFavorite(Common.API_KEY,Common.currentUser.getFbid(),foodList.get(position).getId(),Common.currentRestaurant.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(FavoriteModel->{
                                if (FavoriteModel.isSuccess()) {
                                  fav.setImageResource(R.drawable.ic_favorite_border_24dp);
                                  fav.setTag(false);
                                  if(Common.currentFavOfRestaurant != null)
                                      Common.removeFavorite(foodList.get(position).getId(),Common.currentFavOfRestaurant);
                                }
                                else {
                                    Toast.makeText(context,"[delete favorite result]"+FavoriteModel.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            },throwable -> {
                                //Toast.makeText(context,"[delete favorite]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                            })
                    );
                }
                //post
                else{
                    compositeDisposable.add(myRestaurantAPI.addfavorite(
                                     Common.API_KEY,
                                     Common.currentUser.getFbid(),
                                     foodList.get(position).getId(),
                                     Common.currentRestaurant.getId(),
                                     Common.currentRestaurant.getName(),
                                     foodList.get(position).getName(),
                                     foodList.get(position).getImage(),
                                     foodList.get(position).getPrice())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(FavoriteModel->{
                                if (FavoriteModel.isSuccess()) {
                                    fav.setImageResource(R.drawable.ic_favorite_24dp);
                                    fav.setTag(true);
                                    if(Common.currentFavOfRestaurant != null)
                                        Common.currentFavOfRestaurant.add(new FavoriteOnlyId(foodList.get(position).getId()));
                                }
                                else{
                                    Toast.makeText(context,"[post favorite result]"+FavoriteModel.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            },throwable -> {
                                Toast.makeText(context,"[post favorite]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                            })
                    );
                }
            }
        });
        //EVENT CART AND DETAIL
        holder.setListener(new IFoodInfoOrCartClickListener() {
            @SuppressLint("WrongViewCast")
            @Override
            public void onFoodItemClickListener(View view, int position, boolean isInfo) {
                if(isInfo){
                     //Toast.makeText(context,"info click",Toast.LENGTH_SHORT).show();
                    // EventBus.getDefault().postSticky(new FoodDetailItemEvent(true,foodList.get(position)));
                   //context.startActivity(new Intent(context, FoodDetailsActivity.class));
                    View food_detail = LayoutInflater.from(context)
                            .inflate(R.layout.layout_food_detail,null);
                    img_food_detail =(ImageView)food_detail.findViewById(R.id.img_food_detail);
                    txt_money_food =(TextView)food_detail.findViewById(R.id.txt_money_food);
                    txt_description_food =(TextView)food_detail.findViewById(R.id.txt_description_food);
                    txt_discount_food =(TextView)food_detail.findViewById(R.id.txt_discount_food);
                    radio_group_size =(RadioGroup)food_detail.findViewById(R.id.radio_group_size);

                    selectedFood = foodList.get(position);

                    originalPrice = selectedFood.getPrice();


                    txt_money_food.setText(String.valueOf(originalPrice));
                    txt_description_food.setText(selectedFood.getDescription());
                    txt_discount_food.setText(String.valueOf(selectedFood.getDiscount()));
                    Picasso.get().load(Common.URL_IMG+selectedFood.getImage()).into(img_food_detail);
                    if (selectedFood.isSize()){
                        //load size
                        compositeDisposable.add(myRestaurantAPI.getSizeOfFood(Common.API_KEY,selectedFood.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(sizeModel -> {
                                            EventBus.getDefault().post(new sizeLoadEvent(true,sizeModel.getResult()));
                                            Log.i("[Size Succuss]",String.valueOf(sizeModel.getResult().get(0).getId()));
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
                }
                else{
                    //Toast.makeText(context,"cart click",Toast.LENGTH_SHORT).show();7
                    cartItem cartItem = new cartItem();
                    cartItem.setFoodId(foodList.get(position).getId());
                    cartItem.setFbid(Common.currentUser.getFbid());
                    cartItem.setFoodName(foodList.get(position).getName());
                    cartItem.setFoodPrice(foodList.get(position).getPrice());
                    cartItem.setFoodImage(Common.URL_IMG+foodList.get(position).getImage());
                    cartItem.setFoodQuantity(1);
                    cartItem.setRestaurantId(Common.currentRestaurant.getId());
                    cartItem.setUserPhone(Common.currentUser.getUserPhone());
                    cartItem.setFoodSize("Normal");
                    cartItem.setDiscount((double) foodList.get(position).getDiscount());
                    compositeDisposable.add(cartDataSource.insertorReplaceAll(cartItem)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe( ()->
                                    {
                                        Toast.makeText(context,"Added to cart",Toast.LENGTH_SHORT).show();
                                    },
                                    throwable ->
                                    {
                                        Toast.makeText(context,"[add cart]"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                    })
                    );
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.txt_name_food)
        TextView txt_name_food;
        @BindView(R.id.txt_price_food)
        TextView txt_price_food;
        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.img_add_cart)
        ImageView img_add_cart;
        @BindView(R.id.img_info_food)
        ImageView img_info_food;
        @BindView(R.id.img_fav_food)
        ImageView img_fav_food;

        IFoodInfoOrCartClickListener listener;

        public void setListener(IFoodInfoOrCartClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            img_info_food.setOnClickListener(this);
            img_add_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.img_info_food){
                listener.onFoodItemClickListener(view,getAdapterPosition(),true);
            }
            else if (view.getId() == R.id.img_add_cart){
                listener.onFoodItemClickListener(view,getAdapterPosition(),false);
            }

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
