package com.example.employeerestaurantappfirestore.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeerestaurantappfirestore.R;
import com.example.employeerestaurantappfirestore.adapters.DishAdapter;
import com.example.employeerestaurantappfirestore.managers.MenuManager;
import com.example.employeerestaurantappfirestore.model.ModelDishes;
import com.example.employeerestaurantappfirestore.model.ModelTableList;
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
    private List<ModelDishes> addedDishes;
    private RecyclerView rv_dishes;
    private NestedScrollView nsv_dishes;
    private EditText et_comment;
    private LinearLayout ll_btn_cancellation, ll_btn_added_dishes;
    private DishAdapter dishAdapter;
    private Spinner spin_dish_type;
    private SearchView sv_dishes;
    private int filterPos;
    private String queryText;
    private RelativeLayout rl_dishes_not_found;

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
        sv_dishes = findViewById(R.id.sv_dishes);
        rl_dishes_not_found = findViewById(R.id.rl_dishes_not_found);
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
        spin_dish_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterPos = pos;
                getDishes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sv_dishes.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryText = query;
                getDishes();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                queryText = query;
                getDishes();
                return false;
            }
        });
    }

    private void setStatusVisible(int status1, int status2){
        rl_dishes_not_found.setVisibility(status1);
        nsv_dishes.setVisibility(status2);
    }

    private List<ModelDishes> filterDishesByType(int type) {
        List<ModelDishes> filteredList = new ArrayList<>();
        for (ModelDishes dish : dishes) {
            Log.d("test", dish.getAbbreviatedName() + " " + dish.getIdTypeOfTheDishes().getId() + " " + type);
            if (dish.getIdTypeOfTheDishes().getId().equals(String.valueOf(type))) {
                filteredList.add(dish);
            }
        }
        return filteredList;
    }

    private List<ModelDishes> searchDishes() {
        List<ModelDishes> foundDishes = new ArrayList<>();
        for (ModelDishes dish : dishes){
            if (dish.getName().toLowerCase().contains(queryText.toLowerCase())){
                foundDishes.add(dish);
            }
        }
        return foundDishes;
    }

    private void initData(){
        dishes = new ArrayList<>();
        fireStore = FirebaseFirestore.getInstance();
        queryText = "";
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
        setStatusVisible(View.GONE, View.VISIBLE);
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

            if(filterPos!=0){
                dishes = filterDishesByType(filterPos);
                dishAdapter.notifyDataSetChanged();
                initAdapter();
            }

            if (!Objects.equals(queryText, "") && queryText.length()>1) {
                dishes = searchDishes();
            }

            if (dishes.size()==0){
                setStatusVisible(View.VISIBLE, View.GONE);
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
