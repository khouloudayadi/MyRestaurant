package com.example.androidmyrestaurant.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.androidmyrestaurant.Common.Common;
import com.example.androidmyrestaurant.Interface.IOnPrintItemClickListener;
import com.example.androidmyrestaurant.Interface.IOnRecyclerViewClickListener;
import com.example.androidmyrestaurant.Model.Order;
import com.example.androidmyrestaurant.Model.orderDetail;
import com.example.androidmyrestaurant.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder> {
    Context context;
    List<Order> orderList;
    SimpleDateFormat simpleDateFormat;

    List<Order> orderDetails= new ArrayList<>();

    public MyOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item_order = LayoutInflater.from(context).inflate(R.layout.layout_order,parent,false);
        return new MyOrderAdapter.MyViewHolder(item_order) ;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.txt_num_of_item.setText(new StringBuilder(context.getString(R.string.Num_of_Item)).append(orderList.get(position).getNumOfItem()));
        holder.txt_order_address.setText(orderList.get(position).getOrderAddress());
        holder.txt_order_date.setText(new StringBuilder(simpleDateFormat.format(orderList.get(position).getOrderDate())));
        holder.txt_order_phone.setText(orderList.get(position).getOrderPhone());
        holder.txt_order_price.setText(new StringBuilder(context.getString(R.string.money_sign)).append(orderList.get(position).getTotalPrice()));

        holder.txt_order_number.setText(new StringBuilder(context.getString(R.string.Order_Number)).append(orderList.get(position).getOrderId()));
        holder.txt_order_statut.setText(Common.convertStatusToString(orderList.get(position).getOrderStatus()));

        if (orderList.get(position).isCod())
            holder.txt_payment_methode.setText(new StringBuilder(context.getString(R.string.Cash_On_Deliver)));
        else
            holder.txt_payment_methode.setText(new StringBuilder(context.getString(R.string.Online_Payment)));

        holder.setListener(new IOnPrintItemClickListener() {
            @Override
            public void onPrintItemClickListener(View view, int position) {
                //Code for print

            }
        });

    }

    @Override
    public int getItemCount()
    {
        return orderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_order_phone)
        TextView txt_order_phone;
        @BindView(R.id.txt_order_address)
        TextView txt_order_address;
        @BindView(R.id.txt_payment_methode)
        TextView txt_payment_methode;
        @BindView(R.id.txt_order_date)
        TextView txt_order_date;
        @BindView(R.id.txt_order_price)
        TextView txt_order_price;
        @BindView(R.id.txt_num_of_item)
        TextView txt_num_of_item;
        @BindView(R.id.txt_order_statut)
        TextView txt_order_statut;
        @BindView(R.id.img_print)
        ImageView img_print;

        IOnPrintItemClickListener listener;

        public void setListener(IOnPrintItemClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            img_print.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.img_print)
                listener.onPrintItemClickListener(view,getAdapterPosition());

        }
    }
}
