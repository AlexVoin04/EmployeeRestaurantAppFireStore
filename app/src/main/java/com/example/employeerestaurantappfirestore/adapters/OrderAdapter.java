package com.example.employeerestaurantappfirestore.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.fragments.OrdersFragment;
import com.example.employeerestaurantappfirestore.model.ModelOrder;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>{

    List<ModelOrder> orderList;
    OrdersFragment ordersFragment;

    public OrderAdapter(List<ModelOrder> orderList, OrdersFragment ordersFragment){
        this.orderList = orderList;
        this.ordersFragment = ordersFragment;
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_order, viewGroup, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position){

    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_order_id, tv_table;
        LinearLayout ll_finish_order;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tv_order_id = itemView.findViewById(R.id.tv_order_id);
            tv_table = itemView.findViewById(R.id.tv_table);
            ll_finish_order = itemView.findViewById(R.id.ll_finish_order);
        }
    }
}
