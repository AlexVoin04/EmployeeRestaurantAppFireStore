package com.example.employeerestaurantappfirestore.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.activities.MainActivity;
import com.example.employeerestaurantappfirestore.fragments.OrdersFragment;
import com.example.employeerestaurantappfirestore.interfaces.OnOrderItemClickListener;
import com.example.employeerestaurantappfirestore.interfaces.OnScrollListener;
import com.example.employeerestaurantappfirestore.model.ModelOrderList;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>{

    private OnOrderItemClickListener listener;
    List<ModelOrderList> orderList;
    OrdersFragment ordersFragment;

    public OrderAdapter(List<ModelOrderList> orderList, OrdersFragment ordersFragment){
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
        Configuration config = ordersFragment.getResources().getConfiguration();
        View v;
        if (config.smallestScreenWidthDp >= 600) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_order, viewGroup, false);
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_order_smart, viewGroup, false);
        }
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position){
        ModelOrderList order = orderList.get(position);
        final DocumentReference idTableRef = order.getIdTable();
        viewHolder.tv_table.setText(idTableRef.getId());
        viewHolder.tv_order_id.setText(order.getOrderId());
//        idTableRef.get().addOnSuccessListener(documentSnapshot -> {
//           if(documentSnapshot.exists()){
//               viewHolder.tv_table.setText(documentSnapshot.getString("number"));
//           }
//        });
        // Если все OrderDishes имеют правильный статус зеленый цвет фона
        Drawable circleBackground;
        if (order.getCompleted()) {
            circleBackground = ContextCompat.getDrawable(ordersFragment.requireContext(), R.drawable.circle_background_active);

        } else {
            // Если хотя бы один OrderDish не имеет правильного статуса цвет по умолчанию
            circleBackground = ContextCompat.getDrawable(ordersFragment.requireContext(), R.drawable.circle_background);
        }
        viewHolder.ll_finish_order.setBackground(circleBackground);
        viewHolder.ll_order_info.setOnClickListener(view -> {
            Context context = ordersFragment.getContext();
            if (context instanceof MainActivity) {
                listener = (OnOrderItemClickListener) context;
                listener.onOrderItemClicked(order.getOrderId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_order_id, tv_table;
        LinearLayout ll_finish_order, ll_order_info;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tv_order_id = itemView.findViewById(R.id.tv_order_id);
            tv_table = itemView.findViewById(R.id.tv_table);
            ll_finish_order = itemView.findViewById(R.id.ll_finish_order);
            ll_order_info = itemView.findViewById(R.id.ll_order_info);
        }
    }
}
