package com.example.employeerestaurantappfirestore.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
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
import android.widget.Button;

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
        getTheLatestOrdersForToday();
        return view;
    }

    private void setupSnapshotListener() {
        CollectionReference ordersCollectionRef = FirebaseFirestore.getInstance().collection("Orders");

        ordersCollectionRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting documents: ", error);
                return;
            }

            ordersList.clear();

            if (value != null) {
                for (QueryDocumentSnapshot document : value) {
                    ModelOrder modelOrder = document.toObject(ModelOrder.class);
                    modelOrder.setOrderId(document.getId());
                    ordersList.add(modelOrder);
                }
                initAdapter();

                for (ModelOrder order : ordersList) {
                    Log.d("TAG", order.getOrderId() + " => " + order.getCost());
                }
            }
        });
    }

    private void setupSnapshotListener2() {
        CollectionReference ordersCollectionRef = FirebaseFirestore.getInstance().collection("Orders");

        ordersCollectionRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting documents: ", error);
                return;
            }

            ordersList.clear();

            if (value != null) {
                for (QueryDocumentSnapshot document : value) {
                    ModelOrder modelOrder = document.toObject(ModelOrder.class);
                    modelOrder.setOrderId(document.getId());

                    for (ModelOrder.OrderDishes orderDishes : modelOrder.getDishes()) {
                        if (isToday(orderDishes.getDateTime())) {
                            ordersList.add(modelOrder);
                            break;
                        }
                    }

                }
                initAdapter();

                for (ModelOrder order : ordersList) {
                    Log.d("TAG", order.getOrderId() + " => " + order.getCost());
                }
            }
        });
    }

    private void setupSnapshotListener3() {
        CollectionReference ordersCollectionRef = FirebaseFirestore.getInstance().collection("Orders");
        Map<String, ModelOrder> latestOrdersMap = new HashMap<>();
        ordersCollectionRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting documents: ", error);
                return;
            }

            ordersList.clear();

            if (value != null) {
                for (QueryDocumentSnapshot document : value) {
                    ModelOrder modelOrder = document.toObject(ModelOrder.class);
                    modelOrder.setOrderId(document.getId());

                    for (ModelOrder.OrderDishes orderDishes : modelOrder.getDishes()) {
                        if (isToday(orderDishes.getDateTime())) {
                            String idTable = modelOrder.getIdTable().getId();
                            if (latestOrdersMap.containsKey(idTable)) {
                                ModelOrder existingOrder = latestOrdersMap.get(idTable);

                                // Проверка, является ли текущая запись более поздней
                                assert existingOrder != null;
                                Date existingOrderDate = existingOrder.getDishes().get(0).getDateTime();
                                if (existingOrderDate == null || orderDishes.getDateTime().after(existingOrderDate)) {
                                    latestOrdersMap.put(idTable, modelOrder);
                                }
                            } else {
                                // Если IdTable еще нет в мапе, добавляем текущую запись
                                latestOrdersMap.put(idTable, modelOrder);
                            }
                        }
                    }

                }
                ordersList.addAll(latestOrdersMap.values());
                initAdapter();

                for (ModelOrder order : ordersList) {
                    for (ModelOrder.OrderDishes dish : order.getDishes()) {
                        Log.d("TAG", "OrderID: " + order.getOrderId() + ", Dish DateTime: " + dish.getDateTime());
                    }
                }
            }
        });
    }

    private void getTheLatestOrdersForToday() {
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
                List<ModelOrder> newOrdersList = new ArrayList<>(latestOrdersMap.values());

                // Очищаем ordersList и добавляем все элементы из нового списка
                ordersList.clear();
                ordersList.addAll(newOrdersList);
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

    private void testModel(){
        CollectionReference ordersCollectionRef = FirebaseFirestore.getInstance().collection("Orders");
        ordersCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot document : task.getResult()) {
                    ModelOrder modelOrder = document.toObject(ModelOrder.class);
                    modelOrder.setOrderId(document.getId());
                    ordersList.add(modelOrder);
                }
                initAdapter();
                for (ModelOrder order: ordersList){
                    Log.d("TAG", order.getOrderId() + " => " + order.getCost());
                }

            } else {
                Log.e("Firestore", "Error getting documents: ", task.getException());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initAdapter(){
        OrderAdapter orderAdapter = new OrderAdapter(ordersList, OrdersFragment.this);
        orderAdapter.notifyDataSetChanged();
        rv_orders.setAdapter(orderAdapter);
    }
    private void initViews(){
        ordersList = new ArrayList<>();
        rv_orders = view.findViewById(R.id.rv_orders);
        rv_orders.setLayoutManager(new GridLayoutManager(OrdersFragment.newInstance().getContext(), 1));
        rv_orders.setHasFixedSize(true);
        btn_added_order = view.findViewById(R.id.btn_added_order);
    }

}