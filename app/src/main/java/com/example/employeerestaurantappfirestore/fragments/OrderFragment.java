package com.example.employeerestaurantappfirestore.fragments;

import android.annotation.SuppressLint;
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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.activities.MainActivity;
import com.example.employeerestaurantappfirestore.adapters.DishInOrderAdapter;
import com.example.employeerestaurantappfirestore.dialogs.DishesDialog;
import com.example.employeerestaurantappfirestore.dialogs.TablesDialog;
import com.example.employeerestaurantappfirestore.interfaces.DishChangeListener;
import com.example.employeerestaurantappfirestore.interfaces.OnScrollListener;
import com.example.employeerestaurantappfirestore.interfaces.OrderExtensionListener;
import com.example.employeerestaurantappfirestore.model.ModelDishesQuantity;
import com.example.employeerestaurantappfirestore.model.ModelOrder;
import com.example.employeerestaurantappfirestore.model.ModelOrderList;
import com.example.employeerestaurantappfirestore.utils.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.math3.util.Precision;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderFragment extends Fragment implements DishChangeListener, OrderExtensionListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private View view;
    private Context context;
    private FirebaseFirestore fireStore;
    private static final String ARG_ORDER = "orderId";
    private TextView tv_order_id;
    private EditText et_order_comment, et_order_cost;
    private Spinner spin_table;
    private LinearLayout ll_btn_check, ll_add_dish_btn, ll_save_btn, ll_loading_data;
    private NestedScrollView nsv_dish;
    private RecyclerView rv_dishes;
    private List<ModelOrder.OrderDishes> newDishes;
    DishInOrderAdapter dishInOrderAdapter;

    // TODO: Rename and change types of parameters
    private ModelOrderList modelOrderList;

    public OrderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param orderId Parameter 1.
     * @return A new instance of fragment OrderFragment.
     */
    public static OrderFragment newInstance(String orderId) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    public static OrderFragment newInstanceNewOrder() {
        return newInstance(null);
    }

    // TODO: If null, make a call outside of this method (when initializing views)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String id = (String) getArguments().getSerializable(ARG_ORDER);
            if(id !=null){
                getOrder(id, order -> {
                    modelOrderList = order;
                    if (modelOrderList != null) {
                        getOrderData();
                        initAdapterForSpinner();
                    }
                });
            }
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
        initListeners();

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
        ll_save_btn = view.findViewById(R.id.ll_save_btn);
        ll_loading_data = view.findViewById(R.id.ll_loading_data);
        nsv_dish = view.findViewById(R.id.nsv_dish);
        rv_dishes = view.findViewById(R.id.rv_dishes);
        rv_dishes.setLayoutManager(new LinearLayoutManager(context));
    }

    private void initListeners(){
        ll_save_btn.setOnClickListener(view1 -> {
            saveOrder();
        });
        ll_add_dish_btn.setOnClickListener(view1 -> {
            DishesDialog dishesDialog = new DishesDialog(requireContext(), this);
            dishesDialog.show();
        });
    }

    private void initAdapter(List<ModelOrder.OrderDishes> dishes) {
        dishes.sort((o1, o2) -> o2.getDateTime().compareTo(o1.getDateTime()));
        dishInOrderAdapter = new DishInOrderAdapter(dishes, OrderFragment.this);
        dishInOrderAdapter.setDishChangeListener(OrderFragment.this);
        rv_dishes.setAdapter(dishInOrderAdapter);
    }

    private void getOrderData(){
        tv_order_id.setText(modelOrderList.getOrderId());
        et_order_comment.setText(modelOrderList.getComment());
        et_order_cost.setText(String.valueOf(modelOrderList.getCost()));
        initAdapter(modelOrderList.getDishes());
    }

    private void saveOrder(){
        if (modelOrderList != null){
            Map<String, Object> fields = new HashMap<>();
            String editTextComment = et_order_comment.getText().toString().trim();
            if(!editTextComment.equals(modelOrderList.getComment())){
                fields.put("comment", editTextComment);
            }
            String selectedSpinnerItem = (String) spin_table.getSelectedItem();
            if(!selectedSpinnerItem.equals(modelOrderList.getIdTable().getId())){
                DocumentReference tableReference = fireStore.collection("Tables").document(selectedSpinnerItem);
                fields.put("idTable", tableReference);
            }
            double editTextCost = Precision.round(Double.parseDouble(et_order_cost.getText().toString()), 2);
            if(editTextCost != modelOrderList.getCost()){
                fields.put("cost", editTextCost);
            }
            if(newDishes != null){
                fields.put("dishes", newDishes);
            }
            if (!fields.isEmpty()) {
                if( NetworkUtils.checkNetworkAndShowSnackbar(getActivity())){
                    ll_loading_data.setVisibility(View.VISIBLE);
                    saveField(modelOrderList.getOrderId(), fields);
                }
            }

        }
    }

    private void saveField(String orderId, Map<String, Object> fields){
        DocumentReference orderReference = fireStore.collection("Orders").document(orderId);
        orderReference.update(fields)
                .addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        getOrder(orderId, order -> {
                            modelOrderList = order;
                            if (modelOrderList != null) {
                                getOrderData();
                                newDishes = null;
                                Snackbar.make(requireView(), "Данные сохранены", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Exception e = updateTask.getException();
                        if (e != null) {
                            Log.e("FireStore", Objects.requireNonNull(e.getMessage()));
                        }
                    }
                });
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

    private void getOrder(String id, OnOrderLoadedListener listener){
        DocumentReference orderRef = FirebaseFirestore.getInstance().collection("Orders").document(id);
        orderRef.get().addOnCompleteListener(task -> {
            ll_loading_data.setVisibility(View.VISIBLE);
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    ModelOrderList order = document.toObject(ModelOrderList.class);
                    if (order != null) {
                        order.setOrderId(document.getId());
                    }
                    listener.onOrderLoaded(order);
                    loadingHide();
//                    ll_loading_data.setVisibility(View.GONE);
                } else {
                    // Документ не существует
                    Snackbar.make(requireView(), "Документ не существует", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                // Обработка ошибки
                Log.d("Firestore", "Ошибка при получении документа с заказом", task.getException());
            }
        });
    }

    private void loadingHide(){
        if(ll_loading_data.getVisibility() != View.GONE){
            TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -ll_loading_data.getHeight()-50);
            animate.setDuration(1500);

            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Начало анимации
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Завершение анимации
                    ll_loading_data.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Повторение анимации
                }
            });
            ll_loading_data.startAnimation(animate);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onOrderExtension(List<ModelDishesQuantity> dishesQuantityList, String comment) {
        if(!comment.isEmpty()){
            String existingText = et_order_comment.getText().toString().trim();
            if(existingText.isEmpty()){
                et_order_comment.setText(comment);
            }else{
                et_order_comment.setText(existingText + " | " + comment);
            }
        }

        DocumentReference dishStatusReference = FirebaseFirestore.getInstance().collection("DishStatus").document("1");
        newDishes = dishInOrderAdapter.getItems();
        for (ModelDishesQuantity dishesQuantity: dishesQuantityList){
            DocumentReference dishReference = FirebaseFirestore.getInstance().collection("Dishes").document(dishesQuantity.getDish().getId());
            ModelOrder.OrderDishes dish = new ModelOrder.OrderDishes(
                    new Date(),
                    dishStatusReference,
                    dishReference,
                    dishesQuantity.getQuantity(),
                    dishesQuantity.getDish().getFinalPrice());
            newDishes.add(dish);
        }
        calculateCost(newDishes);
        initAdapter(newDishes);
        smoothScrollUp();
    }

    public void smoothScrollUp() {
        int scrollY = rv_dishes.getScrollY();
        int rvHeight = rv_dishes.getHeight();
        int rvTotalHeight = rv_dishes.computeVerticalScrollRange();
        int difference = rvTotalHeight - rvHeight;

        nsv_dish.post(() -> nsv_dish.smoothScrollTo(0, scrollY + difference));

//        // Прокручиваем RecyclerView в самый низ
//        rv_dishes.post(() -> rv_dishes.smoothScrollToPosition(dishInOrderAdapter.getItemCount() - 1));
//
//        // После того, как RecyclerView завершит прокрутку, прокручиваем NestedScrollView в самый низ
//        rv_dishes.postDelayed(() -> nsv_dish.fullScroll(View.FOCUS_DOWN), 100); // Добавляем небольшую задержку для корректной прокрутки
    }

    public interface OnOrderLoadedListener {
        void onOrderLoaded(ModelOrderList order);
    }

    @Override
    public void onChangeFields(List<ModelOrder.OrderDishes> dishes, boolean isDelete) {
//        modelOrderList.setDishes(dishes);
        newDishes = dishes;
        calculateCost(dishes);
        Log.d("test", "test");
        if(isDelete){
            initAdapter(dishes);
        }
    }

    private void calculateCost(List<ModelOrder.OrderDishes> dishes){
        double cost = 0;
        for(ModelOrder.OrderDishes dish : dishes){
            if(!dish.getIdDishStatus().getId().equals("4")){
                cost += dish.getCost() * dish.getQuantity();
            }
        }
        et_order_cost.setText(String.valueOf(cost));
    }
}