package com.example.employeerestaurantappfirestore.adapters;

import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.interfaces.DishesListener;
import com.example.employeerestaurantappfirestore.model.ModelDishes;
import com.example.employeerestaurantappfirestore.model.ModelDishesQuantity;

import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.ViewHolder>{
    private List<ModelDishes> dishesList;
    private List<ModelDishesQuantity> dishesQuantityList;
    private final DishesListener dishesListener;

    public DishAdapter(List<ModelDishes> dishesList, List<ModelDishesQuantity> dishesQuantityList, DishesListener listener) {
        this.dishesList = dishesList;
        this.dishesQuantityList = dishesQuantityList;
        this.dishesListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dish, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ModelDishes dishes = dishesList.get(position);
        initViews(viewHolder, dishes);
        initListeners(viewHolder, dishes);
    }

    private void initViews(ViewHolder viewHolder, ModelDishes dishes){
        viewHolder.tv_title_dishes.setText(dishes.getAbbreviatedName());
        viewHolder.tv_cost.setText(String.valueOf(dishes.getCost()));
        double discount = dishes.getDiscount();
        if (discount > 0) {
            viewHolder.ll_cost_with_discount.setVisibility(View.VISIBLE);
            double discountCost = dishes.getFinalPrice();
            viewHolder.tv_cost_discount.setText(String.valueOf(discountCost));
            viewHolder.tv_cost.setPaintFlags(viewHolder.tv_cost.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            viewHolder.ll_cost_with_discount.setVisibility(View.GONE);
            viewHolder.tv_cost.setPaintFlags(viewHolder.tv_cost.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        boolean isAdded = true;
        for (ModelDishesQuantity modelDish : dishesQuantityList) {
            if (modelDish.getDish().getId().equals(dishes.getId())) {
                viewHolder.et_count.setText(String.valueOf(modelDish.getQuantity()));
                isAdded = false;
                break;
            }
        }
        if (isAdded){
            setVisible(viewHolder, View.VISIBLE, View.GONE);
        }
        else{
            setVisible(viewHolder, View.GONE, View.VISIBLE);
        }
    }

    private void setVisible(ViewHolder viewHolder, int status1, int status2){
        viewHolder.ll_added_btn.setVisibility(status1);
        viewHolder.ll_plus_minus_count.setVisibility(status2);
    }

    private void initListeners(ViewHolder viewHolder, ModelDishes dishes){
        viewHolder.ll_added_btn.setOnClickListener(view -> {
            ModelDishesQuantity dishesQuantity = new ModelDishesQuantity(dishes, 1);
            if (dishesListener !=null){
                dishesListener.onAddButtonClick(dishesQuantity);
            }
        });
        viewHolder.ll_plus_btn.setOnClickListener(view -> dishesListener.onPlusButtonClick(dishes.getId()));
        viewHolder.ll_minus_btn.setOnClickListener(view -> dishesListener.onMinusButtonClick(dishes.getId()));
        viewHolder.et_count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Действия перед изменением текста
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Действия при изменении текста
                String newQuantity = charSequence.toString();
                if (!newQuantity.isEmpty()){
                    if (Integer.parseInt(newQuantity) >= 1){
                        dishesListener.onChangeTheQuantity(dishes.getId(), Integer.parseInt(newQuantity));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Действия после изменения текста
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishesList == null ? 0 : dishesList .size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_title_dishes, tv_cost_discount, tv_cost;
        EditText et_count;
        LinearLayout ll_added_btn, ll_plus_minus_count, ll_minus_btn, ll_plus_btn, ll_cost_with_discount;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tv_title_dishes = itemView.findViewById(R.id.tv_title_dishes);
            tv_cost_discount = itemView.findViewById(R.id.tv_cost_discount);
            tv_cost = itemView.findViewById(R.id.tv_cost);
            ll_added_btn = itemView.findViewById(R.id.ll_added_btn);
            ll_plus_minus_count = itemView.findViewById(R.id.ll_plus_minus_count);
            ll_minus_btn = itemView.findViewById(R.id.ll_minus_btn);
            et_count = itemView.findViewById(R.id.et_count);
            ll_plus_btn = itemView.findViewById(R.id.ll_plus_btn);
            ll_cost_with_discount = itemView.findViewById(R.id.ll_cost_with_discount);
        }
    }
}
