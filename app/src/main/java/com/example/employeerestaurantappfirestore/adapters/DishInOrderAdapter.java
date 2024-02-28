package com.example.employeerestaurantappfirestore.adapters;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.example.employeerestaurantappfirestore.interfaces.DishChangeListener;
import com.example.employeerestaurantappfirestore.model.ModelOrder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.math3.util.Precision;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DishInOrderAdapter extends RecyclerView.Adapter<DishInOrderAdapter.ViewHolder>{
    private List<ModelOrder.OrderDishes> dishesList;
    private final OrderFragment orderFragment;

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
        Configuration config = orderFragment.getResources().getConfiguration();
        View v;
        if (config.smallestScreenWidthDp >= 600) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dish_in_order, viewGroup, false);
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dish_in_order_smart, viewGroup, false);
        }
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position){
        initAdapterForSpinner(viewHolder.spin_status);
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
                int statusPosition = Arrays.asList(orderFragment.getResources().getStringArray(R.array.dish_statuses))
                        .indexOf(documentSnapshot.getString("name"));
                if (statusPosition != -1) {
                    viewHolder.spin_status.setSelection(statusPosition);
                }
            }
        });
        viewHolder.et_order_cost.setText(String.valueOf(dish.getCost()));
        SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy г., HH:mm", Locale.getDefault());
        viewHolder.tv_date_time.setText(formatter.format(dish.getDateTime()));
        viewHolder.et_quantity.setText(String.valueOf(dish.getQuantity()));
//        viewHolder.spin_status.setSelection(Integer.parseInt(dish.getIdDishStatus().getId())-1);
        Calendar dateAndTime = Calendar.getInstance();
        dateAndTime.setTime(dish.getDateTime());
        initListeners(viewHolder, dateAndTime, dish);
    }

    private void initListeners(@NonNull final ViewHolder viewHolder, Calendar dateAndTime, ModelOrder.OrderDishes dish){
        TimePickerDialog.OnTimeSetListener t= (view, hourOfDay, minute) -> {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            setInitialDateTime(viewHolder.tv_date_time, dateAndTime, dish);
        };
        DatePickerDialog.OnDateSetListener d = (view, year, monthOfYear, dayOfMonth) -> {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime(viewHolder.tv_date_time, dateAndTime, dish);
        };
        viewHolder.ll_date_btn.setOnClickListener(view ->
                new DatePickerDialog(orderFragment.requireContext(), d,
                        dateAndTime.get(Calendar.YEAR),
                        dateAndTime.get(Calendar.MONTH),
                        dateAndTime.get(Calendar.DAY_OF_MONTH))
                        .show()
        );
        viewHolder.ll_time_btn.setOnClickListener(view ->
                new TimePickerDialog(orderFragment.requireContext(), t,
                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                        dateAndTime.get(Calendar.MINUTE), true)
                        .show()
        );

        viewHolder.et_order_cost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Действия перед изменением текста
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Действия при изменении текста
                String newCost = charSequence.toString();
                if(!newCost.isEmpty()){
                    dish.setCost(Precision.round(Double.parseDouble(newCost),2));
                    sendChange(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // Действия после изменения текста
            }
        });

        viewHolder.et_quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Действия перед изменением текста
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Действия при изменении текста
                String newQuantity = charSequence.toString();
                if (!newQuantity.isEmpty()){
                    if (Integer.parseInt(newQuantity) != 0){
                        dish.setQuantity(Integer.parseInt(newQuantity));
                        sendChange(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Действия после изменения текста
            }
        });

        viewHolder.iv_delete.setOnClickListener(view -> {
            dishesList.remove(dish);
            sendChange(true);
        });

        viewHolder.spin_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int selectedItemPosition, long l) {
                // Вызов метода для обработки выбора значения в Spinner
                int statusId = selectedItemPosition + 1;
                DocumentReference dishStatusReference = FirebaseFirestore.getInstance()
                        .collection("DishStatus")
                        .document(String.valueOf(statusId));
                if (!dishStatusReference.getId().equals(dish.getIdDishStatus().getId())){
                    dish.setIdDishStatus(dishStatusReference);
                    sendChange(false);
                }
//                ModelOrder.OrderDishes dish = statusDishes.get(posit);
//                int statusId = selectedItemPosition + 1;
//                DocumentReference dishStatusReference = FirebaseFirestore.getInstance()
//                        .collection("DishStatus")
//                        .document(String.valueOf(statusId));
//                dish.setIdDishStatus(dishStatusReference);
//                DocumentReference tableReference = FirebaseFirestore.getInstance()
//                        .collection("Orders")
//                        .document(orderId);
//
//                tableReference.update("dishes", statusDishes)
//                        .addOnCompleteListener(updateTask -> {
//                            if (updateTask.isSuccessful()) {
//                                Log.d("DishInOrderAdapter", "Статус блюда в заказе "+orderId+" обновлен: " + statusId);
//                            } else {
//                                Exception e = updateTask.getException();
//                                if (e != null) {
//                                    Log.e("FireStore", Objects.requireNonNull(e.getMessage()));
//                                }
//                            }
//                        });

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Ваш код, который выполнится, если не выбрано ничего
            }
        });
    }

    private DishChangeListener mListener;

    public void setDishChangeListener(DishChangeListener listener) {
        this.mListener = listener;
    }

    private void sendChange(boolean isDelete){
        if (mListener != null) {
            mListener.onChangeFields(dishesList, isDelete);
        }
    }

    private void setInitialDateTime(TextView tv_date, Calendar dateAndTime, ModelOrder.OrderDishes dish) {
        tv_date.setText(DateUtils.formatDateTime(orderFragment.requireContext(),
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
        dish.setDateTime(dateAndTime.getTime());
        sendChange(false);
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
