package com.example.employeerestaurantappfirestore.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.activities.MainActivity;
import com.example.employeerestaurantappfirestore.adapters.OrderAdapter;
import com.example.employeerestaurantappfirestore.dialogs.TablesDialog;
import com.example.employeerestaurantappfirestore.interfaces.OnOrderItemClickListener;
import com.example.employeerestaurantappfirestore.model.ModelOrderList;
import com.example.employeerestaurantappfirestore.utils.Animations;
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
    private List<ModelOrderList> ordersList;
    private RelativeLayout rl_orders_not_found;
    private Spinner spin_filter_orders;
    private Context context;
    private Integer filterNumber;
    private TextView tv_tables_select, tv_clear_filter;
    private FirebaseFirestore fireStore;
    private String[] tableArrayForFilter;
    private boolean[] selectedTableForFilter;
    private ArrayList<Integer> tableListForFilter;
    private LinearLayout ll_settings_btn, ll_settings, ll_btn_added_order;
    private boolean opened;
    private NestedScrollView nsv_order;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            view = inflater.inflate(R.layout.fragment_orders, container, false);
            initViews();
        } else {
            view = inflater.inflate(R.layout.fragment_orders_smart, container, false);
            initViews();
            Animations.smartScroll(context, nsv_order);
        }
        initAdapterForSpinner();
        getTablesForFilter();
        initListeners();
        getTheLatestOrdersForToday();
        return view;
    }


    private void getTheLatestOrdersForToday() {
        int scrollY = rv_orders.getScrollY();
        setStatusVisible(View.GONE, View.VISIBLE);
        CollectionReference ordersCollectionRef = fireStore.collection("Orders");
        Map<String, ModelOrderList> latestOrdersMap = new HashMap<>();
        ordersCollectionRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting documents: ", error);
                return;
            }
            ordersList.clear();
            latestOrdersMap.clear();
            if (value != null) {
                for (QueryDocumentSnapshot document : value) {
                    ModelOrderList modelOrderList = document.toObject(ModelOrderList.class);
                    modelOrderList.setOrderId(document.getId());
                    modelOrderList.getDishes().sort(Comparator.comparing(ModelOrderList.OrderDishes::getDateTime).reversed());
                    modelOrderList.setDateTimeMax(modelOrderList.getDishes().get(0).getDateTime());
                    modelOrderList.setCompleted(modelOrderList.calculationComplete());
                    if(isToday(modelOrderList.getDateTimeMax())){
                        String idTable = modelOrderList.getIdTable().getId();
                        if (latestOrdersMap.containsKey(idTable)) {
                            ModelOrderList existingOrder = latestOrdersMap.get(idTable);

                            // Проверка, является ли текущая запись более поздней
                            assert existingOrder != null;
                            existingOrder.getDishes().sort(Comparator.comparing(ModelOrderList.OrderDishes::getDateTime).reversed());
                            Date existingOrderDate = existingOrder.getDishes().get(0).getDateTime();
                            if (existingOrderDate == null || modelOrderList.getDishes().get(0).getDateTime().after(existingOrderDate)) {
                                latestOrdersMap.put(idTable, modelOrderList);
                            }
                        } else {
                            // Если IdTable еще нет в мапе, добавляем текущую запись
                            latestOrdersMap.put(idTable, modelOrderList);
                        }
                    }

                }
                List<ModelOrderList> newOrdersList = new ArrayList<>(latestOrdersMap.values());
                ordersList.addAll(newOrdersList);
                filterReadyOrder(newOrdersList);

                ordersList.sort(Comparator.comparing(ModelOrderList::getDateTimeMax));
                tablesSelect();
                initAdapter();
                if(ordersList.size()==0){
                    Log.d("Firestore", "ordersList.size()==0");
                    setStatusVisible(View.VISIBLE, View.GONE);
                    return;
                }
                for (ModelOrderList order : ordersList) {
                    Log.d("TAG", order.getOrderId() + " => " + order.getCost() +" Date: " +order.getDateTimeMax().toString());
                }
                rv_orders.scrollToPosition(scrollY);
            }
        });
    }

    private void filterReadyOrder(List<ModelOrderList> newOrdersList){
        if(filterNumber == 1){
            ordersList.clear();
            for(ModelOrderList order: newOrdersList){
                if(order.getCompleted()){
                    ordersList.add(order);
                }
            }
        }
        else if (filterNumber==2){
            ordersList.clear();
            for(ModelOrderList order: newOrdersList){
                if(!order.getCompleted()){
                    ordersList.add(order);
                }
            }
        }
    }
    private void tablesSelect(){
        if (tableListForFilter.size()!=0){
            List<ModelOrderList> ordersToRemove = new ArrayList<>();
            for (ModelOrderList order : ordersList) {
                boolean shouldRemove = true;
                for (int j = 0; j < tableListForFilter.size(); j++) {
                    if (tableArrayForFilter[tableListForFilter.get(j)].equals(order.getIdTable().getId())) {
                        shouldRemove = false;
                        break;
                    }
                }
                if (shouldRemove) {
                    ordersToRemove.add(order);
                }
            }

            // Удаление элементов из ordersList
            ordersList.removeAll(ordersToRemove);
        }
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

    private void setStatusVisible(int status1, int status2){
        rl_orders_not_found.setVisibility(status1);
        nsv_order.setVisibility(status2);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initAdapter(){
        OrderAdapter orderAdapter = new OrderAdapter(ordersList, OrdersFragment.this);
        orderAdapter.notifyDataSetChanged();
        rv_orders.setAdapter(orderAdapter);
    }
    private void initViews(){
        fireStore = FirebaseFirestore.getInstance();
        tableListForFilter = new ArrayList<>();
        filterNumber = 0;
        ordersList = new ArrayList<>();
        context = getContext();
        nsv_order = view.findViewById(R.id.nsv_order);
        ll_settings_btn = view.findViewById(R.id.ll_settings_btn);
        ll_settings = view.findViewById(R.id.ll_settings);
        rv_orders = view.findViewById(R.id.rv_orders);
        rv_orders.setLayoutManager(new GridLayoutManager(context, 1));
        rv_orders.setHasFixedSize(true);
        ll_btn_added_order = view.findViewById(R.id.ll_btn_added_order);
        rl_orders_not_found = view.findViewById(R.id.rl_orders_not_found);
        spin_filter_orders = view.findViewById(R.id.spin_filter_orders);
        tv_tables_select = view.findViewById(R.id.tv_tables_select);
        tv_clear_filter = view.findViewById(R.id.tv_clear_filter);
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
        tv_tables_select.setOnClickListener(view -> TablesDialog.initTablesSelectBuilder(
                context,
                tableArrayForFilter,
                selectedTableForFilter,
                tableListForFilter,
                tv_tables_select,
                this::getTheLatestOrdersForToday)
        );
        ll_settings_btn.setOnClickListener(view -> opened = Animations.hideAndShowSettings(opened, ll_settings, context));
        ll_btn_added_order.setOnClickListener(view1 -> {
            if (context instanceof MainActivity) {
                OnOrderItemClickListener listener = (OnOrderItemClickListener) context;
                listener.onNewItemClicked();
            }
        });
        tv_clear_filter.setOnClickListener(view1 -> {
            spin_filter_orders.setSelection(0);
            TablesDialog.clearAll(selectedTableForFilter, tableListForFilter, tv_tables_select, this::getTablesForFilter);
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

    private void getTablesForFilter(){
        TablesDialog.getTables(fireStore, new TablesDialog.OnTablesLoadedListener() {
            @Override
            public void onTablesLoaded(String[] tables) {
                tableArrayForFilter = tables;
                selectedTableForFilter = new boolean[tableArrayForFilter.length];
            }

            @Override
            public void onTablesLoadFailed(Exception e) {
                // Обработка ошибки при загрузке таблиц
            }
        });
    }
}