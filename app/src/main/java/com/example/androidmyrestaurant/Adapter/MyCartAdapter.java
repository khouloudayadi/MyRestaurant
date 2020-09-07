package com.example.androidmyrestaurant.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidmyrestaurant.DataBase.cartDataBase;
import com.example.androidmyrestaurant.DataBase.cartDataSource;
import com.example.androidmyrestaurant.DataBase.cartItem;
import com.example.androidmyrestaurant.DataBase.localCartDataSource;
import com.example.androidmyrestaurant.EventBus.calculatePriceEvent;
import com.example.androidmyrestaurant.Interface.IOnImageViewAdapterClickListener;
import com.example.androidmyrestaurant.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder>{

    Context context;
    List<cartItem> cartItemList;
    cartDataSource cartDataSource;

    public MyCartAdapter(Context context, List<cartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        cartDataSource = new localCartDataSource(cartDataBase.getInstance(context).cartDAO());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item_cart = LayoutInflater.from(context).inflate(R.layout.layout_cart,parent,false);
        return new MyViewHolder(item_cart) ;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(cartItemList.get(position).getFoodImage()).into(holder.img_food);
        holder.txt_name_food.setText(cartItemList.get(position).getFoodName());
        holder.txt_food_price.setText(String.valueOf(cartItemList.get(position).getFoodPrice()));
        holder.txt_qte_food.setText(String.valueOf(cartItemList.get(position).getFoodQuantity()));

        double finalResult = cartItemList.get(position).getFoodPrice() * cartItemList.get(position).getFoodQuantity();
        holder.txt_new_price.setText(String.valueOf(finalResult));
       //holder.txt_extra_price.setText(new StringBuilder(R.string.extra_price).append(String.valueOf(cartItemList.get(position).getFoodExtraPrice())));
        //EVENT
        holder.setListener(new IOnImageViewAdapterClickListener() {
            @Override
            public void onCalculatePriceListener(View view, int position, boolean isDecrease, boolean isDelete) {
                if (!isDelete){
                    if(isDecrease){
                        if (cartItemList.get(position).getFoodQuantity() >1){
                            cartItemList.get(position).setFoodQuantity(cartItemList.get(position).getFoodQuantity()-1);
                        }
                    }
                    else{
                        cartItemList.get(position).setFoodQuantity(cartItemList.get(position).getFoodQuantity()+1);
                    }

                    cartDataSource.updateCart(cartItemList.get(position))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {}

                                @Override
                                public void onSuccess(Integer integer) {
                                    holder.txt_qte_food.setText(String.valueOf(cartItemList.get(position).getFoodQuantity()));
                                    EventBus.getDefault().postSticky(new calculatePriceEvent());
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(context,"[update cart]"+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                }
                else{//delete cart
                    cartDataSource.deleteCart(cartItemList.get(position))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    notifyItemMoved(integer,position);
                                    EventBus.getDefault().postSticky(new calculatePriceEvent());
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(context,"[delete cart]"+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_new_price)
        TextView txt_new_price;
        @BindView(R.id.txt_name_food)
        TextView txt_name_food;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.txt_qte_food)
        TextView txt_qte_food;

        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.img_delete_food)
        ImageView img_delete_food;
        @BindView(R.id.img_increase)
        ImageView img_increase;
        @BindView(R.id.img_decrease)
        ImageView img_decrease;


        IOnImageViewAdapterClickListener listener;

        public void setListener(IOnImageViewAdapterClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);

            img_decrease.setOnClickListener(this);
            img_increase.setOnClickListener(this);
            img_delete_food.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == img_decrease){
                listener.onCalculatePriceListener(view,getAdapterPosition(),true,false);
            }
            else if(view== img_increase){
                listener.onCalculatePriceListener(view,getAdapterPosition(),false,false);
            }
            else if (view == img_delete_food){
                listener.onCalculatePriceListener(view,getAdapterPosition(),false,true);
            }
        }
    }
}
