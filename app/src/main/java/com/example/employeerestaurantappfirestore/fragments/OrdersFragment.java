package com.example.employeerestaurantappfirestore.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.activities.MainActivity;
import com.example.employeerestaurantappfirestore.adapters.OrderAdapter;
import com.example.employeerestaurantappfirestore.dialogs.TablesDialog;
import com.example.employeerestaurantappfirestore.interfaces.OnScrollListener;
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
    private TextView tv_tables_select;
    private FirebaseFirestore fireStore;
    private String[] tableArrayForFilter;
    private boolean[] selectedTableForFilter;
    private ArrayList<Integer> tableListForFilter;
    private LinearLayout ll_settings_btn, ll_settings;
    private boolean opened;
    private NestedScrollView nsv_order;

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
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            view = inflater.inflate(R.layout.fragment_orders, container, false);
            initViews();
        } else {
            view = inflater.inflate(R.layout.fragment_orders_smart, container, false);
            initViews();
            smartScroll();
        }
        initAdapterForSpinner();
        getTablesForFilter();
        initListeners();
        getTheLatestOrdersForToday();
        return view;
    }


    private void getTheLatestOrdersForToday() {
        int scrollY = rv_orders.getScrollY();
        rl_orders_not_found.setVisibility(View.GONE);
        rv_orders.setVisibility(View.VISIBLE);
        CollectionReference ordersCollectionRef = fireStore.collection("Orders");
        Map<String, ModelOrder> latestOrdersMap = new HashMap<>();
        ordersCollectionRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error getting documents: ", error);
                return;
            }
            ordersList.clear();
            latestOrdersMap.clear();
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
                List<ModelOrder> newOrdersList = new ArrayList<>(latestOrdersMap.values());
                ordersList.addAll(newOrdersList);
                filterReadyOrder(newOrdersList);

                ordersList.sort(Comparator.comparing(ModelOrder::getDateTimeMax));
                tablesSelect();
                if(ordersList.size()==0){
                    ordersNotFound();
                    return;
                }
                for (ModelOrder order : ordersList) {
                    Log.d("TAG", order.getOrderId() + " => " + order.getCost() +" Date: " +order.getDateTimeMax().toString());
                }
                initAdapter();
                rv_orders.scrollToPosition(scrollY);
            }
        });
    }

    private void filterReadyOrder(List<ModelOrder> newOrdersList){
        if(filterNumber == 1){
            ordersList.clear();
            for(ModelOrder order: newOrdersList){
                if(order.getCompleted()){
                    ordersList.add(order);
                }
            }
        }
        else if (filterNumber==2){
            ordersList.clear();
            for(ModelOrder order: newOrdersList){
                if(!order.getCompleted()){
                    ordersList.add(order);
                }
            }
        }
    }
    private void tablesSelect(){
        if (tableListForFilter.size()!=0){
            List<ModelOrder> ordersToRemove = new ArrayList<>();
            for (ModelOrder order : ordersList) {
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

    private void ordersNotFound(){
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
        btn_added_order = view.findViewById(R.id.btn_added_order);
        rl_orders_not_found = view.findViewById(R.id.rl_orders_not_found);
        spin_filter_orders = view.findViewById(R.id.spin_filter_orders);
        tv_tables_select = view.findViewById(R.id.tv_tables_select);
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
        tv_tables_select.setOnClickListener(view -> {
            TablesDialog.initTablesSelectBuilder(context, tableArrayForFilter, selectedTableForFilter, tableListForFilter, tv_tables_select, this::getTheLatestOrdersForToday);
        });
        ll_settings_btn.setOnClickListener(view -> {
            if (!opened) {
                // Показываем представление
                ll_settings.setVisibility(View.VISIBLE);
                TranslateAnimation animate = new TranslateAnimation(-ll_settings.getWidth(), 0, 0, 0);
                animate.setDuration(500);
                animate.setFillAfter(true);
                ll_settings.startAnimation(animate);
            } else {
                // Скрываем представление
                TranslateAnimation animate = new TranslateAnimation(0, -ll_settings.getWidth()-100, 0, 0);
                animate.setDuration(500);
                animate.setFillAfter(true);
                animate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // Начало анимации
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // Завершение анимации
                        ll_settings.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // Повторение анимации
                    }
                });
                ll_settings.startAnimation(animate);
            }
            opened = !opened;
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

    private OnScrollListener onScrollListener;
    private void smartScroll() {
        if (context instanceof MainActivity) {
            onScrollListener = (OnScrollListener) context;
        }
        nsv_order.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    // Скроллинг вниз
                    Log.d("ScrollDirection", "Scrolling Down");
                    if (onScrollListener != null) {
                        onScrollListener.onScrollDown();
                    }
                } else if (scrollY < oldScrollY) {
                    // Скроллинг вверх
                    Log.d("ScrollDirection", "Scrolling Up");
                    if (onScrollListener != null) {
                        onScrollListener.onScrollUp();
                    }
                } else if(scrollY==0) {
                    Log.d("ScrollDirection", "Scrolling Up");
                    if (onScrollListener != null) {
                        onScrollListener.onScrollUp();
                    }
                }
            }
        });
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