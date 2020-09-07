package com.example.androidmyrestaurant.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.EventBus.FoodItemEvent;
import com.example.androidmyrestaurant.EventBus.MenuItemEvent;
import com.example.androidmyrestaurant.FoodActivity;
import com.example.androidmyrestaurant.Interface.IOnRecyclerViewClickListener;
import com.example.androidmyrestaurant.MenuActivity;
import com.example.androidmyrestaurant.Model.Menu;
import com.example.androidmyrestaurant.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyMenuAdapter extends RecyclerView.Adapter<MyMenuAdapter.MyViewHolder> {
    Context context;
    List<Menu> menuList;

    public MyMenuAdapter(Context context, List<Menu> menuList) {
        this.context = context;
        this.menuList = menuList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item_menu = LayoutInflater.from(context).inflate(R.layout.layout_menu,parent,false);
        return new MyViewHolder(item_menu);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Picasso.get().load(Common.URL_IMG+ menuList.get(position).getImage()).into(holder.img_category);
        holder.txt_category.setText(new StringBuilder(menuList.get(position).getName()));
        holder.setListener(new IOnRecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
               // Common.currentMenu = menuList.get(position);
                EventBus.getDefault().postSticky(new FoodItemEvent(true,menuList.get(position)));
                context.startActivity(new Intent(context, FoodActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.txt_menu)
        TextView txt_category;
        @BindView(R.id.img_menu)
        ImageView img_category;

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

    @Override
    public int getItemViewType(int position) {
            if(menuList.size()==1){
                return Common.DEFAULT_COLUMN_COUNT;
            }
            else {
                if ((menuList.size() % 2) == 0) return Common.DEFAULT_COLUMN_COUNT;
                else {
                    return (position > 1 && position == menuList.size() - 1) ? Common.FULL_WIDTH_COLUMN : Common.DEFAULT_COLUMN_COUNT;
                }
            }
        }
    }

