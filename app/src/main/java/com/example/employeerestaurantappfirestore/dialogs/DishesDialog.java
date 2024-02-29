package com.example.employeerestaurantappfirestore.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.adapters.DishAdapter;
import com.example.employeerestaurantappfirestore.managers.MenuManager;
import com.example.employeerestaurantappfirestore.model.ModelDishes;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DishesDialog extends Dialog {
    private final Context context;
    private FirebaseFirestore fireStore;
    private List<ModelDishes> dishes;
    private RecyclerView rv_dishes;
    private NestedScrollView nsv_dishes;
    private EditText et_comment;
    private LinearLayout ll_btn_cancellation, ll_btn_added_dishes;
    private DishAdapter dishAdapter;
    private Spinner spin_dish_type;

    public DishesDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_adding_dishes);

        // Получние ширины экрана
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getWindow()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        // Ширина диалога
        getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        initViews();
        initAdapterForSpinners();
        initListeners();
        initData();
        getDishes();
    }

    private void initViews(){
        rv_dishes = findViewById(R.id.rv_dishes);
        rv_dishes.setLayoutManager(new LinearLayoutManager(context));
        nsv_dishes = findViewById(R.id.nsv_dishes);
        et_comment = findViewById(R.id.et_comment);
        ll_btn_cancellation = findViewById(R.id.ll_btn_cancellation);
        ll_btn_added_dishes = findViewById(R.id.ll_btn_added_dishes);
        spin_dish_type = findViewById(R.id.spin_dish_type);
    }

    private void initAdapter() {
        dishAdapter = new DishAdapter(dishes, DishesDialog.this);
        rv_dishes.setAdapter(dishAdapter);
    }

    private void initAdapterForSpinners(){
        ArrayAdapter<CharSequence> adapterStatus = ArrayAdapter.createFromResource(
                context,
                R.array.dish_types,
                android.R.layout.simple_spinner_item
        );
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_dish_type.setAdapter(adapterStatus);
    }

    private void initListeners(){
        ll_btn_cancellation.setOnClickListener(view -> dismiss());

    }

    private void initData(){
        dishes = new ArrayList<>();
        fireStore = FirebaseFirestore.getInstance();

    }

    private void getDishesMenu(){
        MenuManager menuManager = new MenuManager();
        menuManager.getMenuWithStatus("1", new MenuManager.OnMenuLoadedListener() {
            @Override
            public void onMenuLoaded(List<ModelDishes> dishesList) {
                dishes.clear();
                dishes.addAll(dishesList);
            }

            @Override
            public void onError(String errorMessage) {
                // Обработка ошибок при загрузке данных
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDishes(){
        int scrollY = rv_dishes.getScrollY();
        CollectionReference dishesCollection = fireStore.collection("Dishes");
        dishesCollection.addSnapshotListener((dishesSnapshots, error) -> {
            if (error != null) {
                // Обработка ошибок
                Log.e("Firestore", "Error getting documents: ", error);
                hideProgressBar();
                return;
            }

            dishes.clear();

            assert dishesSnapshots != null;
            for (DocumentSnapshot document : dishesSnapshots) {
                // Преобразование каждого документа в объект ModelDishes
                ModelDishes dish = document.toObject(ModelDishes.class);
                if (dish != null) {
                    dish.setId(document.getId());
                    dishes.add(dish);
                }
            }

            initAdapter();
            dishAdapter.notifyDataSetChanged();
            hideProgressBar();
            rv_dishes.scrollToPosition(scrollY);
        });
    }

    private void hideProgressBar() {
    }


}
