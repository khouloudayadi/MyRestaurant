package com.example.androidmyrestaurant.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.EventBus.MenuItemEvent;
import com.example.androidmyrestaurant.Interface.IOnRecyclerViewClickListener;
import com.example.androidmyrestaurant.MenuActivity;
import com.example.androidmyrestaurant.Model.Restaurant;
import com.example.androidmyrestaurant.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyRestaurantAdapter extends RecyclerView.Adapter<MyRestaurantAdapter.MyViewHolder> {

    Context context;
    List<Restaurant> restaurantList;

    public MyRestaurantAdapter(Context context, List<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_restaurant,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(Common.URL_IMG+restaurantList.get(position).getImage()).into(holder.img_restaurant);//restaurantList.get(position).getImage()
        holder.txt_restaurant_address.setText(new StringBuilder(restaurantList.get(position).getAddress()));
        holder.txt_restaurant_name.setText(new StringBuilder(restaurantList.get(position).getName()));
        holder.setListener(new IOnRecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Common.currentRestaurant= restaurantList.get(position);
                EventBus.getDefault().postSticky(new MenuItemEvent(true,restaurantList.get(position)));
                context.startActivity(new Intent(context, MenuActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_restaurant_name)
        TextView txt_restaurant_name;
        @BindView(R.id.txt_restaurant_address)
        TextView txt_restaurant_address;
        @BindView(R.id.img_restaurant)
        ImageView img_restaurant;

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
}
