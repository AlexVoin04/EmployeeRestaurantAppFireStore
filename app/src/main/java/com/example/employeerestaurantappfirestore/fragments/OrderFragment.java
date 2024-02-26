package com.example.employeerestaurantappfirestore.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.activities.MainActivity;
import com.example.employeerestaurantappfirestore.adapters.DishInOrderAdapter;
import com.example.employeerestaurantappfirestore.dialogs.TablesDialog;
import com.example.employeerestaurantappfirestore.interfaces.OnScrollListener;
import com.example.employeerestaurantappfirestore.model.ModelOrder;
import com.example.employeerestaurantappfirestore.model.ModelOrderList;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private View view;
    private Context context;
    private FirebaseFirestore fireStore;
    private DishInOrderAdapter dishInOrderAdapter;
    private static final String ARG_ORDER = "modelOrderList";
    private TextView tv_order_id;
    private EditText et_order_comment, et_order_cost;
    private Spinner spin_table;
    private LinearLayout ll_btn_check, ll_add_dish_btn;
    private NestedScrollView nsv_dish;
    private RecyclerView rv_dishes;

    // TODO: Rename and change types of parameters
    private ModelOrderList modelOrderList;

    public OrderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param modelOrderList Parameter 1.
     * @return A new instance of fragment OrderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderFragment newInstance(ModelOrderList modelOrderList) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER, modelOrderList);
        fragment.setArguments(args);
        return fragment;
    }

    public static OrderFragment newInstanceNewOrder() {
        return newInstance(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            modelOrderList = (ModelOrderList) getArguments().getSerializable(ARG_ORDER);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            view = inflater.inflate(R.layout.fragment_order, container, false);
            initViews();
        } else {
            view = inflater.inflate(R.layout.fragment_order_smart, container, false);
            initViews();
            smartScroll();
        }
        initViews();
        initAdapterForSpinner();
        if (modelOrderList != null) {
            getOrderData();
        }

        return view;
    }

    private void initViews(){
        context = getContext();
        fireStore = FirebaseFirestore.getInstance();
        tv_order_id = view.findViewById(R.id.tv_order_id);
        et_order_comment = view.findViewById(R.id.et_order_comment);
        spin_table = view.findViewById(R.id.spin_table);
        et_order_cost = view.findViewById(R.id.et_order_cost);
        ll_btn_check = view.findViewById(R.id.ll_btn_check);
        ll_add_dish_btn = view.findViewById(R.id.ll_add_dish_btn);
        nsv_dish = view.findViewById(R.id.nsv_dish);
        rv_dishes = view.findViewById(R.id.rv_dishes);
        rv_dishes.setLayoutManager(new LinearLayoutManager(context));
    }

    private void initAdapter(List<ModelOrder.OrderDishes> dishes) {
        dishInOrderAdapter = new DishInOrderAdapter(dishes, OrderFragment.this);
        rv_dishes.setAdapter(dishInOrderAdapter);
    }

    private void getOrderData(){
        tv_order_id.setText(modelOrderList.getOrderId());
        et_order_comment.setText(modelOrderList.getComment());
        et_order_cost.setText(String.valueOf(modelOrderList.getCost()));
        initAdapter(modelOrderList.getDishes());
    }

    private void initAdapterForSpinner(){
        TablesDialog.getTables(fireStore, new TablesDialog.OnTablesLoadedListener() {
            @Override
            public void onTablesLoaded(String[] tables) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_spinner_item,
                        tables
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spin_table.setAdapter(adapter);
                if (modelOrderList != null) {
                    int position = Arrays.asList(tables).indexOf(modelOrderList.getIdTable().getId());
                    if (position != -1) {
                        spin_table.setSelection(position);
                    }
                }
            }

            @Override
            public void onTablesLoadFailed(Exception e) {
                // Обработка ошибки при загрузке таблиц
            }
        });
    }

    private OnScrollListener onScrollListener;
    private void smartScroll() {
        if (context instanceof MainActivity) {
            onScrollListener = (OnScrollListener) context;
        }
        nsv_dish.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
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
}