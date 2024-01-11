package com.example.employeerestaurantappfirestore.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.adapters.OrderAdapter;
import com.example.employeerestaurantappfirestore.model.ModelOrder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersFragment extends Fragment {
    private View view;
    private RecyclerView rv_orders;
    private Button btn_added_order;
    private List<ModelOrder> ordersList;
    private RelativeLayout rl_orders_not_found;
    private Spinner spin_filter_orders;
    private Context context;
    private Integer filterNumber;

    public static OrdersFragment newInstance() {
        return new OrdersFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_orders, container, false);
        initViews();
        initAdapterForSpinner();
        initListeners();
        getTheLatestOrdersForToday();
        return view;
    }


    private void getTheLatestOrdersForToday() {
        rl_orders_not_found.setVisibility(View.GONE);
        rv_orders.setVisibility(View.VISIBLE);
        CollectionReference ordersCollectionRef = FirebaseFirestore.getInstance().collection("Orders");
        Map<String, ModelOrder> latestOrdersMap = new HashMap<>();
        ordersCollectionRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting documents: ", error);
                return;
            }

            if (value != null) {
                for (QueryDocumentSnapshot document : value) {
                    ModelOrder modelOrder = document.toObject(ModelOrder.class);
                    modelOrder.setOrderId(document.getId());
                    modelOrder.getDishes().sort(Comparator.comparing(ModelOrder.OrderDishes::getDateTime).reversed());
                    modelOrder.setDateTimeMax(modelOrder.getDishes().get(0).getDateTime());
                    boolean completed = true;
                    for (ModelOrder.OrderDishes orderDish : modelOrder.getDishes()) {
                        if (!"3".equals(orderDish.getIdDishStatus().getId())) {
                            completed = false;
                            break;
                        }
                    }
                    modelOrder.setCompleted(completed);
                    if(isToday(modelOrder.getDateTimeMax())){
                        String idTable = modelOrder.getIdTable().getId();
                        if (latestOrdersMap.containsKey(idTable)) {
                            ModelOrder existingOrder = latestOrdersMap.get(idTable);

                            // Проверка, является ли текущая запись более поздней
                            assert existingOrder != null;
                            existingOrder.getDishes().sort(Comparator.comparing(ModelOrder.OrderDishes::getDateTime).reversed());
                            Date existingOrderDate = existingOrder.getDishes().get(0).getDateTime();
                            if (existingOrderDate == null || modelOrder.getDishes().get(0).getDateTime().after(existingOrderDate)) {
                                latestOrdersMap.put(idTable, modelOrder);
                            }
                        } else {
                            // Если IdTable еще нет в мапе, добавляем текущую запись
                            latestOrdersMap.put(idTable, modelOrder);
                        }
                    }

                }
                ordersList.clear();
                List<ModelOrder> newOrdersList = new ArrayList<>(latestOrdersMap.values());
                ordersList.addAll(newOrdersList);
                if(ordersList.size()==0){
                    ordersNotFound();
                    return;
                }
                if(filterNumber==1){
                    ordersList.clear();
                    for(ModelOrder order: newOrdersList){
                        if(order.getCompleted()){
                            ordersList.add(order);
                        }
                    }
                }
                else if(filterNumber==2){
                    ordersList.clear();
                    for(ModelOrder order: newOrdersList){
                        if(!order.getCompleted()){
                            ordersList.add(order);
                        }
                    }
                }
                initAdapter();

                for (ModelOrder order : ordersList) {
                    Log.d("TAG", order.getOrderId() + " => " + order.getCost() +"Date: " +order.getDateTimeMax().toString());
                }
            }
        });
    }

    // Вспомогательный метод для проверки, является ли дата сегодняшней
    private boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar otherDate = Calendar.getInstance();
        otherDate.setTime(date);
        return today.get(Calendar.YEAR) == otherDate.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == otherDate.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == otherDate.get(Calendar.DAY_OF_MONTH);
    }

    private void ordersNotFound(){
        Log.d("Firestore", "dishes.size()==0");
        Log.d("Firestore", "ordersList.size()==0");
        rl_orders_not_found.setVisibility(View.VISIBLE);
        rv_orders.setVisibility(View.GONE);
    }



    @SuppressLint("NotifyDataSetChanged")
    private void initAdapter(){
        OrderAdapter orderAdapter = new OrderAdapter(ordersList, OrdersFragment.this);
        orderAdapter.notifyDataSetChanged();
        rv_orders.setAdapter(orderAdapter);
    }
    private void initViews(){
        filterNumber = 0;
        ordersList = new ArrayList<>();
        context = getContext();
        rv_orders = view.findViewById(R.id.rv_orders);
        rv_orders.setLayoutManager(new GridLayoutManager(OrdersFragment.newInstance().getContext(), 1));
        rv_orders.setHasFixedSize(true);
        btn_added_order = view.findViewById(R.id.btn_added_order);
        rl_orders_not_found = view.findViewById(R.id.rl_orders_not_found);
        spin_filter_orders = view.findViewById(R.id.spin_filter_orders);
    }

    private void initListeners(){
        spin_filter_orders.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterNumber = pos;
                getTheLatestOrdersForToday();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initAdapterForSpinner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.orders_filter_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_filter_orders.setAdapter(adapter);
    }

}