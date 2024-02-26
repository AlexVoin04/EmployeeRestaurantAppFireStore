package com.example.employeerestaurantappfirestore.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.fragments.OrderFragment;
import com.example.employeerestaurantappfirestore.model.ModelOrder;
import com.google.firebase.firestore.DocumentReference;


import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DishInOrderAdapter extends RecyclerView.Adapter<DishInOrderAdapter.ViewHolder>{
    private List<ModelOrder.OrderDishes> dishesList;
    private OrderFragment orderFragment;

    public DishInOrderAdapter(List<ModelOrder.OrderDishes> dishesList, OrderFragment orderFragment) {
        this.dishesList = dishesList;
        this.orderFragment = orderFragment;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dish_in_order, viewGroup, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position){
        ModelOrder.OrderDishes dish = dishesList.get(position);
        DocumentReference idDishStatus = dish.getIdDishStatus();
        DocumentReference idDish = dish.getIdDishes();
        idDish.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e("FirestoreError", Objects.requireNonNull(error.getMessage()));
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                viewHolder.tv_title.setText(documentSnapshot.getString("abbreviatedName"));
            }
        });
        idDishStatus.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e("FirestoreError", Objects.requireNonNull(error.getMessage()));
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
//                viewHolder.tv_status_dish_ready_order.setText(documentSnapshot.getString("name"));
            }
        });
        viewHolder.et_order_cost.setText(String.valueOf(dish.getCost()));
        SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy г. в HH:mm", Locale.getDefault());
        viewHolder.tv_date_time.setText(formatter.format(dish.getDateTime()));
        viewHolder.et_quantity.setText(String.valueOf(dish.getQuantity()));
        initAdapterForSpinner(viewHolder.spin_status);
        viewHolder.spin_status.setSelection(Integer.parseInt(dish.getIdDishStatus().getId())-1);
    }

    private void initAdapterForSpinner(Spinner spinner){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                orderFragment.requireContext(),
                R.array.dish_statuses,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return dishesList == null ? 0 : dishesList .size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_title, tv_date_time;
        EditText et_order_cost, et_quantity;
        LinearLayout ll_date_btn, ll_time_btn;
        ImageView iv_delete;
        Spinner spin_status;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            et_order_cost = itemView.findViewById(R.id.et_order_cost);
            tv_date_time = itemView.findViewById(R.id.tv_date_time);
            ll_date_btn = itemView.findViewById(R.id.ll_date_btn);
            ll_time_btn = itemView.findViewById(R.id.ll_time_btn);
            iv_delete = itemView.findViewById(R.id.iv_delete);
            et_quantity = itemView.findViewById(R.id.et_quantity);
            spin_status = itemView.findViewById(R.id.spin_status);
        }
    }
}
